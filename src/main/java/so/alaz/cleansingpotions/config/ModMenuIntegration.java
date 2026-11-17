package so.alaz.cleansingpotions.config;

//? fabric {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Mod Menu shows a config button for every factory except the one this default returns.
        return FabricLoader.getInstance().isModLoaded("cloth-config")
            ? CleansingConfigScreen::create
            : ModMenuApi.super.getModConfigScreenFactory();
    }
}
//?}
