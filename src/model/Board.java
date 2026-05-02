package model;

import util.Constants;

/**
 * Tablero de juego.
 *
 * Mejoras vs versión original:
 *  - Usa Constants para WIDTH/HEIGHT (sin magic numbers).
 *  - Método ghostY() para calcular la posición del ghost piece.
 *  - clearLines() devuelve el conteo correcto sin bug de row++.
 */
public class Board {

    private final int WIDTH  = Constants.BOARD_COLS;
    private final int HEIGHT = Constants.BOARD_ROWS;

    private final int[][] grid = new int[HEIGHT][WIDTH];

    // ── Consultas ─────────────────────────────────────────────────────────

    public int getWidth()               { return WIDTH;           }
    public int getHeight()              { return HEIGHT;          }
    public int getCell(int row, int col){ return grid[row][col];  }

    public boolean isValidPosition(Tetromino piece) {
        int[][] shape = piece.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 0) continue;
                int nx = piece.getX() + j;
                int ny = piece.getY() + i;
                if (nx < 0 || nx >= WIDTH || ny >= HEIGHT) return false;
                if (ny >= 0 && grid[ny][nx] != 0)          return false;
            }
        }
        return true;
    }

    // ── Ghost piece ───────────────────────────────────────────────────────

    /**
     * Calcula la Y más baja donde caería la pieza actual sin modificarla.
     * GamePanel dibuja el ghost en esa posición.
     */
    public int ghostY(Tetromino piece) {
        Tetromino ghost = piece.copy();
        while (true) {
            ghost.setY(ghost.getY() + 1);
            if (!isValidPosition(ghost)) {
                ghost.setY(ghost.getY() - 1);
                return ghost.getY();
            }
        }
    }

    // ── Colocación ────────────────────────────────────────────────────────

    public void placePiece(Tetromino piece) {
        int[][] shape = piece.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int px = piece.getX() + j;
                    int py = piece.getY() + i;
                    if (py >= 0) grid[py][px] = piece.getType() + 1;
                }
            }
        }
    }

    // ── Limpieza de líneas ────────────────────────────────────────────────

    /**
     * Elimina las filas completas y baja el contenido superior.
     * @return número de líneas eliminadas (0-4).
     */
    public int clearLines() {
        int lines = 0;
        for (int row = HEIGHT - 1; row >= 0; row--) {
            if (isRowFull(row)) {
                removeRow(row);
                row++;   // revisitar la misma posición tras bajar todo
                lines++;
            }
        }
        return lines;
    }

    private boolean isRowFull(int row) {
        for (int col = 0; col < WIDTH; col++)
            if (grid[row][col] == 0) return false;
        return true;
    }

    private void removeRow(int row) {
        // Baja todas las filas superiores una posición
        for (int r = row; r > 0; r--)
            System.arraycopy(grid[r - 1], 0, grid[r], 0, WIDTH);
        // Limpia la fila superior
        for (int c = 0; c < WIDTH; c++) grid[0][c] = 0;
    }

    /** Resetea el tablero (nueva partida / vida perdida). */
    public void reset() {
        for (int r = 0; r < HEIGHT; r++)
            for (int c = 0; c < WIDTH; c++)
                grid[r][c] = 0;
    }
}