package gameConfig;

import util.Constants;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/**
 * Singleton que carga y cachea las imágenes de fondo de cada tema.
 * Si la imagen no existe, getBackground() devuelve null y GamePanel
 * pinta el bgFallback del tema como color sólido.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private final Map<Theme, BufferedImage> cache = new EnumMap<>(Theme.class);

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    /**
     * Devuelve la imagen de fondo del tema, cargándola la primera vez.
     * @return BufferedImage o null si el archivo no existe.
     */
    public BufferedImage getBackground(Theme theme) {
        if (cache.containsKey(theme)) return cache.get(theme);

        BufferedImage img = null;
        try {
            File f = new File(Constants.IMAGES_DIR + theme.getBgImage());
            if (f.exists()) img = ImageIO.read(f);
        } catch (Exception e) {
            System.err.println("ThemeManager: no se pudo cargar " + theme.getBgImage());
        }
        cache.put(theme, img);   // null también se cachea para no reintentar
        return img;
    }

    /** Libera el caché (útil si el usuario cambia recursos en caliente). */
    public void clearCache() { cache.clear(); }
}