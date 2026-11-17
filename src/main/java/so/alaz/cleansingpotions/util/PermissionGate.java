package so.alaz.cleansingpotions.util;

import net.minecraft.server.level.ServerPlayer;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.platform.Services;

public final class PermissionGate {

    /** Whether the player may use a potion. Always true while the gate is off and in singleplayer. */
    public static boolean allowsUse(ServerPlayer player) {
        if (!CleansingConfig.get().permissionsEnabled) {
            return true;
        }
        if (player.level().getServer().isSingleplayer()) {
            return true;
        }
        return Services.PLATFORM.hasUsePermission(player);
    }

    private PermissionGate() {
    }
}
