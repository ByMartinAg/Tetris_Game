package ui;


import audio.SoundManager;
import gameConfig.Theme;
import gameConfig.ThemeManager;
import gameConfig.Difficulty;
import gameCore.Game;
import gameCore.GameLoop;
import gameCore.GameState;
import input.InputHandler;
import model.Board;
import model.Tetromino;
import score.ScoreManager;
import util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Panel principal de juego.
 *
 * Responsabilidades:
 *  - Dibujar tablero, pieza actual, ghost piece, fondo del tema.
 *  - Mostrar overlay de pausa.
 *  - Arrancar GameLoop y Game.
 *  - Delegar input a InputHandler.
 *  - Mostrar GameOverPanel cuando el Game lo indique.
 *
 * NO contiene lógica de movimiento ni de puntuación.
 */
public class GamePanel extends JPanel {

    // ── Colores base ─────────────────────────────────────────────────────
    private static final Color C_PAUSE_OVL = new Color(0, 0, 0, 170);
    private static final Font  F_PAUSE     = new Font("Courier New", Font.BOLD, 28);
    private static final Font  F_PAUSE_SUB = new Font("Courier New", Font.PLAIN, 13);

    // ── Componentes ───────────────────────────────────────────────────────
    private final GameState    state;
    private final Game         game;
    private final GameLoop     gameLoop;
    private final SidebarPanel sidebar;

    public GamePanel(String playerName, Difficulty difficulty, Theme theme,
                     JFrame frame, SoundManager sound, ScoreManager scoreManager) {

        state = new GameState(playerName, difficulty, theme);

        game  = new Game(state, sound, scoreManager, () -> showGameOver(frame, sound, scoreManager));

        sidebar  = new SidebarPanel(state);

        // Layout: tablero a la izquierda, sidebar a la derecha
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JPanel boardWrapper = new BoardCanvas();
        boardWrapper.setPreferredSize(new Dimension(Constants.BOARD_W, Constants.BOARD_H));
        add(boardWrapper, BorderLayout.CENTER);
        add(sidebar,      BorderLayout.EAST);

        // Input
        InputHandler input = new InputHandler(game, state);
        addKeyListener(input);
        setFocusable(true);

        // Música del tema
        sound.playBackgroundMusic(theme.getMusicFile(), true);

        // GameLoop: usa repaint() de este panel como callback de render
        gameLoop = new GameLoop(state, game, () ->
                SwingUtilities.invokeLater(this::repaint)
        );
        gameLoop.start();
    }

    // ── Game Over ─────────────────────────────────────────────────────────

    private void showGameOver(JFrame frame, SoundManager sound, ScoreManager sm) {
        gameLoop.stop();
        frame.getContentPane().removeAll();
        frame.add(new GameOverPanel(
                state.getPlayerName(), state.getScore(), frame, sound, sm));
        frame.revalidate();
        frame.repaint();
    }

    // ── Canvas del tablero (subpanel que dibuja todo el juego) ────────────

    private class BoardCanvas extends JPanel {

        BoardCanvas() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Theme theme = state.getTheme();
            int   cs    = Constants.CELL_SIZE;
            int   cols  = Constants.BOARD_COLS;
            int   rows  = Constants.BOARD_ROWS;
            Board board = state.getBoard();

            // ── Fondo del tema ────────────────────────────────────────────
            BufferedImage bg = ThemeManager.getInstance().getBackground(theme);
            if (bg != null) {
                g2.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
                // Capa semitransparente para que la cuadrícula se vea
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.setColor(theme.getBgFallback());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // ── Cuadrícula ────────────────────────────────────────────────
            g2.setColor(new Color(theme.getGridColor().getRed(),
                    theme.getGridColor().getGreen(),
                    theme.getGridColor().getBlue(), 80));
            g2.setStroke(new BasicStroke(0.5f));
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    g2.drawRect(c * cs, r * cs, cs, cs);

            // ── Celdas colocadas ──────────────────────────────────────────
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int val = board.getCell(r, c);
                    if (val > 0) {
                        Color color = theme.getPieceColor(val - 1);
                        drawCell(g2, c * cs, r * cs, cs, color, false);
                    }
                }
            }

            // ── Ghost piece ───────────────────────────────────────────────
            Tetromino current = state.getCurrentPiece();
            if (current != null && !state.isPaused()) {
                int ghostY = board.ghostY(current);
                int[][] shape = current.getShape();
                Color ghostColor = new Color(
                        theme.getPieceColor(current.getType()).getRed(),
                        theme.getPieceColor(current.getType()).getGreen(),
                        theme.getPieceColor(current.getType()).getBlue(), 60);

                for (int i = 0; i < shape.length; i++)
                    for (int j = 0; j < shape[i].length; j++)
                        if (shape[i][j] == 1) {
                            int px = (current.getX() + j) * cs;
                            int py = (ghostY + i) * cs;
                            g2.setColor(ghostColor);
                            g2.fillRect(px, py, cs, cs);
                            g2.setColor(ghostColor.brighter());
                            g2.setStroke(new BasicStroke(1f));
                            g2.drawRect(px, py, cs - 1, cs - 1);
                        }

                // ── Pieza actual ──────────────────────────────────────────
                Color pieceColor = current.getColor(theme);
                for (int i = 0; i < shape.length; i++)
                    for (int j = 0; j < shape[i].length; j++)
                        if (shape[i][j] == 1)
                            drawCell(g2,
                                    (current.getX() + j) * cs,
                                    (current.getY() + i) * cs,
                                    cs, pieceColor, true);
            }

            // ── Overlay de pausa ──────────────────────────────────────────
            if (state.isPaused()) {
                g2.setColor(C_PAUSE_OVL);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setFont(F_PAUSE);
                g2.setColor(new Color(255, 240, 50));
                drawCenteredString(g2, "PAUSA", getHeight() / 2 - 20);

                g2.setFont(F_PAUSE_SUB);
                g2.setColor(new Color(150, 150, 180));
                drawCenteredString(g2, "Presiona P para continuar", getHeight() / 2 + 16);
            }
        }

        // ── Helpers ───────────────────────────────────────────────────────

        /** Dibuja una celda con efecto 3D retro (bisel). */
        private void drawCell(Graphics2D g2, int x, int y, int cs,
                              Color color, boolean bright) {
            // Relleno
            g2.setColor(bright ? color : color.darker());
            g2.fillRect(x, y, cs, cs);

            // Brillo superior-izquierdo
            g2.setColor(color.brighter());
            g2.fillRect(x, y, cs, 3);
            g2.fillRect(x, y, 3, cs);

            // Sombra inferior-derecho
            g2.setColor(color.darker().darker());
            g2.fillRect(x, y + cs - 3, cs, 3);
            g2.fillRect(x + cs - 3, y, 3, cs);
        }

        private void drawCenteredString(Graphics2D g2, String text, int y) {
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            g2.drawString(text, x, y);
        }
    }
}