package so.alaz.cleansingpotions.item;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import so.alaz.cleansingpotions.core.CleanseMode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PotionLangTest {

    private static String lang;

    @BeforeAll
    static void readLangFile() throws IOException {
        try (InputStream in = PotionLangTest.class.getResourceAsStream("/assets/cleansingpotions/lang/en_us.json")) {
            lang = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String title(CleanseMode mode) {
        String name = mode.potionName();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static void assertEntry(String key, String value) {
        assertThat(lang).contains('"' + key + "\": \"" + value + '"');
    }

    private static void assertKey(String key) {
        assertThat(lang).contains('"' + key + "\": \"");
    }

    @Test
    void everyPotionItemHasItsName() {
        for (CleanseMode mode : CleanseMode.values()) {
            String base = "item.cleansingpotions." + mode.potionName();
            assertEntry(base + "_potion", "Potion of " + title(mode));
            assertEntry(base + "_splash_potion", "Splash Potion of " + title(mode));
            assertEntry(base + "_lingering_potion", "Lingering Potion of " + title(mode));
        }
    }

    @Test
    void everyModeHasATooltip() {
        for (CleanseMode mode : CleanseMode.values()) {
            assertKey("item.cleansingpotions." + mode.potionName() + ".desc");
        }
    }

    @Test
    void everyRefusalHasAMessage() {
        assertKey("message.cleansingpotions.no_use");
        assertKey("message.cleansingpotions.variant_disabled");
        assertKey("message.cleansingpotions.cooldown");
    }
}
