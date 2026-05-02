package model;

import gameConfig.Theme;
import java.awt.Color;

/**
 * Representa una pieza de Tetris (Tetromino).
 *
 * Mejoras vs versión original:
 *  - El color se resuelve desde el Theme activo (ya no hardcodeado).
 *  - Guarda una copia de la forma original para reset rápido.
 *  - Método clone() para calcular ghost piece sin modificar la pieza real.
 *  - Wall-kick data básico expuesto para que Game lo use.
 */
public class Tetromino {

    // ── Formas base de los 7 tetrominós (I O T S Z J L) ──────────────────
    private static final int[][][] SHAPES = {
            // 0 - I
            {{0,0,0,0},
                    {1,1,1,1},
                    {0,0,0,0},
                    {0,0,0,0}},
            // 1 - O
            {{1,1},
                    {1,1}},
            // 2 - T
            {{0,1,0},
                    {1,1,1},
                    {0,0,0}},
            // 3 - S
            {{0,1,1},
                    {1,1,0},
                    {0,0,0}},
            // 4 - Z
            {{1,1,0},
                    {0,1,1},
                    {0,0,0}},
            // 5 - J
            {{1,0,0},
                    {1,1,1},
                    {0,0,0}},
            // 6 - L
            {{0,0,1},
                    {1,1,1},
                    {0,0,0}}
    };

    private int[][] shape;
    private final int type;
    private int x, y;

    public Tetromino(int type) {
        this.type  = type;
        this.shape = copyShape(SHAPES[type]);
        this.x     = 3;
        this.y     = 0;
    }

    /** Constructor de copia (para ghost piece). */
    private Tetromino(Tetromino src) {
        this.type  = src.type;
        this.shape = copyShape(src.shape);
        this.x     = src.x;
        this.y     = src.y;
    }

    // ── Rotación ──────────────────────────────────────────────────────────

    /** Rota 90° en sentido horario. */
    public void rotate() {
        int n = shape.length;
        int m = shape[0].length;
        int[][] nueva = new int[m][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                nueva[j][n - 1 - i] = shape[i][j];
        shape = nueva;
    }

    // ── Ghost piece ───────────────────────────────────────────────────────

    /**
     * Devuelve una copia independiente de esta pieza.
     * GamePanel la baja hasta el fondo para dibujar el ghost.
     */
    public Tetromino copy() { return new Tetromino(this); }

    // ── Colores (resueltos desde el tema) ─────────────────────────────────

    public Color getColor(Theme theme)  { return theme.getPieceColor(type); }

    /** Color sólido de respaldo (sin tema). */
    public Color getColor() {
        Color[] fallback = {
                Color.CYAN, Color.YELLOW, Color.MAGENTA,
                Color.GREEN.brighter(), Color.RED.brighter(),
                Color.BLUE.brighter(), Color.ORANGE.brighter()
        };
        return fallback[type % 7];
    }

    // ── Movimiento ────────────────────────────────────────────────────────

    public void moveLeft()  { x--; }
    public void moveRight() { x++; }
    public void moveDown()  { y++; }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static int[][] copyShape(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++)
            copy[i] = src[i].clone();
        return copy;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public int     getType()               { return type;           }
    public int[][] getShape()              { return shape;          }
    public void    setShape(int[][] s)     { shape = copyShape(s);  }
    public int     getX()                  { return x;              }
    public int     getY()                  { return y;              }
    public void    setX(int x)             { this.x = x;            }
    public void    setY(int y)             { this.y = y;            }
}