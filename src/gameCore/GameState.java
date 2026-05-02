package gameCore;

import gameConfig.Difficulty;
import gameConfig.Theme;
import model.Board;
import model.Tetromino;
import util.Constants;

import java.util.Random;

/**
 * Contiene TODO el estado mutable del juego.
 * Es el único objeto que Game, GameLoop y GamePanel comparten;
 * así evitamos pasar decenas de parámetros entre clases.
 *
 * Acceso sincronizado donde se necesite (marcado con synchronized).
 */
public class GameState {

    // ── Configuración de partida ──────────────────────────────────────────
    private final String     playerName;
    private final Difficulty difficulty;
    private final Theme      theme;

    // ── Tablero y piezas ─────────────────────────────────────────────────
    private Board     board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private final Random random = new Random();

    // ── Métricas de juego ─────────────────────────────────────────────────
    private int  score      = 0;
    private int  level      = 1;
    private int  linesTotal = 0;
    private int  lives      = Constants.INITIAL_LIVES;
    private int  combo      = 0;   // líneas consecutivas con eliminación

    // ── Flags de estado ───────────────────────────────────────────────────
    private volatile boolean paused   = false;
    private volatile boolean gameOver = false;

    // ── Velocidad ─────────────────────────────────────────────────────────
    private long dropInterval;

    // ─────────────────────────────────────────────────────────────────────
    public GameState(String playerName, Difficulty difficulty, Theme theme) {
        this.playerName   = playerName;
        this.difficulty   = difficulty;
        this.theme        = theme;
        this.dropInterval = difficulty.getDropInterval();
        this.board        = new Board();
        this.nextPiece    = generatePiece();
        spawnNextPiece();
    }

    // ── Piezas ───────────────────────────────────────────────────────────

    private Tetromino generatePiece() {
        return new Tetromino(random.nextInt(7));
    }

    /** Mueve nextPiece a currentPiece y genera una nueva nextPiece. */
    public synchronized void spawnNextPiece() {
        currentPiece = nextPiece;
        nextPiece    = generatePiece();

        if (!board.isValidPosition(currentPiece)) {
            gameOver = true;
        }
    }

    // ── Lógica de puntaje ─────────────────────────────────────────────────

    public synchronized void addLinesCleared(int lines) {
        if (lines <= 0) { combo = 0; return; }

        // Puntaje base por líneas (tabla Tetris clásica)
        int base = Constants.LINE_SCORES[Math.min(lines, 4)];
        int pts  = base * level;

        // Bonus de combo
        combo++;
        if (combo > 1) pts += Constants.COMBO_BONUS * (combo - 1) * level;

        score      += pts;
        linesTotal += lines;
        updateLevel();
    }

    public synchronized void addSoftDrop() { score += Constants.SOFT_DROP_PTS; }

    public synchronized void addHardDrop(int cells) {
        score += cells * Constants.HARD_DROP_PTS;
    }

    private void updateLevel() {
        int newLevel = (linesTotal / difficulty.getLinesPerLevel()) + 1;
        if (newLevel > level && newLevel <= Constants.MAX_LEVEL) {
            level        = newLevel;
            dropInterval = difficulty.intervalForLevel(level);
        }
    }

    // ── Vidas ─────────────────────────────────────────────────────────────

    /** Resta una vida y reinicia el tablero. Devuelve true si quedan vidas. */
    public synchronized boolean loseLife() {
        lives--;
        if (lives > 0) {
            board        = new Board();
            linesTotal   = 0;
            level        = 1;
            combo        = 0;
            score        = 0;
            dropInterval = difficulty.getDropInterval();
            gameOver     = false;
            nextPiece    = generatePiece();
            spawnNextPiece();
            return true;
        }
        return false;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public String     getPlayerName()   { return playerName;   }
    public Difficulty getDifficulty()   { return difficulty;   }
    public Theme      getTheme()        { return theme;        }
    public Board      getBoard()        { return board;        }

    public synchronized Tetromino getCurrentPiece() { return currentPiece; }
    public synchronized Tetromino getNextPiece()    { return nextPiece;    }

    public synchronized int  getScore()      { return score;      }
    public synchronized int  getLevel()      { return level;      }
    public synchronized int  getLinesTotal() { return linesTotal; }
    public synchronized int  getLives()      { return lives;      }
    public synchronized int  getCombo()      { return combo;      }
    public synchronized long getDropInterval(){ return dropInterval; }

    public synchronized boolean isPaused()   { return paused;   }
    public synchronized boolean isGameOver() { return gameOver; }

    public synchronized void setPaused(boolean paused)     { this.paused   = paused;   }
    public synchronized void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
}