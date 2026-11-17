package so.alaz.cleansingpotions.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import so.alaz.cleansingpotions.core.EffectDisposition;

public final class EffectClassifier {

    public EffectDisposition disposition(Holder<MobEffect> effect) {
        return switch (effect.value().getCategory()) {
            case BENEFICIAL -> EffectDisposition.BENEFICIAL;
            case HARMFUL -> EffectDisposition.HARMFUL;
            case NEUTRAL -> EffectDisposition.NEUTRAL;
        };
    }

    public String id(Holder<MobEffect> effect) {
        return effect.getRegisteredName();
    }
}
