package so.alaz.cleansingpotions.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * An event count that survives a metrics submission. {@link #commit} subtracts what the latest
 * {@link #peek} returned, so increments made after that read stay for the next submission.
 *
 * <p>{@code inc} and {@code add} are safe from any thread. FastStats runs {@code peek} and {@code commit}
 * on the single submitter thread of its context, apart from the final send at shutdown.
 */
final class Counter {

    private final LongAdder adder = new LongAdder();
    private volatile long lastRead;

    void inc() {
        adder.increment();
    }

    void add(long amount) {
        adder.add(amount);
    }

    long peek() {
        long value = adder.sum();
        lastRead = value;
        return value;
    }

    void commit() {
        adder.add(-lastRead);
        lastRead = 0L;
    }
}
