package so.alaz.cleansingpotions.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.core.CleanseMode;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The nine cleansing potion items: one per (variant, mode).
 *
 * <p>26.3 brewing recipes match on item id and cannot read a data component, so a single item
 * carrying the mode in {@code cleanse_mode} makes "gunpowder turns a Purging potion into a Purging
 * splash potion" inexpressible. Giving each combination its own item keeps brewing, recipe viewers,
 * and the model tints all working from the same identity.
 *
 * <p>The three Cleansing ids keep their original names, so stacks from earlier versions survive.
 */
public final class ModItems {

    public record Entry(MilkVariant variant, CleanseMode mode, ResourceKey<Item> key, Item item) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<MilkVariant, Map<CleanseMode, Item>> BY_VARIANT =
        new EnumMap<>(MilkVariant.class);

    /** e.g. {@code soothing_splash_potion}. Cleansing keeps the pre-0.3.0 ids. */
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
                    ? new CleansingPotionItem(properties, mode)
                    : new CleansingThrowItem(properties, variant, mode);
                byMode.put(mode, item);
                ENTRIES.add(new Entry(variant, mode, key, item));
            }
            BY_VARIANT.put(variant, byMode);
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
