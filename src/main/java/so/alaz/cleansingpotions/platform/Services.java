package so.alaz.cleansingpotions.platform;

import so.alaz.cleansingpotions.platform.services.IPlatformHelper;

public final class Services {
    public static final IPlatformHelper PLATFORM = createPlatform();

    private static IPlatformHelper createPlatform() {
        //? fabric {
        return new FabricPlatformHelper();
        //?}
        //? neoforge {
        /*return new NeoForgePlatformHelper();*/
        //?}
    }

    private Services() {
    }
}
