package so.alaz.cleansingpotions.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.CleansePolicy;

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
            if (policy.shouldRemove(classifier.id(holder), classifier.disposition(holder))
                && entity.removeEffect(holder)) {
                removed++;
            }
        }
        return removed;
    }
}
