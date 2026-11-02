package so.alaz.cleansingpotions.brew;

//? if >=26.3 {
/*import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BrewingRecipeConditionsTest {

    private static List<Path> recipes() throws IOException, URISyntaxException {
        Path directory = Path.of(BrewingRecipeConditionsTest.class
            .getResource("/data/cleansingpotions/recipe/brewing").toURI());
        try (Stream<Path> files = Files.list(directory)) {
            return files.toList();
        }
    }

    private static String variantOf(Path recipe) {
        String name = recipe.getFileName().toString();
        if (name.endsWith("_splash.json")) {
            return "splash";
        }
        return name.endsWith("_lingering.json") ? "lingering" : "regular";
    }

    @Test
    void everyRecipeNamesItsVariantForBothLoaders() throws IOException, URISyntaxException {
        List<Path> recipes = recipes();
        assertThat(recipes).hasSize(9);
        for (Path recipe : recipes) {
            String json = Files.readString(recipe).replaceAll("\\s", "");
            String variant = "\"variant\":\"" + variantOf(recipe) + '"';
            assertThat(json)
                .as(recipe.getFileName().toString())
                .contains("\"fabric:load_conditions\":[{\"condition\":\"cleansingpotions:brewing\"," + variant + "}]")
                .contains("\"neoforge:conditions\":[{\"type\":\"cleansingpotions:brewing\"," + variant + "}]");
        }
    }
}
*///?}
