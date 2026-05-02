package ui;

import audio.SoundManager;
import score.ScoreManager;
import util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Pantalla de Game Over estilo retro arcade.
 * Muestra puntaje final, nombre del jugador y botones de Reintentar / Menú.
 */
public class GameOverPanel extends JPanel {

    private static final Color C_BG     = new Color(5,  5, 12);
    private static final Color C_RED    = new Color(230, 40, 40);
    private static final Color C_NEON_Y = new Color(255, 240, 50);
    private static final Color C_NEON_C = new Color( 50, 230, 230);
    private static final Color C_DIM    = new Color( 80,  80, 110);
    private static final Color C_BORDER = new Color( 60,  30,  30);

    private static final Font F_OVER  = new Font("Courier New", Font.BOLD,  54);
    private static final Font F_LABEL = new Font("Courier New", Font.BOLD,  14);
    private static final Font F_SCORE = new Font("Courier New", Font.BOLD,  32);
    private static final Font F_BTN   = new Font("Courier New", Font.BOLD,  16);

    private final String       playerName;
    private final int          finalScore;
    private final JFrame       frame;
    private final SoundManager sound;
    private final ScoreManager scoreManager;

    // Para animación de parpadeo
    private boolean textVisible = true;
    private Timer   blinkTimer;
    private int     glowPhase   = 0;
    private Timer   glowTimer;

    public GameOverPanel(String playerName, int finalScore,
                         JFrame frame, SoundManager sound, ScoreManager scoreManager) {
        this.playerName   = playerName;
        this.finalScore   = finalScore;
        this.frame        = frame;
        this.sound        = sound;
        this.scoreManager = scoreManager;

        setBackground(C_BG);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(Constants.WINDOW_W, Constants.WINDOW_H));

        buildUI();
        startAnimations();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.weightx = 1.0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(10, 60, 10, 60);
        int row = 0;

        // ── GAME OVER (canvas personalizado para glow) ────────────────────
        GameOverTitle titleCanvas = new GameOverTitle();
        titleCanvas.setPreferredSize(new Dimension(400, 110));
        gbc.gridy = row++;
        add(titleCanvas, gbc);

        // ── Nombre ────────────────────────────────────────────────────────
        JLabel nameLabel = centeredLabel(playerName.toUpperCase(), C_NEON_C, F_LABEL);
        gbc.gridy = row++;
        add(nameLabel, gbc);

        // ── Puntaje ───────────────────────────────────────────────────────
        gbc.insets = new Insets(2, 60, 2, 60);
        JLabel scoreTitle = centeredLabel("PUNTAJE FINAL", C_DIM, F_LABEL);
        gbc.gridy = row++;
        add(scoreTitle, gbc);

        JLabel scoreLabel = centeredLabel(String.format("%07d", finalScore), C_NEON_Y, F_SCORE);
        gbc.gridy = row++;
        add(scoreLabel, gbc);

        // ── Ranking ───────────────────────────────────────────────────────
        gbc.insets = new Insets(6, 60, 16, 60);
        JLabel rankPos = buildRankPosition();
        gbc.gridy = row++;
        add(rankPos, gbc);

        // ── Separador ─────────────────────────────────────────────────────
        gbc.insets = new Insets(4, 60, 4, 60);
        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        gbc.gridy = row++;
        add(sep, gbc);

        // ── Botones ───────────────────────────────────────────────────────
        gbc.insets = new Insets(8, 60, 4, 60);
        JButton retryBtn = retroButton("▶  REINTENTAR", C_NEON_C);
        retryBtn.addActionListener(e -> retry());
        gbc.gridy = row++;
        add(retryBtn, gbc);

        gbc.insets = new Insets(4, 60, 8, 60);
        JButton menuBtn = retroButton("⌂  MENÚ PRINCIPAL", new Color(180, 180, 50));
        menuBtn.addActionListener(e -> goToMenu());
        gbc.gridy = row++;
        add(menuBtn, gbc);
    }

    private JLabel buildRankPosition() {
        var scores = scoreManager.getTopScores();
        int pos = -1;
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i).getPlayerName().equals(playerName)
                    && scores.get(i).getPoints() == finalScore) {
                pos = i + 1; break;
            }
        }
        String text = pos > 0
                ? "★  POSICIÓN #" + pos + " EN EL RANKING"
                : "SIN ENTRADA EN EL RANKING";
        Color color = pos == 1 ? new Color(255, 200, 0)
                : pos > 0 ? C_NEON_C
                : C_DIM;
        return centeredLabel(text, color, F_LABEL);
    }

    private void startAnimations() {
        blinkTimer = new Timer(700, e -> { textVisible = !textVisible; repaint(); });
        blinkTimer.start();
        glowTimer  = new Timer(50,  e -> { glowPhase = (glowPhase + 1) % 30; repaint(); });
        glowTimer.start();
    }

    // ── Acciones ──────────────────────────────────────────────────────────

    private void retry() {
        stopTimers();
        // Volvemos al menú para que elija config de nuevo
        goToMenu();
    }

    private void goToMenu() {
        stopTimers();
        frame.getContentPane().removeAll();
        frame.add(new MenuPanel(frame, sound, scoreManager));
        frame.revalidate();
        frame.repaint();
    }

    private void stopTimers() {
        if (blinkTimer != null) blinkTimer.stop();
        if (glowTimer  != null) glowTimer.stop();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private JLabel centeredLabel(String text, Color color, Font font) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private JButton retroButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                boolean press = getModel().isPressed();
                Color bg = press ? accent.darker().darker()
                        : hover ? new Color(accent.getRed()/6,
                        accent.getGreen()/6,
                        accent.getBlue()/6)
                        : new Color(14, 14, 30);
                g2.setColor(bg); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(hover || press ? accent : C_BORDER);
                g2.setStroke(new BasicStroke(hover ? 2f : 1f));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setFont(getFont());
                g2.setColor(hover || press ? accent : C_DIM.brighter());
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D r  = fm.getStringBounds(getText(), g2);
                g2.drawString(getText(),
                        (int)((getWidth()  - r.getWidth())  / 2),
                        (int)((getHeight() - r.getHeight()) / 2) + fm.getAscent());
            }
        };
        btn.setFont(F_BTN);
        btn.setPreferredSize(new Dimension(280, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Canvas del título GAME OVER ───────────────────────────────────────

    private class GameOverTitle extends JComponent {
        @Override protected void paintComponent(Graphics g) {
            if (!textVisible) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(F_OVER);
            String text = "GAME OVER";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth()  - fm.stringWidth(text)) / 2;
            int ty = (getHeight() + fm.getAscent()) / 2 - 10;

            // Glow pulsante
            double pulse = Math.sin(glowPhase * Math.PI / 15.0);
            int maxLayers = (int)(6 + pulse * 4);
            for (int i = maxLayers; i >= 1; i--) {
                int alpha = (int)(20 + pulse * 15) + i * 10;
                g2.setColor(new Color(C_RED.getRed(), C_RED.getGreen(),
                        C_RED.getBlue(), Math.min(alpha, 255)));
                g2.drawString(text, tx - i, ty);
                g2.drawString(text, tx + i, ty);
                g2.drawString(text, tx, ty - i);
                g2.drawString(text, tx, ty + i);
            }
            g2.setColor(C_RED);
            g2.drawString(text, tx, ty);
        }
    }
}