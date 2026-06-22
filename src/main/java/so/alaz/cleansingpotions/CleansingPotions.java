package so.alaz.cleansingpotions;

import net.minecraft.resources.ResourceLocation;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.config.ConfigManager;
import so.alaz.cleansingpotions.effect.EffectClassifier;
import so.alaz.cleansingpotions.effect.EffectCleanser;
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

    public static void reload() {
        ConfigManager.load();
    }

    public static CooldownService cooldowns() {
        return COOLDOWNS;
    }

    public static EffectCleanser cleanser() {
        return cleanser;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    private CleansingPotions() {
    }
}
