package score;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Representa una entrada del ranking. Serializable para guardarse en archivo. */
public class Score implements Serializable, Comparable<Score> {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String        playerName;
    private final int           points;
    private final LocalDateTime date;

    public Score(String playerName, int points) {
        this.playerName = playerName;
        this.points     = points;
        this.date       = LocalDateTime.now();
    }

    public String getPlayerName() { return playerName; }
    public int    getPoints()     { return points;     }
    public String getDateStr()    { return date.format(FMT); }

    /** Orden descendente por puntaje. */
    @Override
    public int compareTo(Score o) { return Integer.compare(o.points, this.points); }

    @Override
    public String toString() {
        return String.format("%-15s %7d pts  %s", playerName, points, getDateStr());
    }
}