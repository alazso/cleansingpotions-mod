package so.alaz.cleansingpotions.metrics;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.EffectDisposition;
import so.alaz.cleansingpotions.item.MilkVariant;

// No dev.faststats references, so this compiles on the 1.21.x nodes too. Counters reset on read
// (sumThenReset), so each FastStats flush reports the delta since the previous one.
public final class CleansingMetrics {

    private static final LongAdder POTIONS_CONSUMED = new LongAdder();
    private static final LongAdder POTIONS_THROWN = new LongAdder();
    private static final LongAdder CLEANSE_OPERATIONS = new LongAdder();
    private static final LongAdder EFFECTS_REMOVED = new LongAdder();
    private static final LongAdder LINGERING_CLOUDS = new LongAdder();
    private static final LongAdder POTIONS_BREWED = new LongAdder();
    private static final LongAdder COOLDOWN_BLOCKS = new LongAdder();
    private static final LongAdder PERMISSION_DENIALS = new LongAdder();
    private static final LongAdder COMMANDS_EXECUTED = new LongAdder();
    private static final LongAdder CONFIG_RELOADS = new LongAdder();

    private static final Map<String, LongAdder> CLEANSES_BY_MODE = new ConcurrentHashMap<>();
    private static final Map<String, LongAdder> POTIONS_BY_VARIANT = new ConcurrentHashMap<>();
    private static final Map<String, LongAdder> REMOVED_BY_DISPOSITION = new ConcurrentHashMap<>();
    private static final Map<String, LongAdder> REMOVED_BY_TYPE = new ConcurrentHashMap<>();
    private static final Map<String, LongAdder> BREWS_BY_RECIPE = new ConcurrentHashMap<>();

    private static final LongAdder REMOVED_PER_CLEANSE_SUM = new LongAdder();
    private static final LongAdder REMOVED_PER_CLEANSE_COUNT = new LongAdder();
    private static final LongAdder ENTITIES_PER_THROW_SUM = new LongAdder();
    private static final LongAdder ENTITIES_PER_THROW_COUNT = new LongAdder();

    private CleansingMetrics() {
    }

    public static void potionConsumed() {
        POTIONS_CONSUMED.increment();
        bump(POTIONS_BY_VARIANT, MilkVariant.REGULAR.tag());
    }

    public static void potionThrown(MilkVariant variant) {
        POTIONS_THROWN.increment();
        bump(POTIONS_BY_VARIANT, variant.tag());
    }

    public static void effectRemoved(String effectId, EffectDisposition disposition) {
        EFFECTS_REMOVED.increment();
        if (effectId != null && !effectId.isBlank()) {
            bump(REMOVED_BY_TYPE, effectId.toLowerCase(Locale.ROOT));
        }
        bump(REMOVED_BY_DISPOSITION, disposition.name().toLowerCase(Locale.ROOT));
    }

    public static void cleanseCompleted(CleanseMode mode, int removed) {
        if (removed <= 0) {
            return;
        }
        CLEANSE_OPERATIONS.increment();
        bump(CLEANSES_BY_MODE, mode.tag());
        REMOVED_PER_CLEANSE_SUM.add(removed);
        REMOVED_PER_CLEANSE_COUNT.increment();
    }

    public static void throwAffected(int affected) {
        ENTITIES_PER_THROW_SUM.add(affected);
        ENTITIES_PER_THROW_COUNT.increment();
    }

    public static void lingeringCloudSpawned() {
        LINGERING_CLOUDS.increment();
    }

    public static void potionBrewed(String recipeKey) {
        POTIONS_BREWED.increment();
        bump(BREWS_BY_RECIPE, recipeKey);
    }

    public static void cooldownBlocked() {
        COOLDOWN_BLOCKS.increment();
    }

    public static void permissionDenied() {
        PERMISSION_DENIALS.increment();
    }

    public static void commandExecuted() {
        COMMANDS_EXECUTED.increment();
    }

    public static void configReloaded() {
        CONFIG_RELOADS.increment();
    }

    public static long takePotionsConsumed() {
        return POTIONS_CONSUMED.sumThenReset();
    }

    public static long takePotionsThrown() {
        return POTIONS_THROWN.sumThenReset();
    }

    public static long takeCleanseOperations() {
        return CLEANSE_OPERATIONS.sumThenReset();
    }

    public static long takeEffectsRemoved() {
        return EFFECTS_REMOVED.sumThenReset();
    }

    public static long takeLingeringClouds() {
        return LINGERING_CLOUDS.sumThenReset();
    }

    public static long takePotionsBrewed() {
        return POTIONS_BREWED.sumThenReset();
    }

    public static long takeCooldownBlocks() {
        return COOLDOWN_BLOCKS.sumThenReset();
    }

    public static long takePermissionDenials() {
        return PERMISSION_DENIALS.sumThenReset();
    }

    public static long takeCommandsExecuted() {
        return COMMANDS_EXECUTED.sumThenReset();
    }

    public static long takeConfigReloads() {
        return CONFIG_RELOADS.sumThenReset();
    }

    public static Map<String, Number> takeCleansesByMode() {
        return drain(CLEANSES_BY_MODE);
    }

    public static Map<String, Number> takePotionsByVariant() {
        return drain(POTIONS_BY_VARIANT);
    }

    public static Map<String, Number> takeRemovedByDisposition() {
        return drain(REMOVED_BY_DISPOSITION);
    }

    public static Map<String, Number> takeRemovedByType() {
        return drain(REMOVED_BY_TYPE);
    }

    public static Map<String, Number> takeBrewsByRecipe() {
        return drain(BREWS_BY_RECIPE);
    }

    public static double takeRemovedPerCleanse() {
        return average(REMOVED_PER_CLEANSE_SUM, REMOVED_PER_CLEANSE_COUNT);
    }

    public static double takeEntitiesPerThrow() {
        return average(ENTITIES_PER_THROW_SUM, ENTITIES_PER_THROW_COUNT);
    }

    private static void bump(Map<String, LongAdder> map, String key) {
        map.computeIfAbsent(key, k -> new LongAdder()).increment();
    }

    private static Map<String, Number> drain(Map<String, LongAdder> map) {
        Map<String, Number> out = new HashMap<>();
        for (Map.Entry<String, LongAdder> entry : map.entrySet()) {
            long value = entry.getValue().sumThenReset();
            if (value != 0L) {
                out.put(entry.getKey(), value);
            }
        }
        return out;
    }

    private static double average(LongAdder sum, LongAdder count) {
        long samples = count.sumThenReset();
        long total = sum.sumThenReset();
        return samples == 0L ? 0.0 : (double) total / (double) samples;
    }
}
