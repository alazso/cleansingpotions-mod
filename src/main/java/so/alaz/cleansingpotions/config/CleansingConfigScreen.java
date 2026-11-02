package so.alaz.cleansingpotions.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import so.alaz.cleansingpotions.CleansingPotions;

public final class CleansingConfigScreen {

    private CleansingConfigScreen() {
    }

    public static Screen create(Screen parent) {
        CleansingConfig cfg = CleansingConfig.get();
        CleansingConfig defaults = new CleansingConfig();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("config.cleansingpotions.title"));
        var entry = builder.entryBuilder();

        var cleanse = builder.getOrCreateCategory(Component.translatable("config.cleansingpotions.category.cleanse"));
        cleanse.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.only_thrower"), cfg.onlyThrower)
            .setTooltip(Component.translatable("config.cleansingpotions.only_thrower.tooltip"))
            .setDefaultValue(defaults.onlyThrower)
            .setSaveConsumer(v -> CleansingConfig.get().onlyThrower = v).build());
        cleanse.addEntry(entry.startDoubleField(Component.translatable("config.cleansingpotions.radius"), cfg.radius)
            .setTooltip(Component.translatable("config.cleansingpotions.radius.tooltip"))
            .setMin(0.5).setMax(32.0).setDefaultValue(defaults.radius)
            .setSaveConsumer(v -> CleansingConfig.get().radius = v).build());

        var cooldown = builder.getOrCreateCategory(Component.translatable("config.cleansingpotions.category.cooldown"));
        cooldown.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.cooldown_enabled"), cfg.cooldownEnabled)
            .setDefaultValue(defaults.cooldownEnabled)
            .setSaveConsumer(v -> CleansingConfig.get().cooldownEnabled = v).build());
        cooldown.addEntry(entry.startLongField(Component.translatable("config.cleansingpotions.cooldown_seconds"), cfg.cooldownSeconds)
            .setMin(0L).setMax(3600L).setDefaultValue(defaults.cooldownSeconds)
            .setSaveConsumer(v -> CleansingConfig.get().cooldownSeconds = v).build());

        var recipes = builder.getOrCreateCategory(Component.translatable("config.cleansingpotions.category.recipes"));
        //? if >=26.3 {
        /*recipes.addEntry(entry.startTextDescription(
            Component.translatable("config.cleansingpotions.recipes.note").withStyle(ChatFormatting.GRAY)).build());
        *///?}
        recipes.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.brewing_enabled"), cfg.brewingEnabled)
            .setTooltip(Component.translatable("config.cleansingpotions.brewing_enabled.tooltip"))
            .setDefaultValue(defaults.brewingEnabled)
            .setSaveConsumer(v -> CleansingConfig.get().brewingEnabled = v).build());
        recipes.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.splash_enabled"), cfg.splashEnabled)
            .setTooltip(Component.translatable("config.cleansingpotions.splash_enabled.tooltip"))
            .setDefaultValue(defaults.splashEnabled)
            .setSaveConsumer(v -> CleansingConfig.get().splashEnabled = v).build());
        recipes.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.lingering_enabled"), cfg.lingeringEnabled)
            .setTooltip(Component.translatable("config.cleansingpotions.lingering_enabled.tooltip"))
            .setDefaultValue(defaults.lingeringEnabled)
            .setSaveConsumer(v -> CleansingConfig.get().lingeringEnabled = v).build());

        var effects = builder.getOrCreateCategory(Component.translatable("config.cleansingpotions.category.effects"));
        effects.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.cleanse_feedback"), cfg.cleanseFeedback)
            .setTooltip(Component.translatable("config.cleansingpotions.cleanse_feedback.tooltip"))
            .setDefaultValue(defaults.cleanseFeedback)
            .setSaveConsumer(v -> CleansingConfig.get().cleanseFeedback = v).build());
        effects.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.impact_particles"), cfg.impactParticles)
            .setTooltip(Component.translatable("config.cleansingpotions.impact_particles.tooltip"))
            .setDefaultValue(defaults.impactParticles)
            .setSaveConsumer(v -> CleansingConfig.get().impactParticles = v).build());

        var misc = builder.getOrCreateCategory(Component.translatable("config.cleansingpotions.category.misc"));
        misc.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.permissions_enabled"), cfg.permissionsEnabled)
            .setTooltip(Component.translatable("config.cleansingpotions.permissions_enabled.tooltip"))
            .setDefaultValue(defaults.permissionsEnabled)
            .setSaveConsumer(v -> CleansingConfig.get().permissionsEnabled = v).build());
        misc.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.check_for_updates"), cfg.checkForUpdates)
            .setTooltip(Component.translatable("config.cleansingpotions.check_for_updates.tooltip"))
            .setDefaultValue(defaults.checkForUpdates)
            .setSaveConsumer(v -> CleansingConfig.get().checkForUpdates = v).build());
        misc.addEntry(entry.startBooleanToggle(Component.translatable("config.cleansingpotions.metrics"), cfg.metrics)
            .setTooltip(Component.translatable("config.cleansingpotions.metrics.tooltip"))
            .setDefaultValue(defaults.metrics)
            .setSaveConsumer(v -> CleansingConfig.get().metrics = v).build());
        misc.addEntry(entry.startTextDescription(
            Component.translatable("config.cleansingpotions.misc.note").withStyle(ChatFormatting.GRAY)).build());

        builder.setSavingRunnable(() -> {
            ConfigManager.save();
            CleansingPotions.applyMetricsToggle();
        });
        return builder.build();
    }
}
