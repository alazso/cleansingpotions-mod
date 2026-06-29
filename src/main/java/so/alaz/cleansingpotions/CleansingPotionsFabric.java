package so.alaz.cleansingpotions;

//? fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import so.alaz.cleansingpotions.command.CleansingCommand;
import so.alaz.cleansingpotions.entity.ModEntities;
import so.alaz.cleansingpotions.item.ModComponents;
import so.alaz.cleansingpotions.item.ModItems;

public class CleansingPotionsFabric implements ModInitializer {

    //? if >=26.1.2 {
    /*@SuppressWarnings("unused")
    private static dev.faststats.fabric.FabricContext metrics;
    *///?}

    @Override
    public void onInitialize() {
        ModItems.build();
        ModEntities.build();

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ModComponents.CLEANSE_MODE_ID, ModComponents.CLEANSE_MODE);
        Registry.register(BuiltInRegistries.ITEM, ModItems.CLEANSING_KEY, ModItems.CLEANSING);
        Registry.register(BuiltInRegistries.ITEM, ModItems.SPLASH_KEY, ModItems.SPLASH);
        Registry.register(BuiltInRegistries.ITEM, ModItems.LINGERING_KEY, ModItems.LINGERING);
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ModEntities.CLEANSING_POTION_KEY, ModEntities.CLEANSING_POTION);
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ModEntities.CLEANSING_CLOUD_KEY, ModEntities.CLEANSING_CLOUD);

        CleansingPotions.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            CleansingCommand.register(dispatcher));

        //? if >=26.1.2 {
        /*if (so.alaz.cleansingpotions.config.CleansingConfig.get().metrics) {
            metrics = new dev.faststats.fabric.FabricContext.Factory("cleansingpotions", "321f21c8899fa729330e9508a4b7fe34")
                    .metrics(so.alaz.cleansingpotions.metrics.FastStatsBridge::registerMetrics)
                    .errorTrackerService(so.alaz.cleansingpotions.metrics.FastStatsBridge.errorTracker())
                    .create();
        }
        *///?}
    }
}
//?}
