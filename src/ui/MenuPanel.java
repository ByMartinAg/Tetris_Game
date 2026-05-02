package ui;

import audio.SoundManager;
import gameConfig.Difficulty;
import gameConfig.Theme;
import score.Score;
import score.ScoreManager;
import util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Menú principal estilo retro arcade.
 *
 * Estética: fondo negro con scanlines animadas, fuente monospace bold,
 * colores neón (amarillo, cian, magenta), bordes pixelados, animación
 * de título con parpadeo y efecto de "insertar moneda".
 */
public class MenuPanel extends JPanel {

    // ── Paleta retro ──────────────────────────────────────────────────────
    private static final Color C_BG       = new Color(8,  8,  18);
    private static final Color C_NEON_Y   = new Color(255, 240,  50);   // amarillo arcade
    private static final Color C_NEON_C   = new Color( 50, 230, 230);   // cian
    private static final Color C_NEON_M   = new Color(230,  50, 230);   // magenta
    private static final Color C_NEON_G   = new Color( 50, 230, 100);   // verde
    private static final Color C_DIM      = new Color(100, 100, 130);
    private static final Color C_PANEL    = new Color( 14,  14,  30);
    private static final Color C_BORDER   = new Color( 60,  60, 120);

    private static final Font  F_TITLE    = new Font("Courier New", Font.BOLD, 52);
    private static final Font  F_LABEL    = new Font("Courier New", Font.BOLD, 13);
    private static final Font  F_SCORE    = new Font("Courier New", Font.PLAIN, 12);
    private static final Font  F_BTN      = new Font("Courier New", Font.BOLD, 16);
    private static final Font  F_SMALL    = new Font("Courier New", Font.PLAIN, 11);

    // ── Estado ────────────────────────────────────────────────────────────
    private final JFrame        frame;
    private final SoundManager  sound;
    private final ScoreManager  scoreManager;

    private JTextField          nameField;
    private JComboBox<Difficulty> diffBox;
    private JComboBox<Theme>    themeBox;
    private JSlider             volSlider;

    // Animación del título
    private boolean titleVisible = true;
    private Timer   blinkTimer;
    private int     scanOffset  = 0;
    private Timer   scanTimer;

    // ─────────────────────────────────────────────────────────────────────

    public MenuPanel(JFrame frame, SoundManager sound, ScoreManager scoreManager) {
        this.frame        = frame;
        this.sound        = sound;
        this.scoreManager = scoreManager;

        setBackground(C_BG);
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(Constants.WINDOW_W, Constants.WINDOW_H));

        // Música de menú
        sound.playBackgroundMusic("menu_music.wav", true);

