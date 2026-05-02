package gameConfig;
/**
 * Cada dificultad define:
 *  - dropInterval   : ms entre caídas automáticas en nivel 1
 *  - speedStep      : ms que se resta al intervalo por cada nivel ganado
 *  - minInterval    : límite inferior (ms) para no volver el juego injugable
 *  - linesPerLevel  : líneas necesarias para subir de nivel
 */
public enum Difficulty {

    FACIL   ("Fácil",    1200, 100, 600, 12),
    MEDIO   ("Medio",    1000, 150, 400, 10),
    DIFICIL ("Difícil",   800, 175, 300,  8),
    AVANZADO("Avanzado",  600, 180, 200,  6),
    EXPERTO ("Experto",   400, 190, 100,  5);

    private final String label;
    private final long   dropInterval;
    private final long   speedStep;
    private final long   minInterval;
    private final int    linesPerLevel;

    Difficulty(String label, long dropInterval, long speedStep,
               long minInterval, int linesPerLevel) {
        this.label        = label;
        this.dropInterval = dropInterval;
        this.speedStep    = speedStep;
        this.minInterval  = minInterval;
        this.linesPerLevel = linesPerLevel;
    }

    /** Calcula el intervalo de caída para un nivel dado (1-based). */
    public long intervalForLevel(int level) {
        long interval = dropInterval - (long)(level - 1) * speedStep;
        return Math.max(interval, minInterval);
    }

    public String  getLabel()        { return label;        }
    public long    getDropInterval() { return dropInterval; }
    public int     getLinesPerLevel(){ return linesPerLevel;}

    @Override
    public String toString() { return label; }
}