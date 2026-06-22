package so.alaz.cleansingpotions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.platform.Services;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File file() {
        return new File(Services.PLATFORM.getConfigDir().toFile(), "cleansingpotions.json");
    }

    public static void load() {
        try {
            File f = file();
            if (!f.exists()) {
                save();
                return;
            }
            try (FileReader r = new FileReader(f)) {
                CleansingConfig cfg = GSON.fromJson(r, CleansingConfig.class);
                if (cfg == null) {
                    cfg = new CleansingConfig();
                }
                CleansingConfig.set(cfg);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to load CleansingPotions config", e);
        }
    }

    public static void save() {
        try {
            File f = file();
            f.getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(CleansingConfig.get(), w);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to save CleansingPotions config", e);
        }
    }

    private ConfigManager() {
    }
}
