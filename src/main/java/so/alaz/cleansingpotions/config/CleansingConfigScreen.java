package so.alaz.cleansingpotions.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CleansingConfigScreen {

    private CleansingConfigScreen() {
    }

    public static Screen create(Screen parent) {
        CleansingConfig cfg = CleansingConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("CleansingPotions"));
        var entry = builder.entryBuilder();

        var cleanse = builder.getOrCreateCategory(Component.literal("Cleanse"));
        cleanse.addEntry(entry.startBooleanToggle(Component.literal("Only Thrower"), cfg.onlyThrower)
            .setTooltip(Component.literal("A thrown splash cleanses only the thrower instead of an area."))
            .setSaveConsumer(v -> cfg.onlyThrower = v).build());
        cleanse.addEntry(entry.startDoubleField(Component.literal("Radius"), cfg.radius)
            .setTooltip(Component.literal("Area-of-effect radius in blocks for splash and lingering."))
            .setMin(0.0).setMax(64.0).setSaveConsumer(v -> cfg.radius = v).build());

        var cooldown = builder.getOrCreateCategory(Component.literal("Cooldown"));
        cooldown.addEntry(entry.startBooleanToggle(Component.literal("Cooldown Enabled"), cfg.cooldownEnabled)
            .setSaveConsumer(v -> cfg.cooldownEnabled = v).build());
        cooldown.addEntry(entry.startLongField(Component.literal("Cooldown Seconds"), cfg.cooldownSeconds)
            .setMin(0L).setMax(3600L).setSaveConsumer(v -> cfg.cooldownSeconds = v).build());

        var recipes = builder.getOrCreateCategory(Component.literal("Recipes"));
        recipes.addEntry(entry.startTextDescription(
            Component.literal("§7Recipe toggles take effect on game load, not on /cleanse reload.")).build());
        recipes.addEntry(entry.startBooleanToggle(Component.literal("Brewing Enabled"), cfg.brewingEnabled)
            .setSaveConsumer(v -> cfg.brewingEnabled = v).build());
        recipes.addEntry(entry.startBooleanToggle(Component.literal("Splash Enabled"), cfg.splashEnabled)
            .setSaveConsumer(v -> cfg.splashEnabled = v).build());
        recipes.addEntry(entry.startBooleanToggle(Component.literal("Lingering Enabled"), cfg.lingeringEnabled)
            .setSaveConsumer(v -> cfg.lingeringEnabled = v).build());

        var effects = builder.getOrCreateCategory(Component.literal("Effects"));
        effects.addEntry(entry.startBooleanToggle(Component.literal("Cleanse Feedback"), cfg.cleanseFeedback)
            .setTooltip(Component.literal("Sparkle particles and a chime when an entity is cleansed."))
            .setSaveConsumer(v -> cfg.cleanseFeedback = v).build());
        effects.addEntry(entry.startBooleanToggle(Component.literal("Impact Particles"), cfg.impactParticles)
            .setTooltip(Component.literal("Shatter particles when a splash or lingering potion lands."))
            .setSaveConsumer(v -> cfg.impactParticles = v).build());

        var misc = builder.getOrCreateCategory(Component.literal("Misc"));
        misc.addEntry(entry.startBooleanToggle(Component.literal("Permissions Enabled"), cfg.permissionsEnabled)
            .setTooltip(Component.literal("Server-only gate; never affects singleplayer."))
            .setSaveConsumer(v -> cfg.permissionsEnabled = v).build());
        misc.addEntry(entry.startBooleanToggle(Component.literal("Metrics"), cfg.metrics)
            .setTooltip(Component.literal("Anonymous usage metrics via FastStats (26.x only)."))
            .setSaveConsumer(v -> cfg.metrics = v).build());
        misc.addEntry(entry.startTextDescription(Component.literal(
            "§7Effect force-lists and per-potion name/color/lore are edited in config/cleansingpotions.json.")).build());

        builder.setSavingRunnable(ConfigManager::save);
        return builder.build();
    }
}
