package so.alaz.cleansingpotions.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.platform.Services;

public final class PermissionGate {

    public static boolean allows(ServerPlayer player, String node) {
        if (!CleansingConfig.get().permissionsEnabled) {
            return true;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null || server.isSingleplayer()) {
            return true;
        }
        return Services.PLATFORM.hasPermission(player, node, true);
    }

    private PermissionGate() {
    }
}
