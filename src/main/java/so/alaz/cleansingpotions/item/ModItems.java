package so.alaz.cleansingpotions.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import so.alaz.cleansingpotions.Constants;

public final class ModItems {

    public static final ResourceKey<Item> CLEANSING_KEY = key("cleansing_potion");
    public static final ResourceKey<Item> SPLASH_KEY = key("cleansing_splash_potion");
    public static final ResourceKey<Item> LINGERING_KEY = key("cleansing_lingering_potion");

    public static CleansingPotionItem CLEANSING;
    public static CleansingThrowItem SPLASH;
    public static CleansingThrowItem LINGERING;

    public static void build() {
        if (CLEANSING != null) {
            return;
        }
        CLEANSING = new CleansingPotionItem(new Item.Properties()
            .setId(CLEANSING_KEY)
            .stacksTo(1));
        SPLASH = new CleansingThrowItem(new Item.Properties()
            .setId(SPLASH_KEY)
            .stacksTo(1));
        LINGERING = new CleansingThrowItem(new Item.Properties()
            .setId(LINGERING_KEY)
            .stacksTo(1));
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    private ModItems() {
    }
}
