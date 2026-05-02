package ui;


import gameConfig.Theme;
import gameCore.GameState;
import model.Tetromino;
import util.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Panel lateral derecho del juego.
 * Muestra: siguiente pieza, puntaje, nivel, líneas, vidas, dificultad.
 * Estética retro arcade coherente con MenuPanel.
 */
public class SidebarPanel extends JPanel {

    private static final Color C_BG     = new Color(8,  8, 18);
    private static final Color C_NEON_Y = new Color(255, 240,  50);
    private static final Color C_NEON_C = new Color( 50, 230, 230);
    private static final Color C_NEON_G = new Color( 50, 230, 100);
    private static final Color C_NEON_M = new Color(230,  50, 230);
    private static final Color C_DIM    = new Color( 90,  90, 120);
    private static final Color C_BORDER = new Color( 50,  50, 100);

    private static final Font F_LABEL = new Font("Courier New", Font.BOLD,  12);
    private static final Font F_VALUE = new Font("Courier New", Font.BOLD,  22);
    private static final Font F_SMALL = new Font("Courier New", Font.PLAIN, 11);

    private final GameState state;

    public SidebarPanel(GameState state) {
        this.state = state;
        setPreferredSize(new Dimension(Constants.SIDEBAR_W, Constants.WINDOW_H));
        setBackground(C_BG);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w   = getWidth();
        int pad = 14;
        int y   = 20;

        // ── Nombre del jugador ────────────────────────────────────────────
        y = drawSection(g2, "JUGADOR", state.getPlayerName().toUpperCase(),
                C_NEON_C, pad, y, w);

        // ── Separador ─────────────────────────────────────────────────────
        y = drawSep(g2, pad, y, w);

        // ── Siguiente pieza ───────────────────────────────────────────────
        y = drawLabel(g2, "SIGUIENTE", pad, y, C_NEON_Y);
        y = drawNextPiece(g2, pad, y, w);

        // ── Separador ─────────────────────────────────────────────────────
        y = drawSep(g2, pad, y, w);

        // ── Puntaje ───────────────────────────────────────────────────────
        y = drawSection(g2, "PUNTAJE",
                String.format("%07d", state.getScore()),
                C_NEON_Y, pad, y, w);

        // ── Nivel ─────────────────────────────────────────────────────────
        y = drawSection(g2, "NIVEL",
                state.getLevel() + " / " + Constants.MAX_LEVEL,
                C_NEON_G, pad, y, w);

        // ── Líneas ────────────────────────────────────────────────────────
        y = drawSection(g2, "LÍNEAS",
                String.valueOf(state.getLinesTotal()),
                C_NEON_C, pad, y, w);

        // ── Separador ─────────────────────────────────────────────────────
        y = drawSep(g2, pad, y, w);

        // ── Vidas ─────────────────────────────────────────────────────────
        y = drawLabel(g2, "VIDAS", pad, y, C_NEON_Y);
        y = drawLives(g2, pad, y, w);

        // ── Separador ─────────────────────────────────────────────────────
        y = drawSep(g2, pad, y, w);

        // ── Combo ─────────────────────────────────────────────────────────
        if (state.getCombo() > 1) {
            y = drawSection(g2, "COMBO", "x" + state.getCombo(),
                    C_NEON_M, pad, y, w);
        }

        // ── Dificultad ────────────────────────────────────────────────────
        y = drawSection(g2, "MODO",
                state.getDifficulty().getLabel().toUpperCase(),
                C_DIM.brighter(), pad, y, w);

        // ── Pausa overlay ─────────────────────────────────────────────────
        if (state.isPaused()) {
            drawPausedOverlay(g2);
        }
    }

    // ── Helpers de dibujo ─────────────────────────────────────────────────

    private int drawLabel(Graphics2D g2, String label, int x, int y, Color color) {
        g2.setFont(F_LABEL);
        g2.setColor(color);
        g2.drawString(label, x, y + 12);
        return y + 18;
    }

    private int drawSection(Graphics2D g2, String label, String value,
                            Color valColor, int x, int y, int w) {
        g2.setFont(F_LABEL);
        g2.setColor(C_DIM);
        g2.drawString(label, x, y + 12);
        y += 16;

        g2.setFont(F_VALUE);
        g2.setColor(valColor);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(value)) / 2;
        g2.drawString(value, Math.max(tx, x), y + fm.getAscent());
        return y + fm.getHeight() + 8;
    }

    private int drawSep(Graphics2D g2, int x, int y, int w) {
        g2.setColor(C_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x, y + 4, w - x, y + 4);
        return y + 14;
    }

    private int drawNextPiece(Graphics2D g2, int x, int y, int w) {
        Tetromino next  = state.getNextPiece();
        Theme     theme = state.getTheme();
        if (next == null) return y + 80;

        int[][] shape   = next.getShape();
        int     cell    = 22;
        int     cols    = shape[0].length;
        int     rows    = shape.length;
        int     startX  = (w - cols * cell) / 2;
        int     startY  = y + 8;

        // Fondo de la caja
        int boxW = cols * cell + 16;
        int boxH = rows * cell + 16;
        int boxX = (w - boxW) / 2;
        g2.setColor(new Color(14, 14, 30));
        g2.fillRect(boxX, startY - 8, boxW, boxH);
        g2.setColor(C_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(boxX, startY - 8, boxW, boxH);

        // Dibujar celdas
        Color pieceColor = next.getColor(theme);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (shape[i][j] == 1) {
                    int px = startX + j * cell;
                    int py = startY + i * cell;
                    g2.setColor(pieceColor);
                    g2.fillRect(px, py, cell - 2, cell - 2);
                    // Brillo superior
                    g2.setColor(pieceColor.brighter());
                    g2.fillRect(px, py, cell - 2, 3);
                    g2.fillRect(px, py, 3, cell - 2);
                }
            }
        }
        return startY + boxH + 10;
    }

    private int drawLives(Graphics2D g2, int x, int y, int w) {
        int lives = state.getLives();
        int heartSize = 18;
        int total = Constants.INITIAL_LIVES * (heartSize + 6);
        int startX = (w - total) / 2;

        for (int i = 0; i < Constants.INITIAL_LIVES; i++) {
            boolean alive = i < lives;
            g2.setFont(new Font("Courier New", Font.BOLD, heartSize));
            g2.setColor(alive ? C_NEON_M : C_DIM);
            g2.drawString("♥", startX + i * (heartSize + 6), y + heartSize);
        }
        return y + heartSize + 12;
    }

    private void drawPausedOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(new Font("Courier New", Font.BOLD, 16));
        g2.setColor(C_NEON_Y);
        String txt = "PAUSA";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, getHeight() / 2);
    }
}