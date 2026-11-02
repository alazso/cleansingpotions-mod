package so.alaz.cleansingpotions.metrics;

import so.alaz.cleansingpotions.config.CleansingConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reports a recovered failure to FastStats, at most once per source per process.
 *
 * <p>Holds no {@code dev.faststats} reference of its own, so a call site stays safe when the nested
 * FastStats mod was rejected: the backend check short circuits before {@link FastStatsBridge} is
 * touched.
 *
 * <p>Callers report failures a player would notice as degraded behaviour.
 */
public final class ErrorReporter {

    /**
     * Reports raised before the backend exists are held here. Config is read before the metrics
     * toggle is known, so the first config load runs ahead of the FastStats context. The latch below
     * bounds this queue by the number of sources.
     */
    private static final Deque<Pending> PENDING = new ArrayDeque<>();

    /**
     * Sources that have already reported. The SDK stores each report that carries attributes as its own
     * entry, so an unlatched hot path would add one tracker entry per failure until a batch is accepted.
     */
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private record Pending(String source, Throwable error) {
    }

    public static void report(String source, Throwable error) {
        if (CleansingMetrics.backendActive()) {
            if (REPORTED.add(source)) {
                deliver(source, error);
            }
            return;
        }
        if (CleansingConfig.get().metrics && REPORTED.add(source)) {
            synchronized (PENDING) {
                PENDING.add(new Pending(source, error));
            }
        }
    }

    /** Called once the FastStats context exists, from {@link FastStatsBridge#start}. */
    static void flushPending() {
        Pending next;
        while (true) {
            synchronized (PENDING) {
                next = PENDING.poll();
            }
            if (next == null) {
                return;
            }
            deliver(next.source(), next.error());
        }
    }

    private static void deliver(String source, Throwable error) {
        try {
            FastStatsBridge.report(source, error);
        } catch (Throwable ignored) {
            // Reporting an error must never raise one.
        }
    }

    private ErrorReporter() {
    }
}
