package so.alaz.cleansingpotions;

//? fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import so.alaz.cleansingpotions.entity.ModEntities;
import so.alaz.cleansingpotions.update.UpdateNotifier;

public class CleansingPotionsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntities.CLEANSING_POTION, ThrownItemRenderer::new);
        EntityRenderers.register(ModEntities.CLEANSING_CLOUD, NoopRenderer::new);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> UpdateNotifier.onJoin());
    }
}
//?}
