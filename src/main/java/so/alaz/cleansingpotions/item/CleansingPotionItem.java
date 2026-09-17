package so.alaz.cleansingpotions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;
import so.alaz.cleansingpotions.util.PermissionGate;

public class CleansingPotionItem extends Item implements CleansingItem {

    private final CleanseMode mode;

    public CleansingPotionItem(Properties properties, CleanseMode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public CleanseMode cleanseMode() {
        return mode;
    }

    @Override
    public MilkVariant variant() {
        return MilkVariant.REGULAR;
    }

    // Name and lore are derived from config at display time, so a stack carries no components at
    // all and a data pack recipe can produce one that looks identical. See MilkItemFactory.
    @Override
    public Component getName(ItemStack stack) {
        return MilkItemFactory.displayName(MilkVariant.REGULAR, MilkItemFactory.modeOf(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> adder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, adder, flag);
        MilkItemFactory.loreLines(MilkItemFactory.modeOf(stack)).forEach(adder);
    }

    // Use animation and duration come from the CONSUMABLE component (see ModItems#drinkable), so
    // they are not overridden here.

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer
            && !PermissionGate.allows(serverPlayer, "cleansingpotions.use")) {
            CleansingMetrics.permissionDenied();
            serverPlayer.sendSystemMessage(
                Component.translatable("message.cleansingpotions.no_use").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Cleanse first, while the stack still holds the potion. super then does everything a
        // vanilla drink does on finishing: the closing gulp, the use stat, the consume_item
        // advancement trigger, the drink game event, and shrinking the stack. The empty bottle
        // comes back through USE_REMAINDER.
        if (!level.isClientSide()) {
            CleansingPotions.cleanser().cleanse(entity, MilkItemFactory.modeOf(stack));
            CleansingMetrics.potionConsumed();
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
