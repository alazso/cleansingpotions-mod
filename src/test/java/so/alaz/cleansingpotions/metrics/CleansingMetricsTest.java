package so.alaz.cleansingpotions.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import so.alaz.cleansingpotions.core.CleanseMode;

import static org.assertj.core.api.Assertions.assertThat;

class CleansingMetricsTest {

    @BeforeEach
    void drain() {
        CleansingMetrics.POTIONS_BREWED.peek();
        CleansingMetrics.brewsByRecipe();
        CleansingMetrics.removedPerCleanse();
        CleansingMetrics.commitAll();
    }

    @Test
    void snapshotOmitsCountersAtZero() {
        CleansingMetrics.potionBrewed("recipe_a");
        CleansingMetrics.brewsByRecipe();
        CleansingMetrics.commitAll();
        assertThat(CleansingMetrics.brewsByRecipe()).isEmpty();
    }

    @Test
    void commitKeepsIncrementsThatLandAfterTheSnapshot() {
        CleansingMetrics.potionBrewed("recipe_b");
        assertThat(CleansingMetrics.brewsByRecipe()).containsEntry("recipe_b", 1L);
        CleansingMetrics.POTIONS_BREWED.peek();
        CleansingMetrics.potionBrewed("recipe_b");
        CleansingMetrics.commitAll();
        assertThat(CleansingMetrics.brewsByRecipe()).containsEntry("recipe_b", 1L);
        assertThat(CleansingMetrics.POTIONS_BREWED.peek()).isEqualTo(1);
    }

    @Test
    void commitWithoutAReadKeepsTheCount() {
        CleansingMetrics.potionBrewed("recipe_c");
        CleansingMetrics.commitAll();
        assertThat(CleansingMetrics.brewsByRecipe()).containsEntry("recipe_c", 1L);
    }

    @Test
    void averageCommitsBothSumAndCount() {
        CleansingMetrics.cleanseCompleted(CleanseMode.ALL, 4);
        CleansingMetrics.cleanseCompleted(CleanseMode.ALL, 2);
        assertThat(CleansingMetrics.removedPerCleanse()).isEqualTo(3.0);
        CleansingMetrics.cleanseCompleted(CleanseMode.ALL, 7);
        CleansingMetrics.commitAll();
        assertThat(CleansingMetrics.removedPerCleanse()).isEqualTo(7.0);
    }
}
