package so.alaz.cleansingpotions.item;

import net.minecraft.world.item.ItemStack;
import so.alaz.cleansingpotions.core.CleanseMode;

/** Builds cleansing potion stacks and reads their mode and variant. A stack carries no components: both come from the item. */
public final class MilkItemFactory {

    public static ItemStack create(MilkVariant variant, CleanseMode mode) {
        return new ItemStack(ModItems.get(variant, mode));
    }

    public static CleanseMode modeOf(ItemStack stack) {
        // A legacy cleanse_mode component overrides the item's mode until the stack is migrated.
        CleanseMode legacy = stack.get(ModComponents.CLEANSE_MODE);
        if (legacy != null) {
            return legacy;
        }
        return stack.getItem() instanceof CleansingItem item ? item.cleanseMode() : CleanseMode.ALL;
    }

    public static MilkVariant variantOf(ItemStack stack) {
        return stack.getItem() instanceof CleansingItem item ? item.variant() : MilkVariant.REGULAR;
    }

    private MilkItemFactory() {
    }
}
