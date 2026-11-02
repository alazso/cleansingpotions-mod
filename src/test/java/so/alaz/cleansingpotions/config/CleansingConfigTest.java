package so.alaz.cleansingpotions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CleansingConfigTest {

    // Matches the Gson that ConfigManager builds.
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** The shape of a 0.2.0 file, with the per-potion blocks that later versions no longer read. */
    private static final String OLD_FILE = """
        {
          "splashEnabled": false,
          "radius": 7.5,
          "cleansing": {"name": "Potion of Cleansing", "lore": ["Removes every effect"], "color": 16777215},
          "soothing": {"name": "Potion of Soothing", "lore": ["Removes harmful effects"], "color": 15246139},
          "purging": {"name": "Potion of Purging", "lore": ["Removes beneficial effects"], "color": 8279976},
          "cooldownSeconds": 12
        }
        """;

    private static CleansingConfig withRadius(double radius) {
        CleansingConfig cfg = new CleansingConfig();
        cfg.radius = radius;
        cfg.sanitize();
        return cfg;
    }

    private static CleansingConfig withCooldown(long seconds) {
        CleansingConfig cfg = new CleansingConfig();
        cfg.cooldownSeconds = seconds;
        cfg.sanitize();
        return cfg;
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void nonFiniteRadiusBecomesTheDefault(double radius) {
        assertThat(withRadius(radius).radius).isEqualTo(4.0);
    }

    @ParameterizedTest
    @CsvSource({"-5.0, 0.5", "0.0, 0.5", "0.25, 0.5", "0.5, 0.5", "4.5, 4.5", "32.0, 32.0", "32.5, 32.0", "1.0E308, 32.0"})
    void radiusIsClamped(double radius, double expected) {
        assertThat(withRadius(radius).radius).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"-9223372036854775808, 0", "-1, 0", "0, 0", "5, 5", "3600, 3600", "3601, 3600", "9223372036854775807, 3600"})
    void cooldownIsClamped(long seconds, long expected) {
        assertThat(withCooldown(seconds).cooldownSeconds).isEqualTo(expected);
    }

    @Test
    void nullForceListsBecomeEmpty() {
        CleansingConfig cfg = new CleansingConfig();
        cfg.forceRemove = null;
        cfg.forceKeep = null;
        cfg.sanitize();
        assertThat(cfg.forceRemove).isEmpty();
        assertThat(cfg.forceKeep).isEmpty();
    }

    @Test
    void nullEntriesAreDroppedAndTheOrderIsKept() {
        CleansingConfig cfg = new CleansingConfig();
        cfg.forceRemove = Arrays.asList("minecraft:poison", null, "minecraft:wither");
        cfg.forceKeep = new ArrayList<>(Arrays.asList(null, null));
        cfg.sanitize();
        assertThat(cfg.forceRemove).containsExactly("minecraft:poison", "minecraft:wither");
        assertThat(cfg.forceKeep).isEmpty();
    }

    @Test
    void defaultsStayAsTheyAre() {
        CleansingConfig cfg = new CleansingConfig();
        cfg.sanitize();
        CleansingConfig untouched = new CleansingConfig();
        assertThat(cfg.radius).isEqualTo(untouched.radius);
        assertThat(cfg.cooldownSeconds).isEqualTo(untouched.cooldownSeconds);
        assertThat(cfg.forceRemove).isEqualTo(untouched.forceRemove);
        assertThat(cfg.forceKeep).isEqualTo(untouched.forceKeep);
    }

    @Test
    void oldFileLoadsAndTheNextSaveDropsItsPotionBlocks() {
        CleansingConfig cfg = GSON.fromJson(OLD_FILE, CleansingConfig.class);
        cfg.sanitize();
        assertThat(cfg.splashEnabled).isFalse();
        assertThat(cfg.radius).isEqualTo(7.5);
        assertThat(cfg.cooldownSeconds).isEqualTo(12);
        // A value the old file does not name keeps its default.
        assertThat(cfg.lingeringEnabled).isTrue();

        String saved = GSON.toJson(cfg);
        assertThat(saved).doesNotContain("\"cleansing\"", "\"soothing\"", "\"purging\"", "Potion of");

        CleansingConfig reloaded = GSON.fromJson(saved, CleansingConfig.class);
        assertThat(reloaded.splashEnabled).isFalse();
        assertThat(reloaded.radius).isEqualTo(7.5);
        assertThat(reloaded.forceRemove).isEqualTo(cfg.forceRemove);
    }

    @Test
    void badValuesFromAFileCanBeSavedOnceSanitized() {
        CleansingConfig cfg = GSON.fromJson("""
            {"radius": NaN, "cooldownSeconds": -4, "forceRemove": null, "forceKeep": ["minecraft:luck", null]}
            """, CleansingConfig.class);
        cfg.sanitize();
        String saved = GSON.toJson(cfg);
        CleansingConfig reloaded = GSON.fromJson(saved, CleansingConfig.class);
        assertThat(reloaded.radius).isEqualTo(4.0);
        assertThat(reloaded.cooldownSeconds).isZero();
        assertThat(reloaded.forceRemove).isEmpty();
        assertThat(reloaded.forceKeep).isEqualTo(List.of("minecraft:luck"));
    }
}