        buildUI();
        startAnimations();
    }

    // ── Construcción de UI ────────────────────────────────────────────────

    private void buildUI() {
        // Panel central con todo el contenido
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(10, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 8, 6, 8);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx   = 0;

        int row = 0;

        // ── Título ────────────────────────────────────────────────────────
        TitleLabel title = new TitleLabel();
        title.setPreferredSize(new Dimension(400, 100));
        gbc.gridy = row++;
        gbc.insets = new Insets(10, 8, 4, 8);
        center.add(title, gbc);
        gbc.insets = new Insets(6, 8, 6, 8);

        // ── Subtítulo ─────────────────────────────────────────────────────
        JLabel sub = neonLabel("▶  JAVA EDITION  ◀", C_NEON_C, F_LABEL);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = row++;
        center.add(sub, gbc);

        // ── Separador ─────────────────────────────────────────────────────
        gbc.gridy = row++;
        center.add(retroSeparator(), gbc);

        // ── Nombre ────────────────────────────────────────────────────────
        gbc.gridy = row++;
        center.add(neonLabel("JUGADOR", C_NEON_Y, F_LABEL), gbc);

        nameField = new JTextField("Player1", 16);
        styleTextField(nameField);
        gbc.gridy = row++;
        center.add(nameField, gbc);

        // ── Dificultad ────────────────────────────────────────────────────
        gbc.gridy = row++;
        center.add(neonLabel("DIFICULTAD", C_NEON_Y, F_LABEL), gbc);

        diffBox = new JComboBox<>(Difficulty.values());
        diffBox.setSelectedItem(Difficulty.MEDIO);
        styleCombo(diffBox);
        gbc.gridy = row++;
        center.add(diffBox, gbc);

        // ── Tema ──────────────────────────────────────────────────────────
        gbc.gridy = row++;
        center.add(neonLabel("TEMA", C_NEON_Y, F_LABEL), gbc);

        themeBox = new JComboBox<>(Theme.values());
        styleCombo(themeBox);
        gbc.gridy = row++;
        center.add(themeBox, gbc);

        // ── Volumen ───────────────────────────────────────────────────────
        gbc.gridy = row++;
        center.add(neonLabel("VOLUMEN", C_NEON_Y, F_LABEL), gbc);

        volSlider = new JSlider(0, 100, (int)(sound.getVolume() * 100));
        styleSlider(volSlider);
        volSlider.addChangeListener(e -> sound.setVolume(volSlider.getValue() / 100f));
        gbc.gridy = row++;
        center.add(volSlider, gbc);

        // ── Separador ─────────────────────────────────────────────────────
        gbc.gridy = row++;
        center.add(retroSeparator(), gbc);

        // ── Botón INICIAR ─────────────────────────────────────────────────
        JButton startBtn = retroButton("▶  INICIAR JUEGO", C_NEON_G);
        startBtn.addActionListener(e -> startGame());
        gbc.gridy  = row++;
        gbc.insets = new Insets(4, 8, 3, 8);
        center.add(startBtn, gbc);

        // ── Botón RANKING ─────────────────────────────────────────────────
        JButton rankBtn = retroButton("★  RANKING", C_NEON_C);
        rankBtn.addActionListener(e -> showRanking());
        gbc.gridy = row++;
        center.add(rankBtn, gbc);

        // ── Botón SALIR ───────────────────────────────────────────────────
        JButton exitBtn = retroButton("✕  SALIR", C_NEON_M);
        exitBtn.addActionListener(e -> System.exit(0));
        gbc.gridy  = row++;
        gbc.insets = new Insets(3, 8, 8, 8);
        center.add(exitBtn, gbc);

        add(center, BorderLayout.CENTER);

        // ── Controles (footer) ────────────────────────────────────────────
        add(buildControls(), BorderLayout.SOUTH);
    }

    private JPanel buildControls() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        p.setOpaque(false);
        String[] hints = {"← → : MOVER", "↑ : ROTAR", "↓ : BAJAR", "SPACE : DROP", "P : PAUSA"};
        for (String h : hints) {
            JLabel l = new JLabel(h);
            l.setFont(F_SMALL);
            l.setForeground(C_DIM);
            p.add(l);
        }
        return p;
    }

    // ── Acciones ──────────────────────────────────────────────────────────

    private void startGame() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Player1";

        Difficulty diff  = (Difficulty) diffBox.getSelectedItem();
        Theme      theme = (Theme)      themeBox.getSelectedItem();

        blinkTimer.stop();
        scanTimer.stop();
        sound.stopBackgroundMusic();

        frame.getContentPane().removeAll();
        GamePanel gp = new GamePanel(name, diff, theme, frame, sound, scoreManager);
        frame.add(gp);
        frame.revalidate();
        frame.repaint();
        gp.requestFocusInWindow();
    }

    private void showRanking() {
        List<Score> top = scoreManager.getTopScores();
        if (top.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Aún no hay puntajes guardados.\n¡Juega una partida primero!",
                    "RANKING", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-3s %-15s %8s  %s%n", "#", "JUGADOR", "PUNTOS", "FECHA"));
        sb.append("─".repeat(50)).append("\n");
        for (int i = 0; i < top.size(); i++) {
            Score s = top.get(i);
            sb.append(String.format("%-3d %-15s %8d  %s%n",
                    i + 1, s.getPlayerName(), s.getPoints(), s.getDateStr()));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Courier New", Font.PLAIN, 13));
        ta.setEditable(false);
        ta.setBackground(C_BG);
        ta.setForeground(C_NEON_Y);
        JOptionPane.showMessageDialog(this, ta, "▶ TOP " + Constants.TOP_SCORES + " ◀",
                JOptionPane.PLAIN_MESSAGE);
    }

    // ── Animaciones ───────────────────────────────────────────────────────

    private void startAnimations() {
        // Parpadeo del título
        blinkTimer = new Timer(600, e -> {
            titleVisible = !titleVisible;
            repaint();
        });
        blinkTimer.start();

        // Scanlines animadas
        scanTimer = new Timer(40, e -> {
            scanOffset = (scanOffset + 2) % 8;
            repaint();
        });
        scanTimer.start();
    }

    // ── Painting (scanlines + fondo) ──────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Scanlines sutiles sobre el fondo
        g2.setColor(new Color(0, 0, 0, 35));
        for (int y = scanOffset; y < getHeight(); y += 4) {
            g2.fillRect(0, y, getWidth(), 2);
        }

        // Vignette lateral
        GradientPaint vLeft = new GradientPaint(
                0, 0, new Color(0, 0, 0, 120),
                80, 0, new Color(0, 0, 0, 0));
        GradientPaint vRight = new GradientPaint(
                getWidth() - 80, 0, new Color(0, 0, 0, 0),
                getWidth(), 0, new Color(0, 0, 0, 120));
        g2.setPaint(vLeft);  g2.fillRect(0, 0, 80, getHeight());
        g2.setPaint(vRight); g2.fillRect(getWidth() - 80, 0, 80, getHeight());
    }

    // ── Factory de componentes retro ─────────────────────────────────────

    private JLabel neonLabel(String text, Color color, Font font) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(new Color(5, 5, 20));
        tf.setForeground(C_NEON_Y);
        tf.setCaretColor(C_NEON_Y);
        tf.setFont(F_BTN);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)));
        tf.setHorizontalAlignment(JTextField.CENTER);
    }

    private <T> void styleCombo(JComboBox<T> cb) {
        cb.setBackground(new Color(5, 5, 20));
        cb.setForeground(C_NEON_C);
        cb.setFont(F_LABEL);
        cb.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        ((JLabel) cb.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void styleSlider(JSlider sl) {
        sl.setOpaque(false);
        sl.setForeground(C_NEON_Y);
        sl.setPaintTicks(true);
        sl.setMajorTickSpacing(25);
        sl.setPaintLabels(false);
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
                        : hover ? new Color(accent.getRed()/5,
                        accent.getGreen()/5,
                        accent.getBlue()/5)
                        : C_PANEL;
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Borde neón
                g2.setColor(hover || press ? accent : C_BORDER);
                g2.setStroke(new BasicStroke(hover ? 2f : 1f));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                // Texto
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
        btn.setPreferredSize(new Dimension(260, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSeparator retroSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        sep.setBackground(C_BG);
        return sep;
    }

    // ── Subcomponente: título con glow ────────────────────────────────────

    private class TitleLabel extends JComponent {
        @Override protected void paintComponent(Graphics g) {
            if (!titleVisible) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(F_TITLE);

            String text = "TETRIS";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth()  - fm.stringWidth(text)) / 2;
            int ty = (getHeight() + fm.getAscent()) / 2 - 8;

            // Glow en capas
            for (int i = 8; i >= 1; i--) {
                int alpha = 18 + i * 8;
                g2.setColor(new Color(C_NEON_Y.getRed(), C_NEON_Y.getGreen(),
                        C_NEON_Y.getBlue(), Math.min(alpha, 255)));
                g2.drawString(text, tx - i, ty);
                g2.drawString(text, tx + i, ty);
                g2.drawString(text, tx, ty - i);
                g2.drawString(text, tx, ty + i);
            }
            // Texto principal
            g2.setColor(C_NEON_Y);
            g2.drawString(text, tx, ty);
        }
    }
}