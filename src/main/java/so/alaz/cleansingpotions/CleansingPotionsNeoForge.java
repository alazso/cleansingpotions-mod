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
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import so.alaz.cleansingpotions.command.CleansingCommand;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.config.CleansingConfigScreen;
import so.alaz.cleansingpotions.entity.ModEntities;
import so.alaz.cleansingpotions.item.ModComponents;
import so.alaz.cleansingpotions.item.ModItems;
import so.alaz.cleansingpotions.metrics.NeoForgeFastStats;
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
        CleansingPotions.init();

        if (dist.isClient()) {
            if (ModList.get().isLoaded("cloth_config")) {
                container.registerExtensionPoint(IConfigScreenFactory.class,
                    (mc, parent) -> CleansingConfigScreen.create(parent));
            }
            NeoForge.EVENT_BUS.addListener(
                (ClientPlayerNetworkEvent.LoggingIn event) -> UpdateNotifier.onJoin());
        }

        if (CleansingConfig.get().metrics) {
            try {
                NeoForgeFastStats.start();
            } catch (RuntimeException | LinkageError | ServiceConfigurationError failure) {
                // The nested FastStats mod pins one exact Minecraft version, so a patch release can
                // reject it, and creating the context reads and writes config/faststats.
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
}
*///?}
