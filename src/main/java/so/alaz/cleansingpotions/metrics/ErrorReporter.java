package so.alaz.cleansingpotions.metrics;

import so.alaz.cleansingpotions.config.CleansingConfig;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Reports a recovered failure to FastStats.
 *
 * <p>Holds no {@code dev.faststats} reference of its own, so a call site stays safe when the nested
 * FastStats mod was rejected: the backend check short circuits before {@link FastStatsBridge} is
 * touched.
 *
 * <p>Report what a player would notice as degraded behaviour, such as a dropped write or an
 * integration that is installed but no longer fits. Control flow (a parse fallback, a probe for a
 * class that may be absent) does not belong here, because the noise buries what matters.
 */
public final class ErrorReporter {

    /**
     * Reports raised before the backend exists are held here. Config is read before the metrics
     * toggle is known, so the first config load runs ahead of the FastStats context, and a failure
     * there is the most useful error this mod can send.
     */
    private static final Deque<Pending> PENDING = new ArrayDeque<>();
    private static final int MAX_PENDING = 8;

    private record Pending(String source, Throwable error) {
    }

    public static void report(String source, Throwable error) {
        if (!CleansingMetrics.backendActive()) {
            if (CleansingConfig.get().metrics) {
                synchronized (PENDING) {
                    if (PENDING.size() < MAX_PENDING) {
                        PENDING.add(new Pending(source, error));
                    }
                }
            }
            return;
        }
        deliver(source, error);
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
