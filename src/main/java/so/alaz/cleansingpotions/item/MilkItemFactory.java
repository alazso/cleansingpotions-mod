package so.alaz.cleansingpotions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;

import java.util.List;

/**
 * Builds cleansing potion stacks, and supplies the display name and lore the item classes render.
 *
 * <p>A stack carries exactly one component: {@code cleanse_mode}. Name and lore are derived from
 * config at display time rather than baked in at creation time, and the tint is chosen by the model
 * from that same component. That keeps a stack built here identical to one produced by a data pack
 * recipe, which can only set static components, and means a config edit restyles potions already in
 * an inventory instead of only newly brewed ones.
 */
public final class MilkItemFactory {

    public static ItemStack create(MilkVariant variant, CleanseMode mode) {
        Item item = switch (variant) {
            case REGULAR -> ModItems.CLEANSING;
            case SPLASH -> ModItems.SPLASH;
            case LINGERING -> ModItems.LINGERING;
        };
        ItemStack stack = new ItemStack(item);
        stack.set(ModComponents.CLEANSE_MODE, mode);
        return stack;
    }

    public static CleanseMode modeOf(ItemStack stack) {
        return stack.getOrDefault(ModComponents.CLEANSE_MODE, CleanseMode.ALL);
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
