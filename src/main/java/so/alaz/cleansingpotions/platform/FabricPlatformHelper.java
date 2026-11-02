package so.alaz.cleansingpotions.platform;

//? fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.metrics.ErrorReporter;
import so.alaz.cleansingpotions.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    public static final String PERMISSIONS_MOD_ID = "fabric-permissions-api-v0";

    private static final String USE_NODE = Constants.MOD_ID + ".use";

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(Constants.MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("0.0.0");
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
    public boolean hasUsePermission(ServerPlayer player) {
        if (!FabricLoader.getInstance().isModLoaded(PERMISSIONS_MOD_ID)) {
            return true;
        }
        try {
            Class<?> permissions = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
            return (boolean) permissions.getMethod("check", Entity.class, String.class, boolean.class)
                .invoke(null, player, USE_NODE, true);
        } catch (Throwable reflectionFailed) {
            // The permissions mod is installed but its API no longer matches, so every check returns the default.
            ErrorReporter.report("permissions:fabric-permissions-api", reflectionFailed);
            return true;
        }
    }
}
//?}
