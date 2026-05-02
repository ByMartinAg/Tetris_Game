package gameConfig;

import java.awt.Color;

/**
 * Cada tema define:
 *  - label          : nombre visible en menú
 *  - bgImage        : nombre del archivo de imagen de fondo (en images/)
 *  - musicFile      : archivo de música de juego (en sounds/)
 *  - pieceColors    : 7 colores para los 7 tipos de Tetromino (I O T S Z J L)
 *  - gridColor      : color de la cuadrícula
 *  - bgFallback     : color sólido si la imagen no carga
 */
public enum Theme {

    CLASICO("Clásico",
            "bg_clasico.png", "game_music.wav",
            new Color[]{
                    Color.CYAN, Color.YELLOW, Color.MAGENTA,
                    Color.GREEN.brighter(), Color.RED.brighter(),
                    Color.BLUE.brighter(), Color.ORANGE.brighter()
            },
            Color.DARK_GRAY, new Color(10, 10, 30)),

    ESPACIO("Espacio",
            "bg_espacio.png", "game_space.wav",
            new Color[]{
                    new Color(0, 255, 255),   // I - cian eléctrico
                    new Color(180, 0, 255),   // O - violeta
                    new Color(0, 180, 255),   // T - azul eléctrico
                    new Color(0, 255, 120),   // S - verde neón
                    new Color(255, 60, 60),   // Z - rojo neón
                    new Color(60, 60, 255),   // J - azul brillante
                    new Color(255, 160, 0)    // L - naranja
            },
            new Color(30, 30, 60), new Color(5, 5, 20)),

    NATURALEZA("Naturaleza",
            "bg_naturaleza.png", "game_nature.wav",
            new Color[]{
                    new Color(0, 200, 200),
                    new Color(220, 200, 0),
                    new Color(180, 80, 180),
                    new Color(50, 180, 50),
                    new Color(200, 60, 60),
                    new Color(30, 100, 200),
                    new Color(220, 130, 0)
            },
            new Color(60, 90, 40), new Color(20, 40, 10)),

    NEON("Neón",
            "bg_neon.png", "game_neon.wav",
            new Color[]{
                    new Color(0, 255, 255),
                    new Color(255, 255, 0),
                    new Color(255, 0, 255),
                    new Color(0, 255, 0),
                    new Color(255, 0, 0),
                    new Color(0, 100, 255),
                    new Color(255, 128, 0)
            },
            new Color(80, 0, 80), new Color(15, 0, 15)),

    RETRO("Retro 8-bit",
            "bg_retro.png", "game_retro.wav",
            new Color[]{
                    new Color(92, 230, 207),
                    new Color(247, 217, 91),
                    new Color(188, 100, 188),
                    new Color(88, 192, 88),
                    new Color(215, 75, 75),
                    new Color(75, 100, 215),
                    new Color(220, 150, 60)
            },
            new Color(60, 60, 80), new Color(20, 20, 35));

    // -------------------------------------------------------

    private final String  label;
    private final String  bgImage;
    private final String  musicFile;
    private final Color[] pieceColors;
    private final Color   gridColor;
    private final Color   bgFallback;

    Theme(String label, String bgImage, String musicFile,
          Color[] pieceColors, Color gridColor, Color bgFallback) {
        this.label       = label;
        this.bgImage     = bgImage;
        this.musicFile   = musicFile;
        this.pieceColors = pieceColors;
        this.gridColor   = gridColor;
        this.bgFallback  = bgFallback;
    }

    public String  getLabel()                 { return label;            }
    public String  getBgImage()               { return bgImage;          }
    public String  getMusicFile()             { return musicFile;        }
    public Color[] getPieceColors()           { return pieceColors;      }
    public Color   getPieceColor(int type)    { return pieceColors[type % 7]; }
    public Color   getGridColor()             { return gridColor;        }
    public Color   getBgFallback()            { return bgFallback;       }

    @Override
    public String toString() { return label; }
}