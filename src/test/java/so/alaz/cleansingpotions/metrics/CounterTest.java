package so.alaz.cleansingpotions.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CounterTest {

    @Test
    void peekLeavesTheCountInPlace() {
        Counter counter = new Counter();
        counter.add(3);
        assertThat(counter.peek()).isEqualTo(3);
        assertThat(counter.peek()).isEqualTo(3);
    }

    @Test
    void commitKeepsIncrementsMadeAfterTheRead() {
        Counter counter = new Counter();
        counter.add(5);
        counter.peek();
        counter.inc();
        counter.inc();
        counter.commit();
        assertThat(counter.peek()).isEqualTo(2);
    }

    @Test
    void commitSubtractsOnlyTheLatestRead() {
        Counter counter = new Counter();
        counter.add(5);
        counter.peek();
        counter.add(5);
        counter.peek();
        counter.commit();
        assertThat(counter.peek()).isZero();
    }

    @Test
    void commitWithoutAReadSubtractsNothing() {
        Counter counter = new Counter();
        counter.add(4);
        counter.commit();
        assertThat(counter.peek()).isEqualTo(4);
    }

    @Test
    void repeatedCommitSubtractsOnce() {
        Counter counter = new Counter();
        counter.add(4);
        counter.peek();
        counter.commit();
        counter.add(1);
        counter.commit();
        assertThat(counter.peek()).isEqualTo(1);
    }
}
