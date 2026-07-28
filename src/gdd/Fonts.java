package gdd;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

// Loads the project font (ThaleahFat) once and hands out sized derivatives.
public final class Fonts {

    private static Font base;
    private static final Map<Float, Font> CACHE = new HashMap<>();

    private Fonts() {
    }

    private static Font base() {
        if (base == null) {
            try {
                base = Font.createFont(Font.TRUETYPE_FONT, new File(Global.FONT_MAIN));
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
            } catch (Exception e) {
                System.err.println("Custom font unavailable, falling back: " + e.getMessage());
                base = new Font("SansSerif", Font.BOLD, 16);
            }
        }
        return base;
    }

    public static Font get(float size) {
        return CACHE.computeIfAbsent(size, s -> base().deriveFont(Font.PLAIN, s));
    }
}
