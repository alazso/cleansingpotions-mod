package so.alaz.cleansingpotions.platform.services;

import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface IPlatformHelper {

    String getPlatformName();

    String getModVersion();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    Path getConfigDir();

    /** Whether the loader's permission system grants {@code cleansingpotions.use}, which defaults to allowed. */
    boolean hasUsePermission(ServerPlayer player);
}
