package so.alaz.cleansingpotions.entity;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.item.MilkVariant;
import so.alaz.cleansingpotions.item.ModComponents;
import so.alaz.cleansingpotions.item.ModItems;

public class CleansingPotionEntity extends ThrowableItemProjectile {

    public CleansingPotionEntity(EntityType<? extends CleansingPotionEntity> type, Level level) {
        super(type, level);
    }

    public CleansingPotionEntity(Level level, LivingEntity thrower, ItemStack stack) {
        super(ModEntities.CLEANSING_POTION, thrower, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SPLASH;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        ItemStack stack = getItem();
        CleanseMode mode = stack.getOrDefault(ModComponents.CLEANSE_MODE, CleanseMode.ALL);
        boolean lingering = stack.is(ModItems.LINGERING);
        MilkVariant variant = lingering ? MilkVariant.LINGERING : MilkVariant.SPLASH;
        if (CleansingConfig.get().variantEnabled(variant)) {
            int color = CleansingConfig.get().color(mode);
            if (lingering) {
                spawnCloud(server, mode, color);
            } else {
                applySplash(mode);
            }
            if (CleansingConfig.get().impactParticles) {
                shatter(server, color);
            }
        }
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.SPLASH_POTION_BREAK,
            SoundSource.NEUTRAL, 1.0F, level().getRandom().nextFloat() * 0.1F + 0.9F);
        discard();
    }

    private void shatter(ServerLevel server, int color) {
        server.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000 | color),
            getX(), getY(), getZ(), 40, 0.3, 0.3, 0.3, 0.0);
    }

    private void applySplash(CleanseMode mode) {
        CleansingConfig cfg = CleansingConfig.get();
        if (cfg.onlyThrower) {
            if (getOwner() instanceof LivingEntity shooter) {
                CleansingPotions.cleanser().cleanse(shooter, mode);
            }
            return;
        }
        double r = cfg.radius;
        AABB box = new AABB(getX() - r, getY() - r, getZ() - r, getX() + r, getY() + r, getZ() + r);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.distanceToSqr(this) <= r * r) {
                CleansingPotions.cleanser().cleanse(entity, mode);
            }
        }
    }

    private void spawnCloud(ServerLevel server, CleanseMode mode, int color) {
        CleansingCloudEntity cloud = new CleansingCloudEntity(server, getX(), getY(), getZ());
        if (getOwner() instanceof LivingEntity shooter) {
            cloud.setOwner(shooter);
        }
        cloud.setMode(mode);
        cloud.configure((float) CleansingConfig.get().radius, color);
        server.addFreshEntity(cloud);
    }
}
