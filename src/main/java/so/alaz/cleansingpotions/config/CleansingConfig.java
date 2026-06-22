package so.alaz.cleansingpotions.config;

import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.CleansePolicy;
import so.alaz.cleansingpotions.item.MilkVariant;

import java.util.ArrayList;
import java.util.List;

public final class CleansingConfig {

    private static volatile CleansingConfig instance = new CleansingConfig();

    public static CleansingConfig get() {
        return instance;
    }

    public static void set(CleansingConfig cfg) {
        instance = cfg;
    }

    public static final class Appearance {
        public String name;
        public String color;
        public List<String> lore;

        public Appearance() {
        }

        public Appearance(String name, String color, List<String> lore) {
            this.name = name;
            this.color = color;
            this.lore = new ArrayList<>(lore);
        }
    }

    public Appearance cleansing = new Appearance("Potion of Cleansing", "#FFFFFF",
        List.of("Clears every potion effect."));
    public Appearance soothing = new Appearance("Potion of Soothing", "#E8A33B",
        List.of("Clears harmful effects."));
    public Appearance purging = new Appearance("Potion of Purging", "#7E57A8",
        List.of("Clears beneficial effects."));

    public boolean splashEnabled = true;
    public boolean lingeringEnabled = true;

    public boolean onlyThrower = false;
    public double radius = 4.0;
    public List<String> forceRemove = new ArrayList<>(List.of("minecraft:bad_omen"));
    public List<String> forceKeep = new ArrayList<>(List.of("minecraft:conduit_power"));

    public boolean brewingEnabled = true;

    public boolean cooldownEnabled = false;
    public long cooldownSeconds = 5;

    public boolean permissionsEnabled = false;

    public boolean cleanseFeedback = true;
    public boolean impactParticles = true;

    public boolean metrics = true;

    public CleansePolicy policy(CleanseMode mode) {
        return new CleansePolicy(mode, forceRemove, forceKeep);
    }

    public Appearance appearance(CleanseMode mode) {
        return switch (mode) {
            case ALL -> cleansing;
            case NEGATIVE_ONLY -> soothing;
            case POSITIVE_ONLY -> purging;
        };
    }

    public int color(CleanseMode mode) {
        return parseColor(appearance(mode).color, defaultColor(mode));
    }

    public boolean variantEnabled(MilkVariant variant) {
        return switch (variant) {
            case REGULAR -> true;
            case SPLASH -> splashEnabled;
            case LINGERING -> lingeringEnabled;
        };
    }

    private static int defaultColor(CleanseMode mode) {
        return switch (mode) {
            case ALL -> 0xFFFFFF;
            case NEGATIVE_ONLY -> 0xE8A33B;
            case POSITIVE_ONLY -> 0x7E57A8;
        };
    }

    private static int parseColor(String hex, int fallback) {
        if (hex == null || hex.isBlank()) {
            return fallback;
        }
        try {
            String value = hex.trim().startsWith("#") ? hex.trim().substring(1) : hex.trim();
            return Integer.parseInt(value, 16) & 0xFFFFFF;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }
}
