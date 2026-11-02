package so.alaz.cleansingpotions.brew;

//? if neoforge && >=26.3 {
/*import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.item.MilkVariant;

public record NeoForgeBrewingCondition(MilkVariant variant) implements ICondition {

    public static final MapCodec<NeoForgeBrewingCondition> CODEC =
        BrewingCondition.VARIANT_CODEC.xmap(NeoForgeBrewingCondition::new, NeoForgeBrewingCondition::variant);

    // Runs on a data-load worker thread.
    @Override
    public boolean test(IContext context) {
        return CleansingConfig.get().brewingAllowed(variant);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
*///?}
