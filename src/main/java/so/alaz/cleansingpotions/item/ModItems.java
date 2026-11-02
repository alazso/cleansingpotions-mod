package so.alaz.cleansingpotions.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.block.DispenserBlock;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.core.CleanseMode;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The nine cleansing potion items, one per variant and mode. 26.3 brewing recipes match on item id only, so
 * the mode is part of the item's identity. The three Cleansing ids must not change: legacy stacks use them.
 */
public final class ModItems {

    public record Entry(MilkVariant variant, CleanseMode mode, ResourceKey<Item> key, Item item) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<MilkVariant, Map<CleanseMode, Item>> BY_VARIANT =
        new EnumMap<>(MilkVariant.class);

    /** Registry path, such as {@code soothing_splash_potion}. */
    public static String path(MilkVariant variant, CleanseMode mode) {
        String base = mode.potionName();
        return switch (variant) {
            case REGULAR -> base + "_potion";
            case SPLASH -> base + "_splash_potion";
            case LINGERING -> base + "_lingering_potion";
        };
    }

    public static void build() {
        if (!ENTRIES.isEmpty()) {
            return;
        }
        for (MilkVariant variant : MilkVariant.values()) {
            Map<CleanseMode, Item> byMode = new EnumMap<>(CleanseMode.class);
            for (CleanseMode mode : CleanseMode.values()) {
                ResourceKey<Item> key = key(path(variant, mode));
                Item.Properties properties = new Item.Properties().setId(key).stacksTo(1);
                Item item = variant == MilkVariant.REGULAR
                    ? new CleansingPotionItem(drinkable(properties), mode)
                    : new CleansingThrowItem(properties, variant, mode);
                byMode.put(mode, item);
                ENTRIES.add(new Entry(variant, mode, key, item));
            }
            BY_VARIANT.put(variant, byMode);
        }
    }

    /**
     * Applies the CONSUMABLE and USE_REMAINDER components vanilla potions use, which supply the drinking
     * animation, sounds, stat, advancement trigger, and empty bottle.
     */
    private static Item.Properties drinkable(Item.Properties properties) {
        return properties
            .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
            .usingConvertsTo(Items.GLASS_BOTTLE);
    }

    /**
     * Makes dispensers throw the splash and lingering potions. Requires {@link #build} first, and must run
     * on the main thread: {@link DispenserBlock#DISPENSER_REGISTRY} is not thread-safe.
     */
    public static void registerDispenserBehaviors() {
        for (Entry entry : ENTRIES) {
            if (entry.item() instanceof CleansingThrowItem) {
                DispenserBlock.registerProjectileBehavior(entry.item());
            }
        }
    }

    /** Every item with its registry key, for the loader-specific registration calls. */
    public static List<Entry> entries() {
        return List.copyOf(ENTRIES);
    }

    public static Item get(MilkVariant variant, CleanseMode mode) {
        return BY_VARIANT.get(variant).get(mode);
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    private ModItems() {
    }
}
