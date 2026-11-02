package so.alaz.cleansingpotions.metrics;

//? fabric {
import dev.faststats.fabric.FabricContext;
import so.alaz.cleansingpotions.Constants;

// Creates the Fabric FastStats context. This class holds every dev.faststats type the entrypoint
// needs, so the entrypoint links when the nested FastStats mod was rejected. The JVM verifies this
// class on the first call to start, so callers must call it directly inside their guard: a method
// reference would link outside the try.
public final class FabricFastStats {

    public static void start() {
        FastStatsBridge.start(tracker -> new FabricContext.Factory(Constants.MOD_ID, FastStatsBridge.PROJECT_TOKEN)
            .requireConsent()
            .metrics(FastStatsBridge::registerMetrics)
            .errorTrackerService(tracker)
            .create());
    }

    private FabricFastStats() {
    }
}
//?}
