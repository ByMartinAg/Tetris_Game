package gameCore;
import audio.SoundManager;
import model.Tetromino;
import score.ScoreManager;

import javax.swing.*;
import java.awt.*;

/**
 * Contiene TODA la lógica de juego (sin nada de rendering).
 *
 * - Movimiento / rotación / hard drop
 * - Colocación de pieza y limpieza de líneas
 * - Auto-drop llamado por GameLoop
 * - Game Over y manejo de vidas
 *
 * Es el único lugar donde se modifica GameState desde hilos de juego.
 */
public class Game {

    private final GameState    state;
    private final SoundManager sound;
    private final ScoreManager scoreManager;
    private final Runnable     onGameOver;   // callback → GamePanel muestra GameOverPanel

    public Game(GameState state, SoundManager sound,
                ScoreManager scoreManager, Runnable onGameOver) {
        this.state        = state;
        this.sound        = sound;
        this.scoreManager = scoreManager;
        this.onGameOver   = onGameOver;
    }

    // ── Movimiento ────────────────────────────────────────────────────────

    public synchronized void moveLeft() {
        if (state.isPaused() || state.isGameOver()) return;
        Tetromino p = state.getCurrentPiece();
        p.setX(p.getX() - 1);
        if (!state.getBoard().isValidPosition(p)) p.setX(p.getX() + 1);
    }

    public synchronized void moveRight() {
        if (state.isPaused() || state.isGameOver()) return;
        Tetromino p = state.getCurrentPiece();
        p.setX(p.getX() + 1);
        if (!state.getBoard().isValidPosition(p)) p.setX(p.getX() - 1);
    }

    public synchronized void softDrop() {
        if (state.isPaused() || state.isGameOver()) return;
        Tetromino p = state.getCurrentPiece();
        p.setY(p.getY() + 1);
        if (!state.getBoard().isValidPosition(p)) {
            p.setY(p.getY() - 1);
            lockPiece();
        } else {
            state.addSoftDrop();
        }
    }

    public synchronized void hardDrop() {
        if (state.isPaused() || state.isGameOver()) return;
        Tetromino p   = state.getCurrentPiece();
        int cells = 0;
        while (canMoveDown()) { p.setY(p.getY() + 1); cells++; }
        state.addHardDrop(cells);
        lockPiece();
    }

    public synchronized void rotate() {
        if (state.isPaused() || state.isGameOver()) return;
        Tetromino p       = state.getCurrentPiece();
        int[][]   oldShape = p.getShape();
        int       oldX     = p.getX();

        p.rotate();

        // Wall kick básico: si queda fuera, intenta corrección ±1 y ±2
        if (!state.getBoard().isValidPosition(p)) {
            boolean fixed = false;
            for (int kick : new int[]{1, -1, 2, -2}) {
                p.setX(oldX + kick);
                if (state.getBoard().isValidPosition(p)) { fixed = true; break; }
            }
            if (!fixed) {
                p.setShape(oldShape);
                p.setX(oldX);
                return;
            }
        }
        sound.playSound("rotate.wav");
    }

    // ── Drop automático (llamado por GameLoop) ────────────────────────────

    public synchronized void autoDropPiece() {
        if (state.isPaused() || state.isGameOver()) return;
        Tetromino p = state.getCurrentPiece();
        p.setY(p.getY() + 1);
        if (!state.getBoard().isValidPosition(p)) {
            p.setY(p.getY() - 1);
            lockPiece();
        }
    }

    // ── Colocación y limpieza ─────────────────────────────────────────────

    private void lockPiece() {
        state.getBoard().placePiece(state.getCurrentPiece());
        sound.playSound("drop.wav");

        int lines = state.getBoard().clearLines();
        state.addLinesCleared(lines);

        if (lines > 0) sound.playSound("line_clear.wav");

        state.spawnNextPiece();
        if (state.isGameOver()) {
            handleGameOver();
        }
    }

    private boolean canMoveDown() {
        Tetromino p = state.getCurrentPiece();
        p.setY(p.getY() + 1);
        boolean ok = state.getBoard().isValidPosition(p);
        p.setY(p.getY() - 1);
        return ok;
    }

    // ── Game Over / Vidas ─────────────────────────────────────────────────

    private void handleGameOver() {
        sound.stopBackgroundMusic();

        boolean hasLives = state.loseLife();

        if (hasLives) {
            // Queda vida: diálogo rápido y continúa
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null,
                            state.getPlayerName() + ", ¡perdiste una vida!\n" +
                                    "Te quedan " + state.getLives() + " oportunidades.",
                            "¡Vida perdida!", JOptionPane.WARNING_MESSAGE)
            );
            sound.playBackgroundMusic(state.getTheme().getMusicFile(), true);
        } else {
            // Sin vidas: guarda puntaje y delega al panel
            scoreManager.addScore(state.getPlayerName(), state.getScore());
            SwingUtilities.invokeLater(onGameOver);
        }
    }

    // ── Pausa ─────────────────────────────────────────────────────────────

    public void togglePause() {
        state.setPaused(!state.isPaused());
    }
}