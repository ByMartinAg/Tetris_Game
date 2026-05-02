package audio;

import util.Constants;

import javax.sound.sampled.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestiona toda la reproducción de audio.
 *
 * Mejoras vs versión original:
 *  - Los efectos de sonido se reproducen en un ExecutorService (hilo dedicado)
 *    para no bloquear el game loop ni el EDT.
 *  - Control de volumen aplicado tanto a música como a efectos.
 *  - stopBackgroundMusic() también cierra el Clip para liberar recursos.
 *  - Manejo silencioso de archivos faltantes (el juego no explota).
 */
public class SoundManager {

    private Clip bgClip;
    private float volume = 0.7f;   // 0.0 – 1.0

    /** Pool de 4 hilos para efectos concurrentes (rotate + drop + line_clear a la vez). */
    private final ExecutorService sfxPool = Executors.newFixedThreadPool(4);

    // ── Música de fondo ───────────────────────────────────────────────────

    public synchronized void playBackgroundMusic(String fileName, boolean loop) {
        stopBackgroundMusic();
        try {
            File f = new File(Constants.SOUNDS_DIR + fileName);
            if (!f.exists()) return;

            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            bgClip = AudioSystem.getClip();
            bgClip.open(ais);
            applyVolume(bgClip, volume);
            if (loop) bgClip.loop(Clip.LOOP_CONTINUOUSLY);
            else       bgClip.start();
        } catch (Exception e) {
            System.err.println("SoundManager: error en música " + fileName);
        }
    }

    public synchronized void stopBackgroundMusic() {
        if (bgClip != null) {
            bgClip.stop();
            bgClip.close();
            bgClip = null;
        }
    }

    // ── Efectos de sonido ─────────────────────────────────────────────────

    /** Reproduce un efecto en un hilo separado para no bloquear el game loop. */
    public void playSound(String fileName) {
        sfxPool.execute(() -> {
            try {
                File f = new File(Constants.SOUNDS_DIR + fileName);
                if (!f.exists()) return;

                AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                applyVolume(clip, volume);
                clip.start();
                // Cierra el clip cuando termine la reproducción
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
            } catch (Exception ignored) { /* silencioso */ }
        });
    }

    // ── Volumen ───────────────────────────────────────────────────────────

    public synchronized void setVolume(float vol) {
        this.volume = Math.max(0f, Math.min(1f, vol));
        if (bgClip != null && bgClip.isOpen()) applyVolume(bgClip, this.volume);
    }

    public float getVolume() { return volume; }

    private void applyVolume(Clip clip, float vol) {
        try {
            FloatControl ctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = ctrl.getMaximum() - ctrl.getMinimum();
            ctrl.setValue(ctrl.getMinimum() + range * vol);
        } catch (Exception ignored) {}
    }

    // ── Limpieza ──────────────────────────────────────────────────────────

    public void shutdown() {
        stopBackgroundMusic();
        sfxPool.shutdownNow();
    }
}