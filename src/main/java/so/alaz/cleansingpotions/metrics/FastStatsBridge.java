package so.alaz.cleansingpotions.metrics;

import dev.faststats.Attributes;
import dev.faststats.ErrorTracker;
import dev.faststats.FastStatsContext;
import dev.faststats.Metrics;
import dev.faststats.SimpleContext;
import dev.faststats.data.Metric;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.config.CleansingConfig;

import java.util.function.Function;

public final class FastStatsBridge {

    static final String PROJECT_TOKEN = "321f21c8899fa729330e9508a4b7fe34";

    /** Two requests at the SDK's 3 second timeout. */
    private static final long FINAL_SEND_WAIT_MILLIS = 6_000L;

    private static volatile ErrorTracker errorTracker;
    private static volatile FastStatsContext context;

    private FastStatsBridge() {
    }

    // Each id must match a data source defined in the FastStats dashboard (docs/faststats-data-sources.md).
    public static Metrics registerMetrics(Metrics.Factory factory) {
        return factory
            .addMetric(Metric.number("potions_consumed", CleansingMetrics.POTIONS_CONSUMED::peek))
            .addMetric(Metric.number("potions_thrown", CleansingMetrics.POTIONS_THROWN::peek))
            .addMetric(Metric.number("cleanse_operations", CleansingMetrics.CLEANSE_OPERATIONS::peek))
            .addMetric(Metric.number("effects_removed", CleansingMetrics.EFFECTS_REMOVED::peek))
            .addMetric(Metric.number("lingering_clouds_spawned", CleansingMetrics.LINGERING_CLOUDS::peek))
            .addMetric(Metric.number("potions_brewed", CleansingMetrics.POTIONS_BREWED::peek))
            .addMetric(Metric.number("cooldown_blocks", CleansingMetrics.COOLDOWN_BLOCKS::peek))
            .addMetric(Metric.number("permission_denials", CleansingMetrics.PERMISSION_DENIALS::peek))
            .addMetric(Metric.number("commands_executed", CleansingMetrics.COMMANDS_EXECUTED::peek))
            .addMetric(Metric.number("config_reloads", CleansingMetrics.CONFIG_RELOADS::peek))
            .addMetric(Metric.numberMap("cleanses_by_mode", CleansingMetrics::cleansesByMode))
            .addMetric(Metric.numberMap("potions_used_by_variant", CleansingMetrics::potionsByVariant))
            .addMetric(Metric.numberMap("effects_removed_by_disposition", CleansingMetrics::removedByDisposition))
            .addMetric(Metric.numberMap("effects_removed_by_type", CleansingMetrics::removedByType))
            .addMetric(Metric.numberMap("brews_by_recipe", CleansingMetrics::brewsByRecipe))
            .addMetric(Metric.number("effects_removed_per_cleanse", CleansingMetrics::removedPerCleanse))
            .addMetric(Metric.number("entities_affected_per_throw", CleansingMetrics::entitiesPerThrow))
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
            // A counter supplier records what it read, and a flush subtracts only that.
            .onFlush(CleansingMetrics::commitAll)
            .create();
    }

    /** Creates the context. Called by the loader's holder class, inside the entrypoint's guard. */
    static void start(Function<ErrorTracker, FastStatsContext> factory) {
        ErrorTracker tracker = ErrorTracker.contextAware();
        try {
            // Every mod and Minecraft share one class loader, so the SDK's loader check alone also
            // captures another mod's thread death.
            tracker.addExclusionRule((thread, error) -> !ErrorReporter.hasOwnFrame(error));
            context = factory.apply(tracker);
        } catch (Throwable failure) {
            // ErrorTracker.contextAware installed the JVM default uncaught exception handler.
            tracker.detachErrorContext();
            throw failure;
        }
        errorTracker = tracker;
        CleansingMetrics.backendActive(true);
        ErrorReporter.flushPending();
    }

    /**
     * Ends all FastStats traffic for this process without a final send. A stopped context cannot
     * restart, because shutdown closes its executor, so turning metrics back on needs a game restart.
     * Callers run after the context is ready: a dedicated server starts it at SERVER_STARTED, before
     * any command can run. Thread-safe: the reload command and the config screen run on different
     * threads. Does not wait for a {@link #sendFinalBatch} still running: a request it already has in
     * flight finishes on its own thread, and it makes no further request.
     */
    public static synchronized void stop() {
        FastStatsContext current = context;
        if (current == null) {
            return;
        }
        context = null;
        CleansingMetrics.backendActive(false);
        // SimpleContext.shutdown sends one last batch unless this flag is already false.
        try {
            ((SimpleContext) current).submissionActive = false;
        } catch (LinkageError fieldGone) {
            // The field is undocumented, and another mod can nest a newer FastStats without it.
            Constants.LOG.warn("FastStats may send one last batch while stopping ({})", fieldGone.toString());
        }
        current.shutdown();
        errorTracker.detachErrorContext();
    }

    /**
     * Shuts the context down, which sends one last batch and ends its traffic, and holds the calling
     * thread for at most 6 seconds while that happens. The send runs on a daemon thread, so one still
     * running after the wait continues until it finishes or JVM exit abandons it. Holds no lock during
     * the send, so {@link #stop} does not wait for it. Does nothing after {@link #stop}, which has
     * already ended traffic without that batch, and repeating the call after a finished send sends
     * nothing.
     */
    public static void sendFinalBatch() {
        sendFinalBatch(FINAL_SEND_WAIT_MILLIS);
    }

    /**
     * {@link #sendFinalBatch()} with the wait in milliseconds. The wait must be above zero, because
     * {@link Thread#join(long)} waits without limit at zero. An interrupt ends the wait and stays set on
     * the calling thread.
     */
    static void sendFinalBatch(long waitMillis) {
        FastStatsContext current = context;
        if (current == null) {
            return;
        }
        Thread send = new Thread(current::shutdown, "cleansingpotions-faststats-final-send");
        send.setDaemon(true);
        send.start();
        try {
            send.join(waitMillis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
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
