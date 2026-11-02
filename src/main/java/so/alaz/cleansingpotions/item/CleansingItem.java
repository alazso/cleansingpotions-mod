package so.alaz.cleansingpotions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;
import so.alaz.cleansingpotions.core.CleanseMode;

import java.util.function.Consumer;

/** Base of every cleansing potion item. Holds the mode and variant fixed at registration. */
public abstract class CleansingItem extends Item {

    private final MilkVariant variant;
    private final CleanseMode mode;

    protected CleansingItem(Properties properties, MilkVariant variant, CleanseMode mode) {
        super(properties);
        this.variant = variant;
        this.mode = mode;
    }

    public CleanseMode cleanseMode() {
        return mode;
    }

    public MilkVariant variant() {
        return variant;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> adder, TooltipFlag flag) {
        adder.accept(Component.translatable("item.cleansingpotions." + MilkItemFactory.modeOf(stack).potionName() + ".desc")
            .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        MilkItemFactory.migrateLegacy(stack, owner);
    }
}
