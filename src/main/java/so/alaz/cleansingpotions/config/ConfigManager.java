package so.alaz.cleansingpotions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;
import so.alaz.cleansingpotions.metrics.ErrorReporter;
import so.alaz.cleansingpotions.platform.Services;

import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public final class ConfigManager {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern METRICS_ON = Pattern.compile("\"metrics\"\\s*:\\s*true");

    private static boolean loadSucceeded;

    private static Path file() {
        return Services.PLATFORM.getConfigDir().resolve("cleansingpotions.json");
    }

    /**
     * Reads the config file into {@link CleansingConfig}. A missing file is created from the defaults.
     *
     * <p>A failed load leaves the file as it is and keeps the values in effect. Until a load has
     * succeeded those values are the defaults, except that metrics are on only if the file's text holds
     * {@code "metrics": true}. That text check applies only while FastStats is not running.
     *
     * @return false if the file could not be read or parsed
     */
    public static synchronized boolean load() {
        String text = null;
        try {
            Path target = file();
            if (Files.notExists(target)) {
                CleansingConfig.set(new CleansingConfig());
                save();
                loadSucceeded = true;
                return true;
            }
            // Decodes with replacement, so a file saved in another encoding still loads.
            text = new String(Files.readAllBytes(target), StandardCharsets.UTF_8);
            CleansingConfig cfg = GSON.fromJson(text, CleansingConfig.class);
            if (cfg == null) {
                throw new JsonParseException("The config file is empty");
            }
            cfg.sanitize();
            CleansingConfig.set(cfg);
            loadSucceeded = true;
            return true;
        } catch (Exception e) {
            // ErrorReporter consults this value, so it is set before the report.
            applyMetricsFromText(text);
            Constants.LOG.error("Failed to load CleansingPotions config", e);
            // On the first load this runs before the metrics backend exists, so ErrorReporter holds
            // the report.
            ErrorReporter.report("config:load", e);
            return false;
        }
    }

    /**
     * Sets metrics from the text of a file that failed to load, null if it could not be read. Does
     * nothing once a load has succeeded or while FastStats is running.
     */
    static void applyMetricsFromText(String text) {
        if (!loadSucceeded && !CleansingMetrics.backendActive()) {
            CleansingConfig.get().metrics = text != null && METRICS_ON.matcher(text).find();
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
