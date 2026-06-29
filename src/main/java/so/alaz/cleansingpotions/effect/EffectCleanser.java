package so.alaz.cleansingpotions.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.CleansePolicy;
import so.alaz.cleansingpotions.core.EffectDisposition;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;

import java.util.List;
import java.util.function.Function;

public final class EffectCleanser {

    private final EffectClassifier classifier;
    private final Function<CleanseMode, CleansePolicy> policyLookup;

    public EffectCleanser(EffectClassifier classifier, Function<CleanseMode, CleansePolicy> policyLookup) {
        this.classifier = classifier;
        this.policyLookup = policyLookup;
    }

    public int cleanse(LivingEntity entity, CleanseMode mode) {
        CleansePolicy policy = policyLookup.apply(mode);
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
}
