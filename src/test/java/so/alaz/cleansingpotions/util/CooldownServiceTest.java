package so.alaz.cleansingpotions.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CooldownServiceTest {

    private final CooldownService cooldowns = new CooldownService();
    private final UUID player = UUID.randomUUID();

    @Test
    void aPlayerWhoHasNotThrownIsNotCooling() {
        assertThat(cooldowns.isCooling(player, 3600)).isFalse();
        assertThat(cooldowns.remainingSeconds(player, 3600)).isZero();
    }

    @Test
    void aThrowStartsTheCooldownForThatPlayerOnly() {
        cooldowns.start(player);

        assertThat(cooldowns.isCooling(player, 3600)).isTrue();
        assertThat(cooldowns.remainingSeconds(player, 3600)).isBetween(3599L, 3600L);
        assertThat(cooldowns.isCooling(UUID.randomUUID(), 3600)).isFalse();
    }

    @Test
    void theCurrentLengthAppliesToAThrowAlreadyCooling() {
        cooldowns.start(player);
        assertThat(cooldowns.isCooling(player, 3600)).isTrue();

        assertThat(cooldowns.isCooling(player, 0)).isFalse();
        assertThat(cooldowns.remainingSeconds(player, 0)).isZero();
    }

    @Test
    void clearForgetsEveryThrow() {
        cooldowns.start(player);

        cooldowns.clear();

        assertThat(cooldowns.isCooling(player, 3600)).isFalse();
    }
}
