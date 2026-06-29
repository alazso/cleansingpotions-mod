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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import so.alaz.cleansingpotions.command.CleansingCommand;
import so.alaz.cleansingpotions.config.CleansingConfigScreen;
import so.alaz.cleansingpotions.entity.ModEntities;
import so.alaz.cleansingpotions.item.ModComponents;
import so.alaz.cleansingpotions.item.ModItems;

@Mod(Constants.MOD_ID)
public class CleansingPotionsNeoForge {

    //? if >=26.1.2 {
    @SuppressWarnings("unused")
    private dev.faststats.neoforge.NeoForgeContext metrics;
    //?}

    public CleansingPotionsNeoForge(IEventBus modBus, ModContainer container, Dist dist) {
        // Items must be built in RegisterEvent: registries are frozen by constructor time on NeoForge.
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onRegisterRenderers);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        CleansingPotions.init();

        if (dist.isClient()) {
            if (ModList.get().isLoaded("cloth_config")) {
                container.registerExtensionPoint(IConfigScreenFactory.class,
                    (mc, parent) -> CleansingConfigScreen.create(parent));
            }
            NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) ->
                    so.alaz.cleansingpotions.update.UpdateNotifier.onJoin());
        }

        //? if >=26.1.2 {
        if (so.alaz.cleansingpotions.config.CleansingConfig.get().metrics) {
            metrics = new dev.faststats.neoforge.NeoForgeContext.Factory("cleansingpotions", "321f21c8899fa729330e9508a4b7fe34")
                    .metrics(so.alaz.cleansingpotions.metrics.FastStatsBridge::registerMetrics)
                    .errorTrackerService(so.alaz.cleansingpotions.metrics.FastStatsBridge.errorTracker())
                    .create();
        }
        //?}
    }

    private void onRegister(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, helper ->
            helper.register(ModComponents.CLEANSE_MODE_ID, ModComponents.CLEANSE_MODE));
        event.register(Registries.ITEM, helper -> {
            ModItems.build();
            helper.register(ModItems.CLEANSING_KEY, ModItems.CLEANSING);
            helper.register(ModItems.SPLASH_KEY, ModItems.SPLASH);
            helper.register(ModItems.LINGERING_KEY, ModItems.LINGERING);
        });
        event.register(Registries.ENTITY_TYPE, helper -> {
            ModEntities.build();
            helper.register(ModEntities.CLEANSING_POTION_KEY, ModEntities.CLEANSING_POTION);
            helper.register(ModEntities.CLEANSING_CLOUD_KEY, ModEntities.CLEANSING_CLOUD);
        });
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
