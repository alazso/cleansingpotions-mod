package so.alaz.cleansingpotions.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
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
    public Component getName(ItemStack stack) {
        return MilkItemFactory.displayName(variant, MilkItemFactory.modeOf(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> adder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, adder, flag);
        MilkItemFactory.loreLines(MilkItemFactory.modeOf(stack)).forEach(adder);
    }
}
