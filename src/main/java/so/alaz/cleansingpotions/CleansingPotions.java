package so.alaz.cleansingpotions;

import net.minecraft.resources.Identifier;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.config.ConfigManager;
import so.alaz.cleansingpotions.effect.EffectClassifier;
import so.alaz.cleansingpotions.effect.EffectCleanser;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;
import so.alaz.cleansingpotions.metrics.FastStatsBridge;
import so.alaz.cleansingpotions.util.CooldownService;

public final class CleansingPotions {

    private static EffectCleanser cleanser;
    private static final CooldownService COOLDOWNS = new CooldownService();

    public static void init() {
        ConfigManager.load();
        cleanser = new EffectCleanser(new EffectClassifier());
        Constants.LOG.info("CleansingPotions initialized.");
    }

    /** Rereads the config file. Returns false if that failed, in which case the previous values stay. */
    public static boolean reload() {
        boolean loaded = ConfigManager.load();
        if (loaded) {
            CleansingMetrics.configReloaded();
            applyMetricsToggle();
        }
        return loaded;
    }

    /**
     * Applies a changed metrics toggle. Stopping is immediate; starting needs a game restart.
     * {@link FastStatsBridge} is reached only behind {@link CleansingMetrics#backendActive}, which makes
     * this safe when the nested FastStats mod is absent.
     */
    public static void applyMetricsToggle() {
        boolean wanted = CleansingConfig.get().metrics;
        if (!wanted && CleansingMetrics.backendActive()) {
            FastStatsBridge.stop();
            Constants.LOG.info("Metrics turned off: FastStats stopped and will send nothing further.");
        } else if (wanted && !CleansingMetrics.backendActive()) {
            Constants.LOG.info("Metrics are on but FastStats is not running; it can only start at game start.");
        }
    }

    public static CooldownService cooldowns() {
        return COOLDOWNS;
    }

    public static EffectCleanser cleanser() {
        return cleanser;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    private CleansingPotions() {
    }
}
