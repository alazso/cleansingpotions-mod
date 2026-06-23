package so.alaz.cleansingpotions.platform;

//? neoforge {
/*import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public String getModVersion() {
        return ModList.get().getModContainerById(Constants.MOD_ID)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("0.0.0");
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return false;
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean hasPermission(Entity player, String node, boolean defaultValue) {
        return defaultValue;
    }
}
*///?}
