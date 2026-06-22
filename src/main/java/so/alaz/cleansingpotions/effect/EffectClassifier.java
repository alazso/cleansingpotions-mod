package so.alaz.cleansingpotions.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import so.alaz.cleansingpotions.core.EffectDisposition;

public final class EffectClassifier {

    public EffectDisposition disposition(Holder<MobEffect> effect) {
        MobEffectCategory category = effect.value().getCategory();
        if (category == null) {
            return EffectDisposition.NEUTRAL;
        }
        return switch (category) {
            case BENEFICIAL -> EffectDisposition.BENEFICIAL;
            case HARMFUL -> EffectDisposition.HARMFUL;
            case NEUTRAL -> EffectDisposition.NEUTRAL;
        };
    }

    public String id(Holder<MobEffect> effect) {
        var key = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
        return key == null ? "" : key.toString();
    }
}
