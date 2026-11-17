package so.alaz.cleansingpotions.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.EffectDisposition;
import so.alaz.cleansingpotions.item.MilkVariant;

// Holds no dev.faststats reference. FastStats also runs metric suppliers when it assembles an error
// report, so reading a counter must not clear it. Only commitAll lowers a count.
public final class CleansingMetrics {

    static final Counter POTIONS_CONSUMED = new Counter();
    static final Counter POTIONS_THROWN = new Counter();
    static final Counter CLEANSE_OPERATIONS = new Counter();
    static final Counter EFFECTS_REMOVED = new Counter();
    static final Counter LINGERING_CLOUDS = new Counter();
    static final Counter POTIONS_BREWED = new Counter();
    static final Counter COOLDOWN_BLOCKS = new Counter();
    static final Counter PERMISSION_DENIALS = new Counter();
    static final Counter COMMANDS_EXECUTED = new Counter();
    static final Counter CONFIG_RELOADS = new Counter();

    private static final Map<String, Counter> CLEANSES_BY_MODE = new ConcurrentHashMap<>();
    private static final Map<String, Counter> POTIONS_BY_VARIANT = new ConcurrentHashMap<>();
    private static final Map<String, Counter> REMOVED_BY_DISPOSITION = new ConcurrentHashMap<>();
    private static final Map<String, Counter> REMOVED_BY_TYPE = new ConcurrentHashMap<>();
    private static final Map<String, Counter> BREWS_BY_RECIPE = new ConcurrentHashMap<>();

    private static final Counter REMOVED_PER_CLEANSE_SUM = new Counter();
    private static final Counter REMOVED_PER_CLEANSE_COUNT = new Counter();
    private static final Counter ENTITIES_PER_THROW_SUM = new Counter();
    private static final Counter ENTITIES_PER_THROW_COUNT = new Counter();

    private static final List<Counter> COUNTERS = List.of(
        POTIONS_CONSUMED, POTIONS_THROWN, CLEANSE_OPERATIONS, EFFECTS_REMOVED, LINGERING_CLOUDS,
        POTIONS_BREWED, COOLDOWN_BLOCKS, PERMISSION_DENIALS, COMMANDS_EXECUTED, CONFIG_RELOADS,
        REMOVED_PER_CLEANSE_SUM, REMOVED_PER_CLEANSE_COUNT, ENTITIES_PER_THROW_SUM,
        ENTITIES_PER_THROW_COUNT);

    private static final List<Map<String, Counter>> COUNTER_MAPS = List.of(
        CLEANSES_BY_MODE, POTIONS_BY_VARIANT, REMOVED_BY_DISPOSITION, REMOVED_BY_TYPE,
        BREWS_BY_RECIPE);

    private static volatile boolean backendActive;

    private CleansingMetrics() {
    }

    /**
     * Whether a FastStats context exists. Resolves no {@code dev.faststats} type, so it is safe to call
     * when the nested FastStats mod was rejected.
     */
    public static boolean backendActive() {
        return backendActive;
    }

    static void backendActive(boolean active) {
        backendActive = active;
    }

    public static void potionConsumed() {
        POTIONS_CONSUMED.inc();
        bump(POTIONS_BY_VARIANT, MilkVariant.REGULAR.tag());
    }

    public static void potionThrown(MilkVariant variant) {
        POTIONS_THROWN.inc();
        bump(POTIONS_BY_VARIANT, variant.tag());
    }

    public static void effectRemoved(String effectId, EffectDisposition disposition) {
        EFFECTS_REMOVED.inc();
        bump(REMOVED_BY_TYPE, effectId.toLowerCase(Locale.ROOT));
        bump(REMOVED_BY_DISPOSITION, disposition.name().toLowerCase(Locale.ROOT));
    }

    public static void cleanseCompleted(CleanseMode mode, int removed) {
        if (removed <= 0) {
            return;
        }
        CLEANSE_OPERATIONS.inc();
        bump(CLEANSES_BY_MODE, mode.tag());
        REMOVED_PER_CLEANSE_SUM.add(removed);
        REMOVED_PER_CLEANSE_COUNT.inc();
    }

    public static void throwAffected(int affected) {
        ENTITIES_PER_THROW_SUM.add(affected);
        ENTITIES_PER_THROW_COUNT.inc();
    }

    public static void lingeringCloudSpawned() {
        LINGERING_CLOUDS.inc();
    }

    public static void potionBrewed(String recipeKey) {
        POTIONS_BREWED.inc();
        bump(BREWS_BY_RECIPE, recipeKey);
    }

    public static void cooldownBlocked() {
        COOLDOWN_BLOCKS.inc();
    }

    public static void permissionDenied() {
        PERMISSION_DENIALS.inc();
    }

    public static void commandExecuted() {
        COMMANDS_EXECUTED.inc();
    }

    public static void configReloaded() {
        CONFIG_RELOADS.inc();
    }

    static Map<String, Number> cleansesByMode() {
        return snapshot(CLEANSES_BY_MODE);
    }

    static Map<String, Number> potionsByVariant() {
        return snapshot(POTIONS_BY_VARIANT);
    }

    static Map<String, Number> removedByDisposition() {
        return snapshot(REMOVED_BY_DISPOSITION);
    }

    static Map<String, Number> removedByType() {
        return snapshot(REMOVED_BY_TYPE);
    }

    static Map<String, Number> brewsByRecipe() {
        return snapshot(BREWS_BY_RECIPE);
    }

    static double removedPerCleanse() {
        return average(REMOVED_PER_CLEANSE_SUM, REMOVED_PER_CLEANSE_COUNT);
    }

    static double entitiesPerThrow() {
        return average(ENTITIES_PER_THROW_SUM, ENTITIES_PER_THROW_COUNT);
    }

    /**
     * Subtracts from every counter what its latest read reported. Registered as the FastStats flush
     * callback, which runs only once a metrics submission has been accepted, so a failed submission keeps
     * its counts for the next. Map entries stay in place, so an increment racing the commit cannot land
     * on a detached counter.
     */
    static void commitAll() {
        COUNTERS.forEach(Counter::commit);
        COUNTER_MAPS.forEach(map -> map.values().forEach(Counter::commit));
    }

    private static void bump(Map<String, Counter> map, String key) {
        map.computeIfAbsent(key, k -> new Counter()).inc();
    }

    private static Map<String, Number> snapshot(Map<String, Counter> map) {
        Map<String, Number> out = new HashMap<>();
        for (Map.Entry<String, Counter> entry : map.entrySet()) {
            long value = entry.getValue().peek();
            if (value != 0L) {
                out.put(entry.getKey(), value);
            }
        }
        return out;
    }

    private static double average(Counter sum, Counter count) {
        long total = sum.peek();
        long samples = count.peek();
        return samples == 0L ? 0.0 : (double) total / (double) samples;
    }
}
