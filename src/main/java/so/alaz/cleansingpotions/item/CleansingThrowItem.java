package so.alaz.cleansingpotions.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.entity.CleansingPotionEntity;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;
import so.alaz.cleansingpotions.util.PermissionGate;

public class CleansingThrowItem extends CleansingItem implements ProjectileItem {

    public CleansingThrowItem(Properties properties, MilkVariant variant, CleanseMode mode) {
        super(properties, variant, mode);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // The client does not predict the throw: every refusal depends on server state, and the server sends
        // nothing back for a refused use.
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        CleansingConfig cfg = CleansingConfig.get();
        if (!PermissionGate.allows(serverPlayer, "cleansingpotions.use")) {
            CleansingMetrics.permissionDenied();
            sendRefusal(serverPlayer, Component.translatable("message.cleansingpotions.no_use"));
            return InteractionResult.FAIL;
        }
        if (!cfg.variantEnabled(variant())) {
            sendRefusal(serverPlayer, Component.translatable("message.cleansingpotions.variant_disabled"));
            return InteractionResult.FAIL;
        }
        if (cfg.cooldownEnabled && CleansingPotions.cooldowns().isCooling(player.getUUID())) {
            CleansingMetrics.cooldownBlocked();
            sendRefusal(serverPlayer, Component.translatable("message.cleansingpotions.cooldown",
                CleansingPotions.cooldowns().remainingSeconds(player.getUUID())));
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS,
            0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        CleansingPotionEntity projectile = new CleansingPotionEntity(server, player, stack.copyWithCount(1));
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
        server.addFreshEntity(projectile);
        CleansingMetrics.potionThrown(variant());
        if (cfg.cooldownEnabled) {
            CleansingPotions.cooldowns().start(player.getUUID(), cfg.cooldownSeconds);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new CleansingPotionEntity(level, pos.x(), pos.y(), pos.z(), stack);
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder()
            .uncertainty(DispenseConfig.DEFAULT.uncertainty() * 0.5F)
            .power(DispenseConfig.DEFAULT.power() * 1.25F)
            .build();
    }
}
