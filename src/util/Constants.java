package util;

public final class Constants {

    private Constants() {}

    // --- Tablero ---
    public static final int BOARD_COLS   = 10;
    public static final int BOARD_ROWS   = 20;
    public static final int CELL_SIZE    = 30;   // px por celda

    // --- Ventana ---
    public static final int BOARD_W      = BOARD_COLS * CELL_SIZE;   // 300
    public static final int BOARD_H      = BOARD_ROWS * CELL_SIZE;   // 600
    public static final int SIDEBAR_W    = 200;
    public static final int WINDOW_W     = BOARD_W + SIDEBAR_W;      // 500
    public static final int WINDOW_H     = BOARD_H;                  // 600

    // --- Game loop ---
    public static final int TARGET_FPS   = 144;
    public static final long FRAME_TIME  = 1_000_000_000L / TARGET_FPS; // nanosegundos

    // --- Niveles ---
    public static final int MAX_LEVEL    = 5;
    public static final int LINES_PER_LEVEL = 10;

    // --- Vidas ---
    public static final int INITIAL_LIVES = 3;

    // --- Puntaje ---
    public static final int[] LINE_SCORES = {0, 100, 300, 500, 800}; // 0-4 líneas
    public static final int SOFT_DROP_PTS = 1;
    public static final int HARD_DROP_PTS = 2;
    public static final int COMBO_BONUS   = 50;

    // --- Rutas ---
    public static final String SOUNDS_DIR  = "sounds/";
    public static final String IMAGES_DIR  = "images/";
    public static final String SCORES_FILE = "scores.dat";
    public static final int TOP_SCORES     = 10;
}