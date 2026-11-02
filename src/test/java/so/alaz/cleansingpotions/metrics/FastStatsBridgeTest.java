package so.alaz.cleansingpotions.metrics;

import dev.faststats.Config;
import dev.faststats.ErrorTracker;
import dev.faststats.Metrics;
import dev.faststats.SimpleContext;
import dev.faststats.internal.PlatformLoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FastStatsBridgeTest {

    private static final String NEOFORGE_HOLDER = "so/alaz/cleansingpotions/metrics/NeoForgeFastStats.class";
    private static final String OWN_CLASS = "so.alaz.cleansingpotions.metrics.ErrorReporter";
    // The SDK's loader check loads each frame's class, so this one must exist on the test classpath.
    private static final String OTHER_CLASS = "org.assertj.core.api.Assertions";

    /** A real SDK context with no services, so nothing can reach the network. */
    private static final class RecordingContext extends SimpleContext {

        final List<Boolean> submissionActiveAtShutdown = new ArrayList<>();

        RecordingContext() {
            super(null, new PlatformLoggerFactory((level, throwable, message) -> {
            }), new AllOnConfig(), "test", "0".repeat(32));
            submissionActive = true;
            ready();
        }

        @Override
        public void shutdown() {
            submissionActiveAtShutdown.add(submissionActive);
            super.shutdown();
        }

        @Override
        protected boolean preSubmissionStart() {
            return true;
        }

        @Override
        public String getProjectName() {
            return "test";
        }

        @Override
        protected Metrics.Factory metricsFactory() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    }

    private record AllOnConfig() implements Config {

        @Override
        public UUID serverId() {
            return new UUID(0, 0);
        }

        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public boolean submitMetrics() {
            return true;
        }

        @Override
        public boolean errorTracking() {
            return true;
        }

        @Override
        public boolean additionalMetrics() {
            return true;
        }

        @Override
        public boolean debug() {
            return false;
        }
    }

    private static RecordingContext started(Consumer<ErrorTracker> trackerSetup) {
        RecordingContext context = new RecordingContext();
        FastStatsBridge.start(tracker -> {
            trackerSetup.accept(tracker);
            return context;
        });
        return context;
    }

    @AfterEach
    void stopBridge() {
        FastStatsBridge.stop();
    }

    /** Hands the throwable to the JVM default handler, which is how a dying thread reaches the SDK. */
    private static List<Throwable> capturedOnThreadDeath(String frameClass) {
        List<Throwable> captured = new ArrayList<>();
        started(tracker -> tracker.setContextErrorHandler((loader, error) -> captured.add(error)));
        RuntimeException error = new RuntimeException("thread death");
        error.setStackTrace(new StackTraceElement[]{new StackTraceElement(frameClass, "run", "Source.java", 1)});
        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), error);
        return captured;
    }

    @Test
    void automaticCaptureKeepsAThreadDeathWithAFrameInThisMod() {
        assertThat(capturedOnThreadDeath(OWN_CLASS)).hasSize(1);
    }

    @Test
    void automaticCaptureDropsAThreadDeathWithNoFrameInThisMod() {
        assertThat(capturedOnThreadDeath(OTHER_CLASS)).isEmpty();
    }

    @Test
    void finalBatchShutsDownWithSubmissionStillActive() {
        RecordingContext context = started(tracker -> {
        });

        FastStatsBridge.sendFinalBatch();

        assertThat(context.submissionActiveAtShutdown).containsExactly(true);
    }

    @Test
    void finalBatchAfterStopDoesNotShutDownAgain() {
        RecordingContext context = started(tracker -> {
        });

        FastStatsBridge.stop();
        FastStatsBridge.sendFinalBatch();

        assertThat(context.submissionActiveAtShutdown).containsExactly(false);
    }

    @Test
    void repeatedFinalBatchFindsSubmissionOff() {
        RecordingContext context = started(tracker -> {
        });

        FastStatsBridge.sendFinalBatch();
        FastStatsBridge.sendFinalBatch();

        assertThat(context.submissionActiveAtShutdown).containsExactly(true, false);
    }

    @Test
    void finalBatchWithoutAContextDoesNothing() {
        FastStatsBridge.sendFinalBatch();
    }

    @Test
    void neoForgeHolderListensForGameShutdown() throws IOException {
        try (InputStream in = FastStatsBridgeTest.class.getClassLoader().getResourceAsStream(NEOFORGE_HOLDER)) {
            assumeTrue(in != null, "the NeoForge holder exists only on NeoForge nodes");
            // Class names sit in the constant pool as plain ASCII.
            assertThat(new String(in.readAllBytes(), StandardCharsets.ISO_8859_1))
                .contains("net/neoforged/neoforge/event/GameShuttingDownEvent");
        }
    }
}
