package so.alaz.cleansingpotions.platform.services;

import net.minecraft.world.entity.Entity;

import java.nio.file.Path;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    Path getConfigDir();

    boolean hasPermission(Entity player, String node, boolean defaultValue);
}
