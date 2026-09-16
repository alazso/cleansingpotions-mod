package so.alaz.cleansingpotions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;

import java.util.List;

/**
 * Builds cleansing potion stacks, and supplies the display name and lore the item classes render.
 *
 * <p>A stack carries no components at all: the mode is the item's identity (see {@link ModItems})
 * and name, lore, and tint are derived from it at display time. That keeps a stack built here
 * identical to one produced by a data pack recipe, which can only set static components, and means
 * a config edit restyles potions already in an inventory instead of only newly brewed ones.
 */
public final class MilkItemFactory {

    public static ItemStack create(MilkVariant variant, CleanseMode mode) {
        return new ItemStack(ModItems.get(variant, mode));
    }

    public static CleanseMode modeOf(ItemStack stack) {
        // Stacks from before 0.3.0 carried the mode in a cleanse_mode component on a single item
        // id. Honour it where present so an existing Soothing or Purging potion keeps working;
        // such a stack renders with the Cleansing tint, since the model picks tint by item.
        CleanseMode legacy = stack.get(ModComponents.CLEANSE_MODE);
        if (legacy != null) {
            return legacy;
        }
        return stack.getItem() instanceof CleansingItem item ? item.cleanseMode() : CleanseMode.ALL;
    }

    public static MilkVariant variantOf(ItemStack stack) {
        return stack.getItem() instanceof CleansingItem item ? item.variant() : MilkVariant.REGULAR;
    }

    public static Component displayName(MilkVariant variant, CleanseMode mode) {
        String prefix = switch (variant) {
            case REGULAR -> "";
            case SPLASH -> "Splash ";
            case LINGERING -> "Lingering ";
        };
        return Component.literal(prefix + CleansingConfig.get().appearance(mode).name)
            .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.WHITE));
    }

    public static List<Component> loreLines(CleanseMode mode) {
        List<String> lines = CleansingConfig.get().appearance(mode).lore;
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        return lines.stream()
            .map(line -> (Component) Component.literal(line)
                .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY)))
            .toList();
    }

    private MilkItemFactory() {
    }
}
