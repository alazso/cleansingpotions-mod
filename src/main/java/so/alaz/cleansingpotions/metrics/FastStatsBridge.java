package so.alaz.cleansingpotions.metrics;

import dev.faststats.Attributes;
import dev.faststats.ErrorTracker;
import dev.faststats.FastStatsContext;
import dev.faststats.Metrics;
import dev.faststats.SimpleContext;
import dev.faststats.data.Metric;
import so.alaz.cleansingpotions.config.CleansingConfig;

import java.util.function.Function;

public final class FastStatsBridge {

    static final String PROJECT_TOKEN = "321f21c8899fa729330e9508a4b7fe34";

    private static volatile FastStatsContext context;

    private FastStatsBridge() {
    }

    // Each id must match a data source defined in the FastStats dashboard (docs/faststats-data-sources.md).
    public static Metrics registerMetrics(Metrics.Factory factory) {
        return factory
            .addMetric(Metric.number("potions_consumed", CleansingMetrics::peekPotionsConsumed))
            .addMetric(Metric.number("potions_thrown", CleansingMetrics::peekPotionsThrown))
            .addMetric(Metric.number("cleanse_operations", CleansingMetrics::peekCleanseOperations))
            .addMetric(Metric.number("effects_removed", CleansingMetrics::peekEffectsRemoved))
            .addMetric(Metric.number("lingering_clouds_spawned", CleansingMetrics::peekLingeringClouds))
            .addMetric(Metric.number("potions_brewed", CleansingMetrics::peekPotionsBrewed))
            .addMetric(Metric.number("cooldown_blocks", CleansingMetrics::peekCooldownBlocks))
            .addMetric(Metric.number("permission_denials", CleansingMetrics::peekPermissionDenials))
            .addMetric(Metric.number("commands_executed", CleansingMetrics::peekCommandsExecuted))
            .addMetric(Metric.number("config_reloads", CleansingMetrics::peekConfigReloads))
            .addMetric(Metric.numberMap("cleanses_by_mode", CleansingMetrics::peekCleansesByMode))
            .addMetric(Metric.numberMap("potions_used_by_variant", CleansingMetrics::peekPotionsByVariant))
            .addMetric(Metric.numberMap("effects_removed_by_disposition", CleansingMetrics::peekRemovedByDisposition))
            .addMetric(Metric.numberMap("effects_removed_by_type", CleansingMetrics::peekRemovedByType))
            .addMetric(Metric.numberMap("brews_by_recipe", CleansingMetrics::peekBrewsByRecipe))
            .addMetric(Metric.number("effects_removed_per_cleanse", CleansingMetrics::peekRemovedPerCleanse))
            .addMetric(Metric.number("entities_affected_per_throw", CleansingMetrics::peekEntitiesPerThrow))
            .addMetric(Metric.number("cleanse_radius", () -> CleansingConfig.get().radius))
            .addMetric(Metric.bool("cooldown_enabled", () -> CleansingConfig.get().cooldownEnabled))
            .addMetric(Metric.number("cooldown_seconds", () -> CleansingConfig.get().cooldownSeconds))
            .addMetric(Metric.bool("only_thrower", () -> CleansingConfig.get().onlyThrower))
            .addMetric(Metric.bool("brewing_enabled", () -> CleansingConfig.get().brewingEnabled))
            .addMetric(Metric.bool("splash_enabled", () -> CleansingConfig.get().splashEnabled))
            .addMetric(Metric.bool("lingering_enabled", () -> CleansingConfig.get().lingeringEnabled))
            .addMetric(Metric.bool("permissions_enabled", () -> CleansingConfig.get().permissionsEnabled))
            .addMetric(Metric.bool("cleanse_feedback", () -> CleansingConfig.get().cleanseFeedback))
            .addMetric(Metric.bool("impact_particles", () -> CleansingConfig.get().impactParticles))
            .addMetric(Metric.bool("check_for_updates", () -> CleansingConfig.get().checkForUpdates))
            .addMetric(Metric.stringArray("force_remove_effects",
                () -> CleansingConfig.get().forceRemove.toArray(new String[0])))
            .addMetric(Metric.stringArray("force_keep_effects",
                () -> CleansingConfig.get().forceKeep.toArray(new String[0])))
            // Suppliers above only read; counters reset once a submission is accepted.
            .onFlush(CleansingMetrics::resetAll)
            .create();
    }

    /** Creates the context. Called by the loader's holder class, inside the entrypoint's guard. */
    static void start(Function<ErrorTracker, FastStatsContext> factory) {
        ErrorTracker tracker = ErrorTracker.contextAware();
        try {
            context = factory.apply(tracker);
        } catch (Throwable failure) {
            // ErrorTracker.contextAware installed the JVM default uncaught exception handler.
            tracker.detachErrorContext();
            throw failure;
        }
        CleansingMetrics.backendActive(true);
        ErrorReporter.flushPending();
    }

    /**
     * Whether a tracked error will reach FastStats. False when no context exists, on the SDK's first
     * run, or when the owner opted out in config/faststats/config.properties. The SDK builds its error
     * service in all of those cases, so {@code errorTrackerService()} being present says nothing.
     */
    public static boolean errorsWillSend() {
        FastStatsContext current = context;
        return current != null && current.getConfig().errorTracking() && submitting(current);
    }

    private static boolean submitting(FastStatsContext current) {
        try {
            return ((SimpleContext) current).submissionActive;
        } catch (LinkageError fieldGone) {
            // The field is undocumented, and another mod can nest a newer FastStats without it.
            return true;
        }
    }

    /**
     * Reports an error this mod caught and recovered from. Requires "Error Tracking" enabled in the
     * FastStats project Features.
     *
     * <p>FastStats captures errors on its own only when one kills a thread, and the mod platforms
     * register nothing else, so anything handled in a catch block reaches the dashboard only through
     * here.
     *
     * <p>Must be called through {@link ErrorReporter#report}, which is safe when the nested FastStats
     * mod is absent.
     */
    public static void report(String source, Throwable error) {
        track(error, true, Attributes.empty().put("source", source));
    }

    public static void reportTestError(String source) {
        RuntimeException error = new RuntimeException(
            "CleansingPotions intentional test error (triggered by " + source + ")");
        track(error, false, Attributes.empty()
            .put("source", source)
            .put("intentional", true));
    }

    private static void track(Throwable error, boolean handled, Attributes attributes) {
        FastStatsContext current = context;
        if (current == null || !errorsWillSend()) {
            return;
        }
        current.errorTrackerService().ifPresent(service -> service.globalErrorTracker()
            .trackError(error)
            .handled(handled)
            .attributes(attributes));
    }
}
