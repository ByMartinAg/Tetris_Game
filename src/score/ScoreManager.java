package score;

import util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carga y guarda el ranking en un archivo binario (serialización Java).
 * Thread-safe: todos los métodos públicos son synchronized.
 */
public class ScoreManager {

    private final List<Score> scores = new ArrayList<>();
    private final File        file   = new File(Constants.SCORES_FILE);

    public ScoreManager() { load(); }

    // ── API pública ───────────────────────────────────────────────────────

    public synchronized void addScore(String name, int points) {
        scores.add(new Score(name, points));
        Collections.sort(scores);
        if (scores.size() > Constants.TOP_SCORES)
            scores.subList(Constants.TOP_SCORES, scores.size()).clear();
        save();
    }

    /** Devuelve copia inmutable del ranking actual. */
    public synchronized List<Score> getTopScores() {
        return Collections.unmodifiableList(new ArrayList<>(scores));
    }

    // ── Persistencia ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void load() {
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<Score> loaded = (List<Score>) in.readObject();
            scores.addAll(loaded);
            Collections.sort(scores);
        } catch (Exception e) {
            System.err.println("ScoreManager: no se pudo leer " + file.getName());
        }
    }

    private void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new ArrayList<>(scores));
        } catch (IOException e) {
            System.err.println("ScoreManager: no se pudo guardar " + file.getName());
        }
    }
}