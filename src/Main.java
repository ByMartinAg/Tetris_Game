import audio.SoundManager;
import score.ScoreManager;
import ui.MenuPanel;
import util.Constants;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Instancias compartidas por toda la app
            SoundManager  sound  = new SoundManager();
            ScoreManager  scores = new ScoreManager();

            JFrame frame = new JFrame("TETRIS");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new MenuPanel(frame, sound, scores));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Cierre limpio: para el audio
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    sound.shutdown();
                }
            });
        });
    }
}