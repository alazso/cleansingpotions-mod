package so.alaz.cleansingpotions.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.EffectDisposition;
import so.alaz.cleansingpotions.item.MilkVariant;

import static org.assertj.core.api.Assertions.assertThat;

class CleansingMetricsTest {

    @BeforeEach
    void drain() {
        CleansingMetrics.POTIONS_CONSUMED.peek();
        CleansingMetrics.POTIONS_THROWN.peek();
        CleansingMetrics.CLEANSE_OPERATIONS.peek();
        CleansingMetrics.EFFECTS_REMOVED.peek();
        CleansingMetrics.POTIONS_BREWED.peek();
        CleansingMetrics.cleansesByMode();
        CleansingMetrics.potionsByVariant();
        CleansingMetrics.removedByDisposition();
        CleansingMetrics.removedByType();
        CleansingMetrics.brewsByRecipe();
        CleansingMetrics.removedPerCleanse();
        CleansingMetrics.entitiesPerThrow();
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

    @Test
    void anAverageWithNoSamplesIsZero() {
        assertThat(CleansingMetrics.removedPerCleanse()).isZero();
        assertThat(CleansingMetrics.entitiesPerThrow()).isZero();
    }

    @Test
    void entitiesPerThrowAveragesEveryThrowIncludingMisses() {
        CleansingMetrics.throwAffected(3);
        CleansingMetrics.throwAffected(0);
        assertThat(CleansingMetrics.entitiesPerThrow()).isEqualTo(1.5);
    }

    @Test
    void committingTwiceSubtractsOnce() {
        CleansingMetrics.potionBrewed("recipe_d");
        CleansingMetrics.potionBrewed("recipe_d");
        CleansingMetrics.POTIONS_BREWED.peek();
        CleansingMetrics.brewsByRecipe();
        CleansingMetrics.commitAll();
        CleansingMetrics.potionBrewed("recipe_d");
        CleansingMetrics.commitAll();
        assertThat(CleansingMetrics.POTIONS_BREWED.peek()).isEqualTo(1);
        assertThat(CleansingMetrics.brewsByRecipe()).containsEntry("recipe_d", 1L);
    }

    @Test
    void potionCountersSplitByVariant() {
        CleansingMetrics.potionConsumed();
        CleansingMetrics.potionThrown(MilkVariant.SPLASH);
        CleansingMetrics.potionThrown(MilkVariant.SPLASH);
        CleansingMetrics.potionThrown(MilkVariant.LINGERING);
        assertThat(CleansingMetrics.POTIONS_CONSUMED.peek()).isEqualTo(1);
        assertThat(CleansingMetrics.POTIONS_THROWN.peek()).isEqualTo(3);
        assertThat(CleansingMetrics.potionsByVariant())
            .containsEntry("regular", 1L)
            .containsEntry("splash", 2L)
            .containsEntry("lingering", 1L);
    }

    @Test
    void removedEffectsCountByTypeAndDisposition() {
        CleansingMetrics.effectRemoved("MINECRAFT:Poison", EffectDisposition.HARMFUL);
        CleansingMetrics.effectRemoved("minecraft:speed", EffectDisposition.BENEFICIAL);
        CleansingMetrics.effectRemoved(" ", EffectDisposition.NEUTRAL);
        CleansingMetrics.effectRemoved(null, EffectDisposition.NEUTRAL);
        assertThat(CleansingMetrics.EFFECTS_REMOVED.peek()).isEqualTo(4);
        assertThat(CleansingMetrics.removedByType())
            .containsOnlyKeys("minecraft:poison", "minecraft:speed");
        assertThat(CleansingMetrics.removedByDisposition())
            .containsEntry("harmful", 1L)
            .containsEntry("beneficial", 1L)
            .containsEntry("neutral", 2L);
    }

    @Test
    void aCleanseThatRemovedNothingIsNotCounted() {
        CleansingMetrics.cleanseCompleted(CleanseMode.NEGATIVE_ONLY, 0);
        assertThat(CleansingMetrics.CLEANSE_OPERATIONS.peek()).isZero();
        assertThat(CleansingMetrics.cleansesByMode()).isEmpty();
        assertThat(CleansingMetrics.removedPerCleanse()).isZero();

        CleansingMetrics.cleanseCompleted(CleanseMode.NEGATIVE_ONLY, 2);
        assertThat(CleansingMetrics.CLEANSE_OPERATIONS.peek()).isEqualTo(1);
        assertThat(CleansingMetrics.cleansesByMode()).containsEntry("negative_only", 1L);
    }
}
