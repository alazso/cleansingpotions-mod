package so.alaz.cleansingpotions.config;

import org.junit.jupiter.api.Test;
import so.alaz.cleansingpotions.item.MilkVariant;

import static org.assertj.core.api.Assertions.assertThat;

class BrewingAllowedTest {

    @Test
    void everyVariantIsAllowedByDefault() {
        CleansingConfig cfg = new CleansingConfig();
        for (MilkVariant variant : MilkVariant.values()) {
            assertThat(cfg.brewingAllowed(variant)).isTrue();
        }
    }

    @Test
    void brewingOffStopsEveryVariant() {
        CleansingConfig cfg = new CleansingConfig();
        cfg.brewingEnabled = false;
        for (MilkVariant variant : MilkVariant.values()) {
            assertThat(cfg.brewingAllowed(variant)).isFalse();
        }
    }

    @Test
    void splashOffStopsOnlySplash() {
        CleansingConfig cfg = new CleansingConfig();
        cfg.splashEnabled = false;
        assertThat(cfg.brewingAllowed(MilkVariant.REGULAR)).isTrue();
        assertThat(cfg.brewingAllowed(MilkVariant.SPLASH)).isFalse();
        assertThat(cfg.brewingAllowed(MilkVariant.LINGERING)).isTrue();
    }

    @Test
    void lingeringOffStopsOnlyLingering() {
        CleansingConfig cfg = new CleansingConfig();
        cfg.lingeringEnabled = false;
        assertThat(cfg.brewingAllowed(MilkVariant.REGULAR)).isTrue();
        assertThat(cfg.brewingAllowed(MilkVariant.SPLASH)).isTrue();
        assertThat(cfg.brewingAllowed(MilkVariant.LINGERING)).isFalse();
    }
}
