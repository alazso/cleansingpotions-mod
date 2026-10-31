package so.alaz.cleansingpotions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.metrics.ErrorReporter;
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
            // Every setting silently falls back to its default, which an owner may not notice. On
            // the first load this runs before the metrics backend exists, so the report is held
            // until it does.
            Constants.LOG.error("Failed to load CleansingPotions config", e);
            ErrorReporter.report("config:load", e);
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
            // The edit the owner just made is gone.
            Constants.LOG.error("Failed to save CleansingPotions config", e);
            ErrorReporter.report("config:save", e);
        }
    }

    private ConfigManager() {
    }
}
