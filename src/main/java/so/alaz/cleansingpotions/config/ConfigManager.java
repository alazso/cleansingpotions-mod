package so.alaz.cleansingpotions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.metrics.ErrorReporter;
import so.alaz.cleansingpotions.platform.Services;

import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return Services.PLATFORM.getConfigDir().resolve("cleansingpotions.json");
    }

    /**
     * Reads the config file into {@link CleansingConfig}. A missing file is created from the defaults.
     * A failed load leaves the file as it is and keeps the values in effect.
     *
     * @return false if the file could not be read or parsed
     */
    public static synchronized boolean load() {
        try {
            Path target = file();
            if (Files.notExists(target)) {
                CleansingConfig.set(new CleansingConfig());
                save();
                return true;
            }
            // Decodes with replacement: Files.readString throws on a file saved in another encoding.
            String text = new String(Files.readAllBytes(target), StandardCharsets.UTF_8);
            CleansingConfig cfg = GSON.fromJson(text, CleansingConfig.class);
            if (cfg == null) {
                throw new JsonParseException("The config file is empty");
            }
            cfg.sanitize();
            CleansingConfig.set(cfg);
            return true;
        } catch (Exception e) {
            Constants.LOG.error("Failed to load CleansingPotions config", e);
            // On the first load this runs before the metrics backend exists, so ErrorReporter holds
            // the report.
            ErrorReporter.report("config:load", e);
            return false;
        }
    }

    /** Sanitizes the values in effect and writes them. A failed write leaves the existing file intact. */
    public static synchronized void save() {
        try {
            CleansingConfig cfg = CleansingConfig.get();
            cfg.sanitize();
            String json = GSON.toJson(cfg);
            Path target = file();
            Files.createDirectories(target.getParent());
            Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
            Files.writeString(tmp, json);
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to save CleansingPotions config", e);
            ErrorReporter.report("config:save", e);
        }
    }

    private ConfigManager() {
    }
}
