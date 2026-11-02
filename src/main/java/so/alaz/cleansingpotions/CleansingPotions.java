package so.alaz.cleansingpotions;

import net.minecraft.resources.Identifier;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.config.ConfigManager;
import so.alaz.cleansingpotions.effect.EffectClassifier;
import so.alaz.cleansingpotions.effect.EffectCleanser;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;
import so.alaz.cleansingpotions.util.CooldownService;

public final class CleansingPotions {

    private static EffectCleanser cleanser;
    private static final CooldownService COOLDOWNS = new CooldownService();

    public static void init() {
        ConfigManager.load();
        cleanser = new EffectCleanser(new EffectClassifier(),
            mode -> CleansingConfig.get().policy(mode));
        Constants.LOG.info("CleansingPotions initialized.");
    }

    /** Rereads the config file. Returns false if that failed, in which case the previous values stay. */
    public static boolean reload() {
        boolean loaded = ConfigManager.load();
        if (loaded) {
            CleansingMetrics.configReloaded();
        }
        return loaded;
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
