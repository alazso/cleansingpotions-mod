package so.alaz.cleansingpotions;

//? fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import so.alaz.cleansingpotions.entity.ModEntities;

public class CleansingPotionsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.CLEANSING_POTION, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.CLEANSING_CLOUD, NoopRenderer::new);
    }
}
//?}
