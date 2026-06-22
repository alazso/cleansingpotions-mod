package so.alaz.cleansingpotions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;

import java.util.List;

public final class MilkItemFactory {

    public static ItemStack create(MilkVariant variant, CleanseMode mode) {
        Item item = switch (variant) {
            case REGULAR -> ModItems.CLEANSING;
            case SPLASH -> ModItems.SPLASH;
            case LINGERING -> ModItems.LINGERING;
        };
        ItemStack stack = new ItemStack(item);
        stack.set(ModComponents.CLEANSE_MODE, mode);
        stack.set(DataComponents.CUSTOM_NAME, name(variant, mode));
        stack.set(DataComponents.LORE, lore(mode));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
            List.of(), List.of(), List.of(), List.of(CleansingConfig.get().color(mode))));
        return stack;
    }

    private static Component name(MilkVariant variant, CleanseMode mode) {
        String prefix = switch (variant) {
            case REGULAR -> "";
            case SPLASH -> "Splash ";
            case LINGERING -> "Lingering ";
        };
        String text = prefix + CleansingConfig.get().appearance(mode).name;
        return Component.literal(text)
            .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.WHITE));
    }

    private static ItemLore lore(CleanseMode mode) {
        List<String> lines = CleansingConfig.get().appearance(mode).lore;
        if (lines == null || lines.isEmpty()) {
            return ItemLore.EMPTY;
        }
        List<Component> rendered = lines.stream()
            .map(line -> (Component) Component.literal(line)
                .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY)))
            .toList();
        return new ItemLore(rendered);
    }

    private MilkItemFactory() {
    }
}
