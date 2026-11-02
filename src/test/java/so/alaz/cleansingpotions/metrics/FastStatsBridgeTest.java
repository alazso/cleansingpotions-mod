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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FastStatsBridgeTest {

    private static final String NEOFORGE_HOLDER = "so/alaz/cleansingpotions/metrics/NeoForgeFastStats.class";
    private static final String OWN_CLASS = "so.alaz.cleansingpotions.metrics.ErrorReporter";
    // The SDK's loader check loads each frame's class, so this one must exist on the test classpath.
    private static final String OTHER_CLASS = "org.assertj.core.api.Assertions";
    private static final long SHORT_WAIT_MILLIS = 100;
    private static final long RETURN_LIMIT_MILLIS = 5_000;
    // Above RETURN_LIMIT_MILLIS, so a caller held for the whole stall fails its assertion.
    private static final long STALL_MILLIS = 15_000;

    /** A real SDK context with no services, so nothing can reach the network. */
    private static class RecordingContext extends SimpleContext {

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

    /** Its first shutdown stands in for a stalled send. A later shutdown returns at once, as the SDK's does. */
    private static final class StalledContext extends RecordingContext {

        final CompletableFuture<Thread> sender = new CompletableFuture<>();
        private final CountDownLatch release = new CountDownLatch(1);

        @Override
        public void shutdown() {
            if (sender.complete(Thread.currentThread())) {
                try {
                    release.await(STALL_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            super.shutdown();
        }

        /** Ends the stall and waits for the sending thread, if one started. */
        void finish() throws InterruptedException {
            release.countDown();
            Thread thread = sender.getNow(Thread.currentThread());
            if (thread != Thread.currentThread()) {
                thread.join(STALL_MILLIS);
            }
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
    void finalBatchHoldsTheCallerOnlyForTheWaitWhileTheSendStalls() throws Exception {
        StalledContext context = new StalledContext();
        FastStatsBridge.start(tracker -> context);
        try {
            long start = System.nanoTime();
            FastStatsBridge.sendFinalBatch(SHORT_WAIT_MILLIS);
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertThat(elapsedMillis).isLessThan(RETURN_LIMIT_MILLIS);
            Thread sender = context.sender.get(RETURN_LIMIT_MILLIS, TimeUnit.MILLISECONDS);
            assertThat(sender).isNotSameAs(Thread.currentThread());
            assertThat(sender.isDaemon()).isTrue();
            assertThat(sender.isAlive()).isTrue();
        } finally {
            context.finish();
        }
    }

    @Test
    void stopDoesNotWaitForAStalledFinalBatch() throws Exception {
        StalledContext context = new StalledContext();
        FastStatsBridge.start(tracker -> context);
        // The wait outlasts RETURN_LIMIT_MILLIS, so a class lock held across it fails the assertion.
        Thread caller = new Thread(() -> FastStatsBridge.sendFinalBatch(STALL_MILLIS));
        caller.setDaemon(true);
        try {
            caller.start();
            context.sender.get(RETURN_LIMIT_MILLIS, TimeUnit.MILLISECONDS);

            long start = System.nanoTime();
            FastStatsBridge.stop();
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertThat(elapsedMillis).isLessThan(RETURN_LIMIT_MILLIS);
        } finally {
            context.finish();
            caller.join(STALL_MILLIS);
        }
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
