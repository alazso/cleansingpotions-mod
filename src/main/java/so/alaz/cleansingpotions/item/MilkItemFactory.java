package so.alaz.cleansingpotions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import so.alaz.cleansingpotions.core.CleanseMode;

/**
 * Builds cleansing potion stacks and reads their mode and variant. A stack it builds has no cleanse_mode component:
 * its mode and variant come from the item.
 */
public final class MilkItemFactory {

    // The style of a generated custom name on a legacy stack. An anvil rename has an empty style.
    private static final Style LEGACY_NAME_STYLE = Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE);

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

    /**
     * Replaces a legacy stack in a player's inventory with its per-mode item. The new stack keeps the count,
     * unrelated components, and any custom name the player gave it. A stack being drunk stays as it is until the
     * drink ends, because changing the item in hand cancels the drink.
     */
    public static void migrateLegacy(ItemStack stack, Entity owner) {
        if (!stack.has(ModComponents.CLEANSE_MODE) || !(owner instanceof Player player) || player.getUseItem() == stack) {
            return;
        }
        ItemStack migrated = stack.transmuteCopy(ModItems.get(variantOf(stack), modeOf(stack)));
        migrated.remove(ModComponents.CLEANSE_MODE);
        migrated.remove(DataComponents.LORE);
        migrated.remove(DataComponents.CUSTOM_MODEL_DATA);
        Component name = migrated.get(DataComponents.CUSTOM_NAME);
        if (name != null && LEGACY_NAME_STYLE.equals(name.getStyle())) {
            migrated.remove(DataComponents.CUSTOM_NAME);
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i) == stack) {
                inventory.setItem(i, migrated);
                return;
            }
        }
    }

    private MilkItemFactory() {
    }
}
