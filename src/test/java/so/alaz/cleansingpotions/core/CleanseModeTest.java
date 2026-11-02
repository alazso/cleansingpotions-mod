package so.alaz.cleansingpotions.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static so.alaz.cleansingpotions.core.EffectDisposition.BENEFICIAL;
import static so.alaz.cleansingpotions.core.EffectDisposition.HARMFUL;
import static so.alaz.cleansingpotions.core.EffectDisposition.NEUTRAL;

class CleanseModeTest {

    @Test
    void allRemovesEveryDisposition() {
        assertThat(CleanseMode.ALL.removes(BENEFICIAL)).isTrue();
        assertThat(CleanseMode.ALL.removes(HARMFUL)).isTrue();
        assertThat(CleanseMode.ALL.removes(NEUTRAL)).isTrue();
    }

    @Test
    void negativeOnlyRemovesOnlyHarmful() {
        assertThat(CleanseMode.NEGATIVE_ONLY.removes(HARMFUL)).isTrue();
        assertThat(CleanseMode.NEGATIVE_ONLY.removes(BENEFICIAL)).isFalse();
        assertThat(CleanseMode.NEGATIVE_ONLY.removes(NEUTRAL)).isFalse();
    }

    @Test
    void positiveOnlyRemovesOnlyBeneficial() {
        assertThat(CleanseMode.POSITIVE_ONLY.removes(BENEFICIAL)).isTrue();
        assertThat(CleanseMode.POSITIVE_ONLY.removes(HARMFUL)).isFalse();
        assertThat(CleanseMode.POSITIVE_ONLY.removes(NEUTRAL)).isFalse();
    }

    @Test
    void fromTagParsesKnownValues() {
        assertThat(CleanseMode.fromTag("ALL")).isEqualTo(CleanseMode.ALL);
        assertThat(CleanseMode.fromTag("negative_only")).isEqualTo(CleanseMode.NEGATIVE_ONLY);
        assertThat(CleanseMode.fromTag(" Positive_Only ")).isEqualTo(CleanseMode.POSITIVE_ONLY);
    }

    @Test
    void tagReadsBackAsTheSameMode() {
        for (CleanseMode mode : CleanseMode.values()) {
            assertThat(CleanseMode.fromTag(mode.tag())).isEqualTo(mode);
        }
    }

    @Test
    void fromTagReadsBadInputAsAll() {
        assertThat(CleanseMode.fromTag(null)).isEqualTo(CleanseMode.ALL);
        assertThat(CleanseMode.fromTag("   ")).isEqualTo(CleanseMode.ALL);
        assertThat(CleanseMode.fromTag("sideways")).isEqualTo(CleanseMode.ALL);
    }

    @Test
    void potionNamesRoundTrip() {
        for (CleanseMode mode : CleanseMode.values()) {
            assertThat(CleanseMode.fromPotionName(mode.potionName())).isEqualTo(mode);
        }
        assertThat(CleanseMode.fromPotionName("nonsense")).isNull();
    }
}
