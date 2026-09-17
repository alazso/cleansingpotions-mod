package so.alaz.cleansingpotions.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.EffectDisposition;
import so.alaz.cleansingpotions.item.MilkVariant;

// No dev.faststats references; FastStatsBridge wires the readers below into the SDK.
//
// Readers are side-effect free and report the total since the last successful metrics submission.
// FastStats reads its metric suppliers not only when it submits metrics but also when it assembles
// an error report, so a supplier that reset its counter on read would hand that count to the error
// batch and it would never reach the dashboard. Counters are cleared by resetAll instead, which
// FastStats runs as a flush callback only after a submission succeeds.
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

    private static final List<LongAdder> COUNTERS = List.of(
        POTIONS_CONSUMED, POTIONS_THROWN, CLEANSE_OPERATIONS, EFFECTS_REMOVED, LINGERING_CLOUDS,
        POTIONS_BREWED, COOLDOWN_BLOCKS, PERMISSION_DENIALS, COMMANDS_EXECUTED, CONFIG_RELOADS,
        REMOVED_PER_CLEANSE_SUM, REMOVED_PER_CLEANSE_COUNT, ENTITIES_PER_THROW_SUM,
        ENTITIES_PER_THROW_COUNT);

    private static final List<Map<String, LongAdder>> COUNTER_MAPS = List.of(
        CLEANSES_BY_MODE, POTIONS_BY_VARIANT, REMOVED_BY_DISPOSITION, REMOVED_BY_TYPE,
        BREWS_BY_RECIPE);

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

    public static long peekPotionsConsumed() {
        return POTIONS_CONSUMED.sum();
    }

    public static long peekPotionsThrown() {
        return POTIONS_THROWN.sum();
    }

    public static long peekCleanseOperations() {
        return CLEANSE_OPERATIONS.sum();
    }

    public static long peekEffectsRemoved() {
        return EFFECTS_REMOVED.sum();
    }

    public static long peekLingeringClouds() {
        return LINGERING_CLOUDS.sum();
    }

    public static long peekPotionsBrewed() {
        return POTIONS_BREWED.sum();
    }

    public static long peekCooldownBlocks() {
        return COOLDOWN_BLOCKS.sum();
    }

    public static long peekPermissionDenials() {
        return PERMISSION_DENIALS.sum();
    }

    public static long peekCommandsExecuted() {
        return COMMANDS_EXECUTED.sum();
    }

    public static long peekConfigReloads() {
        return CONFIG_RELOADS.sum();
    }

    public static Map<String, Number> peekCleansesByMode() {
        return snapshot(CLEANSES_BY_MODE);
    }

    public static Map<String, Number> peekPotionsByVariant() {
        return snapshot(POTIONS_BY_VARIANT);
    }

    public static Map<String, Number> peekRemovedByDisposition() {
        return snapshot(REMOVED_BY_DISPOSITION);
    }

    public static Map<String, Number> peekRemovedByType() {
        return snapshot(REMOVED_BY_TYPE);
    }

    public static Map<String, Number> peekBrewsByRecipe() {
        return snapshot(BREWS_BY_RECIPE);
    }

    public static double peekRemovedPerCleanse() {
        return average(REMOVED_PER_CLEANSE_SUM, REMOVED_PER_CLEANSE_COUNT);
    }

    public static double peekEntitiesPerThrow() {
        return average(ENTITIES_PER_THROW_SUM, ENTITIES_PER_THROW_COUNT);
    }

    /**
     * Clears every counter. Registered as the FastStats flush callback, which only runs once a
     * metrics submission has been accepted, so a failed submission keeps its counts for the next.
     * Map entries are zeroed rather than removed, so an increment racing the reset cannot land on a
     * detached counter.
     */
    public static void resetAll() {
        COUNTERS.forEach(LongAdder::reset);
        COUNTER_MAPS.forEach(map -> map.values().forEach(LongAdder::reset));
    }

    private static void bump(Map<String, LongAdder> map, String key) {
        map.computeIfAbsent(key, k -> new LongAdder()).increment();
    }

    private static Map<String, Number> snapshot(Map<String, LongAdder> map) {
        Map<String, Number> out = new HashMap<>();
        for (Map.Entry<String, LongAdder> entry : map.entrySet()) {
            long value = entry.getValue().sum();
            if (value != 0L) {
                out.put(entry.getKey(), value);
            }
        }
        return out;
    }

    private static double average(LongAdder sum, LongAdder count) {
        long samples = count.sum();
        return samples == 0L ? 0.0 : (double) sum.sum() / (double) samples;
    }
}
