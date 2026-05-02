package gameCore;

import util.Constants;

/**
 * Hilo dedicado SOLO al game loop (lógica + señal de repaint).
 *
 * - Implementa Runnable  → se ejecuta en su propio Thread.
 * - Usa bandera volatile 'running' para parada segura.
 * - Separa update() (lógica) de render() (delegado al panel via Runnable callback).
 * - Controla el drop automático de piezas con System.nanoTime().
 */
public class GameLoop implements Runnable {

    private final GameState state;
    private final Game      game;          // lógica de movimiento/colisión
    private final Runnable  renderCallback; // GamePanel::repaint

    private volatile boolean running = false;
    private Thread thread;

    private long lastDropTime;

    public GameLoop(GameState state, Game game, Runnable renderCallback) {
        this.state          = state;
        this.game           = game;
        this.renderCallback = renderCallback;
    }

    // ── Control del hilo ─────────────────────────────────────────────────

    public void start() {
        running      = true;
        lastDropTime = System.nanoTime();
        thread       = new Thread(this, "GameLoop-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    public boolean isRunning() { return running; }

    // ── Loop principal ────────────────────────────────────────────────────

    @Override
    public void run() {
        long previousTime = System.nanoTime();
        long lag          = 0L;

        while (running && !state.isGameOver()) {

            // ── Pausa: espera sin consumir CPU ──────────────────────────
            if (state.isPaused()) {
                try { Thread.sleep(50); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); break;
                }
                previousTime = System.nanoTime(); // reset para no acumular lag
                lastDropTime = previousTime;
                continue;
            }

            long currentTime = System.nanoTime();
            long elapsed     = currentTime - previousTime;
            previousTime     = currentTime;
            lag             += elapsed;

            // ── Update a paso fijo (TARGET_FPS) ─────────────────────────
            while (lag >= Constants.FRAME_TIME) {
                update();
                lag -= Constants.FRAME_TIME;
            }

            // ── Render ───────────────────────────────────────────────────
            renderCallback.run();

            // ── Cap de FPS: duerme el tiempo sobrante ────────────────────
            long sleepNs = Constants.FRAME_TIME - (System.nanoTime() - currentTime);
            if (sleepNs > 0) {
                try {
                    Thread.sleep(sleepNs / 1_000_000, (int)(sleepNs % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); break;
                }
            }
        }
        running = false;
    }

    // ── Lógica de un frame ────────────────────────────────────────────────

    private void update() {
        long now      = System.nanoTime();
        long dropMs   = (now - lastDropTime) / 1_000_000;

        if (dropMs >= state.getDropInterval()) {
            game.autoDropPiece();
            lastDropTime = now;
        }
    }
}