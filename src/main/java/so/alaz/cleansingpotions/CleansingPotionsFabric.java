package so.alaz.cleansingpotions;

//? fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import so.alaz.cleansingpotions.brew.FabricBrewingCondition;
import so.alaz.cleansingpotions.command.CleansingCommand;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.entity.ModEntities;
import so.alaz.cleansingpotions.item.ModComponents;
import so.alaz.cleansingpotions.item.ModItems;
import so.alaz.cleansingpotions.metrics.FabricFastStats;
import so.alaz.cleansingpotions.platform.FabricPlatformHelper;

import java.util.ServiceConfigurationError;

public class CleansingPotionsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModItems.build();
        ModEntities.build();

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ModComponents.CLEANSE_MODE_ID, ModComponents.CLEANSE_MODE);
        for (ModItems.Entry entry : ModItems.entries()) {
            Registry.register(BuiltInRegistries.ITEM, entry.key(), entry.item());
        }
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ModEntities.CLEANSING_POTION_KEY, ModEntities.CLEANSING_POTION);
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ModEntities.CLEANSING_CLOUD_KEY, ModEntities.CLEANSING_CLOUD);
        ModItems.registerDispenserBehaviors();
        ResourceConditions.register(FabricBrewingCondition.TYPE);

        CleansingPotions.init();

        if (CleansingConfig.get().permissionsEnabled
            && !FabricLoader.getInstance().isModLoaded(FabricPlatformHelper.PERMISSIONS_MOD_ID)) {
            Constants.LOG.warn("permissionsEnabled is on, but the gate has no effect without Fabric Permissions API ({}).",
                FabricPlatformHelper.PERMISSIONS_MOD_ID);
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            CleansingCommand.register(dispatcher));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CleansingPotions.cooldowns().clear());

        if (CleansingConfig.get().metrics) {
            try {
                FabricFastStats.start();
            } catch (RuntimeException | LinkageError | ServiceConfigurationError failure) {
                // The nested FastStats mod pins one exact Minecraft version, so a patch release can
                // reject it, and creating the context reads and writes config/faststats.
                Constants.LOG.warn("FastStats is unavailable; continuing without metrics ({})",
                    failure.toString());
            }
        }
    }
}
//?}
