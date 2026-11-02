package so.alaz.cleansingpotions;

//? neoforge {
/*import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
//? if >=26.3 {
/^import net.neoforged.neoforge.registries.NeoForgeRegistries;
^///?}
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.handler.DefaultPermissionHandler;
//? if >=26.3 {
/^import so.alaz.cleansingpotions.brew.BrewingCondition;
import so.alaz.cleansingpotions.brew.NeoForgeBrewingCondition;
^///?}
import so.alaz.cleansingpotions.command.CleansingCommand;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.config.CleansingConfigScreen;
import so.alaz.cleansingpotions.entity.ModEntities;
import so.alaz.cleansingpotions.item.ModComponents;
import so.alaz.cleansingpotions.item.ModItems;
import so.alaz.cleansingpotions.metrics.NeoForgeFastStats;
import so.alaz.cleansingpotions.metrics.TelemetryButtonGuard;
import so.alaz.cleansingpotions.platform.NeoForgePlatformHelper;
import so.alaz.cleansingpotions.update.UpdateNotifier;

import java.util.ServiceConfigurationError;

@Mod(Constants.MOD_ID)
public class CleansingPotionsNeoForge {

    public CleansingPotionsNeoForge(IEventBus modBus, ModContainer container, Dist dist) {
        // Items must be built in RegisterEvent: registries are frozen by constructor time on NeoForge.
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onRegisterRenderers);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onGatherPermissionNodes);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
        CleansingPotions.init();

        if (dist.isClient()) {
            if (ModList.get().isLoaded("cloth_config")) {
                container.registerExtensionPoint(IConfigScreenFactory.class,
                    (mc, parent) -> CleansingConfigScreen.create(parent));
            }
            NeoForge.EVENT_BUS.addListener(
                (ClientPlayerNetworkEvent.LoggingIn event) -> UpdateNotifier.onJoin());
            NeoForge.EVENT_BUS.addListener(
                (ScreenEvent.Init.Post event) -> TelemetryButtonGuard.apply(event.getScreen(), event.getListenersList()));
        }

        if (CleansingConfig.get().metrics) {
            try {
                NeoForgeFastStats.start();
            } catch (RuntimeException | LinkageError | ServiceConfigurationError failure) {
                // Creating the context reads and writes config/faststats, and another mod can nest a
                // different FastStats.
                Constants.LOG.warn("FastStats is unavailable; continuing without metrics ({})",
                    failure.toString());
            }
        }
    }

    private void onRegister(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, helper ->
            helper.register(ModComponents.CLEANSE_MODE_ID, ModComponents.CLEANSE_MODE));
        event.register(Registries.ITEM, helper -> {
            ModItems.build();
            for (ModItems.Entry entry : ModItems.entries()) {
                helper.register(entry.key(), entry.item());
            }
        });
        event.register(Registries.ENTITY_TYPE, helper -> {
            ModEntities.build();
            helper.register(ModEntities.CLEANSING_POTION_KEY, ModEntities.CLEANSING_POTION);
            helper.register(ModEntities.CLEANSING_CLOUD_KEY, ModEntities.CLEANSING_CLOUD);
        });
        //? if >=26.3 {
        /^event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, helper ->
            helper.register(BrewingCondition.ID, NeoForgeBrewingCondition.CODEC));
        ^///?}
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // Common setup listeners run in parallel off the main thread, and the behaviour map is not thread-safe.
        event.enqueueWork(ModItems::registerDispenserBehaviors);
    }

    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CLEANSING_POTION, ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.CLEANSING_CLOUD, NoopRenderer::new);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CleansingCommand.register(event.getDispatcher());
    }

    private void onGatherPermissionNodes(PermissionGatherEvent.Nodes event) {
        // Registered whatever the config says, because PermissionAPI throws for an unregistered node.
        event.addNodes(NeoForgePlatformHelper.USE);
    }

    private void onServerStarting(ServerStartingEvent event) {
        if (CleansingConfig.get().permissionsEnabled
            && event.getServer().isDedicatedServer()
            && DefaultPermissionHandler.IDENTIFIER.equals(PermissionAPI.getActivePermissionHandler())) {
            Constants.LOG.warn("permissionsEnabled is on, but the gate has no effect without a permission handler mod selected in NeoForge's permissionHandler setting.");
        }
    }

    private void onServerStopped(ServerStoppedEvent event) {
        CleansingPotions.cooldowns().clear();
    }
}
*///?}
