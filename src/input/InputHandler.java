package input;

import gameCore.Game;
import gameCore.GameState;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Captura eventos de teclado y los delega a Game.
 * Está completamente desacoplado de GamePanel: no sabe nada de rendering.
 *
 * Teclas:
 *  ← →   mover
 *  ↓     soft drop
 *  ↑     rotar
 *  SPACE hard drop
 *  P     pausar / reanudar
 */
public class InputHandler extends KeyAdapter {

    private final Game      game;
    private final GameState state;

    public InputHandler(Game game, GameState state) {
        this.game  = game;
        this.state = state;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // La pausa se maneja siempre (para poder reanudar)
        if (e.getKeyCode() == KeyEvent.VK_P) {
            game.togglePause();
            return;
        }

        // El resto de controles solo si no está pausado ni game over
        if (state.isPaused() || state.isGameOver()) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT  -> game.moveLeft();
            case KeyEvent.VK_RIGHT -> game.moveRight();
            case KeyEvent.VK_DOWN  -> game.softDrop();
            case KeyEvent.VK_UP    -> game.rotate();
            case KeyEvent.VK_SPACE -> game.hardDrop();

        }
        switch (e.getKeyCode()){
            case KeyEvent.VK_W -> game.rotate();
            case KeyEvent.VK_A -> game.moveLeft();
            case KeyEvent.VK_S -> game.softDrop();
            case KeyEvent.VK_D -> game.moveRight();
            case KeyEvent.VK_F -> game.hardDrop();
        }
    }
}