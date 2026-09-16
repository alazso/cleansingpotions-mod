package so.alaz.cleansingpotions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.entity.CleansingPotionEntity;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;
import so.alaz.cleansingpotions.util.PermissionGate;

public class CleansingThrowItem extends Item {

    // One class backs both the splash and lingering items, so it has to be told which it is in
    // order to prefix the display name.
    private final MilkVariant variant;

    public CleansingThrowItem(Properties properties, MilkVariant variant) {
        super(properties);
        this.variant = variant;
    }

    // Name and lore come from config at display time; a stack only carries cleanse_mode.
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

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CleansingConfig cfg = CleansingConfig.get();
        if (player instanceof ServerPlayer serverPlayer
            && !PermissionGate.allows(serverPlayer, "cleansingpotions.use")) {
            CleansingMetrics.permissionDenied();
            serverPlayer.sendSystemMessage(
                Component.translatable("message.cleansingpotions.no_use").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (cfg.cooldownEnabled && CleansingPotions.cooldowns().isCooling(player.getUUID())) {
            if (player instanceof ServerPlayer serverPlayer) {
                CleansingMetrics.cooldownBlocked();
                serverPlayer.sendSystemMessage(Component.translatable("message.cleansingpotions.cooldown",
                    CleansingPotions.cooldowns().remainingSeconds(player.getUUID())).withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS,
            0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel server) {
            CleansingPotionEntity projectile = new CleansingPotionEntity(server, player, stack.copyWithCount(1));
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
            server.addFreshEntity(projectile);
            CleansingMetrics.potionThrown(stack.is(ModItems.LINGERING) ? MilkVariant.LINGERING : MilkVariant.SPLASH);
            if (cfg.cooldownEnabled) {
                CleansingPotions.cooldowns().start(player.getUUID(), cfg.cooldownSeconds);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
