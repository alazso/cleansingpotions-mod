package so.alaz.cleansingpotions.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.CleansePolicy;
import so.alaz.cleansingpotions.core.EffectDisposition;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;

import java.util.List;

public final class EffectCleanser {

    private final EffectClassifier classifier;

    public EffectCleanser(EffectClassifier classifier) {
        this.classifier = classifier;
    }

    public int cleanse(LivingEntity entity, CleanseMode mode) {
        CleansePolicy policy = CleansingConfig.get().policy(mode);
        int removed = 0;
        for (MobEffectInstance effect : List.copyOf(entity.getActiveEffects())) {
            var holder = effect.getEffect();
            String id = classifier.id(holder);
            EffectDisposition disposition = classifier.disposition(holder);
            if (policy.shouldRemove(id, disposition) && entity.removeEffect(holder)) {
                removed++;
                CleansingMetrics.effectRemoved(id, disposition);
            }
        }
        CleansingMetrics.cleanseCompleted(mode, removed);
        if (removed > 0 && CleansingConfig.get().cleanseFeedback && entity.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.6, entity.getZ(),
                8, 0.35, 0.4, 0.35, 0.0);
            server.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.4F);
        }
        return removed;
    }

    /**
     * Cleanses every living entity within {@code radius} of the point. The distance is measured to the
     * entity's bounding box.
     *
     * @return how many of them lost at least one effect
     */
    public int cleanseArea(Level level, double x, double y, double z, double radius, CleanseMode mode) {
        AABB box = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        Vec3 point = new Vec3(x, y, z);
        int affected = 0;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.getBoundingBox().distanceToSqr(point) <= radius * radius && cleanse(entity, mode) > 0) {
                affected++;
            }
        }
        return affected;
    }
}
