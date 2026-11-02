package so.alaz.cleansingpotions.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;
import so.alaz.cleansingpotions.util.PermissionGate;

public class CleansingPotionItem extends CleansingItem {

    public CleansingPotionItem(Properties properties, CleanseMode mode) {
        super(properties, MilkVariant.REGULAR, mode);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer && !PermissionGate.allowsUse(serverPlayer)) {
            CleansingMetrics.permissionDenied();
            sendRefusal(serverPlayer, Component.translatable("message.cleansingpotions.no_use"));
            // The client has already started the drink, and the server sends nothing back for a refused use.
            // Starting and stopping marks the using-item flag dirty, and its update ends the drink on the client.
            serverPlayer.startUsingItem(hand);
            serverPlayer.stopUsingItem();
            serverPlayer.inventoryMenu.sendAllDataToRemote();
            return InteractionResult.FAIL;
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Runs before super consumes the stack, while modeOf can still read it.
        if (!level.isClientSide()) {
            CleansingPotions.cleanser().cleanse(entity, MilkItemFactory.modeOf(stack));
            CleansingMetrics.potionConsumed();
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
