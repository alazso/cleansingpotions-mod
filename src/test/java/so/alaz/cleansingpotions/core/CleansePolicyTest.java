package so.alaz.cleansingpotions.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static so.alaz.cleansingpotions.core.EffectDisposition.BENEFICIAL;
import static so.alaz.cleansingpotions.core.EffectDisposition.HARMFUL;
import static so.alaz.cleansingpotions.core.EffectDisposition.NEUTRAL;

class CleansePolicyTest {

    @Test
    void modeDecidesWhenNoOverrides() {
        CleansePolicy negative = new CleansePolicy(CleanseMode.NEGATIVE_ONLY, List.of(), List.of());
        assertThat(negative.shouldRemove("minecraft:poison", HARMFUL)).isTrue();
        assertThat(negative.shouldRemove("minecraft:speed", BENEFICIAL)).isFalse();
        assertThat(negative.shouldRemove("minecraft:glowing", NEUTRAL)).isFalse();
    }

    @Test
    void forceRemoveOverridesMode() {
        // bad omen is not beneficial, so POSITIVE_ONLY would keep it; force-remove wins.
        CleansePolicy policy = new CleansePolicy(CleanseMode.POSITIVE_ONLY,
            Set.of("minecraft:bad_omen"), Set.of());
        assertThat(policy.shouldRemove("minecraft:bad_omen", NEUTRAL)).isTrue();
    }

    @Test
    void forceKeepOverridesMode() {
        CleansePolicy policy = new CleansePolicy(CleanseMode.ALL, Set.of(), Set.of("minecraft:conduit_power"));
        assertThat(policy.shouldRemove("minecraft:conduit_power", BENEFICIAL)).isFalse();
    }

    @Test
    void forceKeepBeatsForceRemove() {
        CleansePolicy policy = new CleansePolicy(CleanseMode.ALL,
            Set.of("minecraft:luck"), Set.of("minecraft:luck"));
        assertThat(policy.shouldRemove("minecraft:luck", BENEFICIAL)).isFalse();
    }

    @Test
    void effectIdsMatchCaseInsensitively() {
        CleansePolicy policy = new CleansePolicy(CleanseMode.NEGATIVE_ONLY,
            Set.of("MINECRAFT:BAD_OMEN"), Set.of());
        assertThat(policy.shouldRemove("minecraft:bad_omen", HARMFUL)).isTrue();
    }
}
