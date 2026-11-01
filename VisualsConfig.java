package pl.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class VisualsConfig {
    public static final int[] COLORS = {
            0xFFFF3030, 0xFF30FF30, 0xFF3060FF, 0xFFFFFF30,
            0xFF30FFFF, 0xFFFF30FF, 0xFFFFFFFF, 0xFFFF9020
    };
    public static final String[] COLOR_NAMES = {
            "Czerwony", "Zielony", "Niebieski", "Żółty",
            "Cyjan", "Różowy", "Biały", "Pomarańczowy"
    };

    public enum Visual {
        BOX("Obramowanie (box)", 0),
        GLOW("Poświata (glow)", 4),
        RAINBOW("Tęczowy box", -1),
        RING("Pierścień pod nogami", 1),
        TRACER("Linia do celu (tracer)", 3),
        MARKER("Znacznik nad głową", 5),
        HEAD("Box na głowie", 7);

        public final String label;
        public boolean enabled = false;
        public int colorIndex;
        public final boolean rainbow;

        Visual(String label, int colorIndex) {
            this.label = label;
            this.rainbow = colorIndex < 0;
            this.colorIndex = Math.max(colorIndex, 0);
        }

        public int color() {
            return COLORS[colorIndex];
        }

        public void nextColor() {
            colorIndex = (colorIndex + 1) % COLORS.length;
        }
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("visualsmod.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            Properties p = new Properties();
            p.load(r);
            for (Visual v : Visual.values()) {
                v.enabled = Boolean.parseBoolean(p.getProperty(v.name() + ".enabled", "false"));
                try {
                    int c = Integer.parseInt(p.getProperty(v.name() + ".color", String.valueOf(v.colorIndex)));
                    v.colorIndex = Math.floorMod(c, COLORS.length);
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Visual v : Visual.values()) {
            p.setProperty(v.name() + ".enabled", String.valueOf(v.enabled));
            p.setProperty(v.name() + ".color", String.valueOf(v.colorIndex));
        }
        try (Writer w = Files.newBufferedWriter(file())) {
            p.store(w, "Visuals Mod");
        } catch (IOException ignored) {
        }
    }
}
