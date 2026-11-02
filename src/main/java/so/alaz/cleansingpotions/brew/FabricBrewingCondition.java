package so.alaz.cleansingpotions.brew;

//? fabric {
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.RegistryOps;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.item.MilkVariant;

public record FabricBrewingCondition(MilkVariant variant) implements ResourceCondition {

    public static final ResourceConditionType<FabricBrewingCondition> TYPE = ResourceConditionType.create(
        BrewingCondition.ID,
        BrewingCondition.VARIANT_CODEC.xmap(FabricBrewingCondition::new, FabricBrewingCondition::variant));

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    // Runs on a data-load worker thread.
    @Override
    public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
        return CleansingConfig.get().brewingAllowed(variant);
    }
}
//?}
