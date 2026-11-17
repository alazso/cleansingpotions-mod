package so.alaz.cleansingpotions.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import so.alaz.cleansingpotions.core.CleanseMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PotionTintTest {

    private static String path(MilkVariant variant, CleanseMode mode) {
        return switch (variant) {
            case REGULAR -> mode.potionName() + "_potion";
            case SPLASH -> mode.potionName() + "_splash_potion";
            case LINGERING -> mode.potionName() + "_lingering_potion";
        };
    }

    private static JsonObject itemDefinition(String path) throws IOException {
        String resource = "/assets/cleansingpotions/items/" + path + ".json";
        try (InputStream in = PotionTintTest.class.getResourceAsStream(resource)) {
            assertThat(in).as(resource).isNotNull();
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
    }

    @Test
    void everyItemTintMatchesItsModeColour() throws IOException {
        for (MilkVariant variant : MilkVariant.values()) {
            for (CleanseMode mode : CleanseMode.values()) {
                String path = path(variant, mode);
                JsonArray tints = itemDefinition(path).getAsJsonObject("model").getAsJsonArray("tints");
                assertThat(tints).as(path).hasSize(1);
                JsonObject tint = tints.get(0).getAsJsonObject();
                assertThat(tint.get("type").getAsString()).as(path).isEqualTo("minecraft:constant");
                assertThat(tint.get("value").getAsInt()).as(path).isEqualTo(mode.rgb());
            }
        }
    }
}
