package so.alaz.cleansingpotions.brew;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.item.MilkVariant;

/** Id and codec shared by the Fabric and NeoForge load conditions that gate the brewing recipes. */
public final class BrewingCondition {

    public static final Identifier ID = CleansingPotions.id("brewing");

    public static final MapCodec<MilkVariant> VARIANT_CODEC = Codec.STRING.comapFlatMap(
            raw -> {
                MilkVariant variant = MilkVariant.fromTag(raw);
                return variant != null
                    ? DataResult.success(variant)
                    : DataResult.error(() -> "Unknown brewing variant: " + raw);
            },
            MilkVariant::tag)
        .fieldOf("variant");

    private BrewingCondition() {
    }
}
