package so.alaz.cleansingpotions.metrics;

//? if >=26.1.2 {
/*import dev.faststats.Attributes;
import dev.faststats.ErrorTracker;
import dev.faststats.Metrics;
import dev.faststats.data.Metric;
import so.alaz.cleansingpotions.config.CleansingConfig;

public final class FastStatsBridge {

    private static volatile ErrorTracker errorTracker;

    private FastStatsBridge() {
    }

    // Each id must match a data source defined in the FastStats dashboard (docs/faststats-data-sources.md).
    public static Metrics registerMetrics(Metrics.Factory factory) {
        return factory
            .addMetric(Metric.number("potions_consumed", CleansingMetrics::takePotionsConsumed))
            .addMetric(Metric.number("potions_thrown", CleansingMetrics::takePotionsThrown))
            .addMetric(Metric.number("cleanse_operations", CleansingMetrics::takeCleanseOperations))
            .addMetric(Metric.number("effects_removed", CleansingMetrics::takeEffectsRemoved))
            .addMetric(Metric.number("lingering_clouds_spawned", CleansingMetrics::takeLingeringClouds))
            .addMetric(Metric.number("potions_brewed", CleansingMetrics::takePotionsBrewed))
            .addMetric(Metric.number("cooldown_blocks", CleansingMetrics::takeCooldownBlocks))
            .addMetric(Metric.number("permission_denials", CleansingMetrics::takePermissionDenials))
            .addMetric(Metric.number("commands_executed", CleansingMetrics::takeCommandsExecuted))
            .addMetric(Metric.number("config_reloads", CleansingMetrics::takeConfigReloads))
            .addMetric(Metric.numberMap("cleanses_by_mode", CleansingMetrics::takeCleansesByMode))
            .addMetric(Metric.numberMap("potions_used_by_variant", CleansingMetrics::takePotionsByVariant))
            .addMetric(Metric.numberMap("effects_removed_by_disposition", CleansingMetrics::takeRemovedByDisposition))
            .addMetric(Metric.numberMap("effects_removed_by_type", CleansingMetrics::takeRemovedByType))
            .addMetric(Metric.numberMap("brews_by_recipe", CleansingMetrics::takeBrewsByRecipe))
            .addMetric(Metric.number("effects_removed_per_cleanse", CleansingMetrics::takeRemovedPerCleanse))
            .addMetric(Metric.number("entities_affected_per_throw", CleansingMetrics::takeEntitiesPerThrow))
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
            .create();
    }

    // Requires "Error Tracking" enabled in the FastStats project Features.
    public static ErrorTracker errorTracker() {
        ErrorTracker tracker = errorTracker;
        if (tracker == null) {
            tracker = ErrorTracker.contextAware();
            errorTracker = tracker;
        }
        return tracker;
    }

    public static void reportTestError(String source) {
        RuntimeException error = new RuntimeException(
            "CleansingPotions intentional test error (triggered by " + source + ")");
        errorTracker()
            .trackError(error)
            .handled(false)
            .attributes(Attributes.empty()
                .put("source", source)
                .put("intentional", true));
    }
}
*///?}
