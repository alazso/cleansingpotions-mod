package so.alaz.cleansingpotions.metrics;

//? neoforge {
/*import dev.faststats.neoforge.NeoForgeContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import so.alaz.cleansingpotions.Constants;

// Creates the NeoForge FastStats context. This class holds every dev.faststats type the entrypoint
// needs, so the entrypoint links when the nested FastStats mod was rejected. The JVM verifies this
// class on the first call to start, so callers must call it directly inside their guard: a method
// reference would link outside the try.
public final class NeoForgeFastStats {

    public static void start() {
        FastStatsBridge.start(tracker -> new NeoForgeContext.Factory(Constants.MOD_ID, FastStatsBridge.PROJECT_TOKEN)
            .metrics(FastStatsBridge::registerMetrics)
            .errorTrackerService(tracker)
            .create());
        // NeoForgeContext shuts itself down only on a dedicated server, at ServerStoppingEvent, so a client
        // has no other final send. A repeated shutdown sends nothing.
        NeoForge.EVENT_BUS.addListener((GameShuttingDownEvent event) -> FastStatsBridge.sendFinalBatch());
    }

    private NeoForgeFastStats() {
    }
}
*///?}
