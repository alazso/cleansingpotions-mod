package so.alaz.cleansingpotions.platform;

//? fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import so.alaz.cleansingpotions.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean hasPermission(Entity player, String node, boolean defaultValue) {
        if (!FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) {
            return defaultValue;
        }
        try {
            Class<?> permissions = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
            return (boolean) permissions.getMethod("check", Entity.class, String.class, boolean.class)
                .invoke(null, player, node, defaultValue);
        } catch (Throwable ignored) {
            return defaultValue;
        }
    }
}
//?}
