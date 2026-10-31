package so.alaz.cleansingpotions.config;

import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.core.CleansePolicy;
import so.alaz.cleansingpotions.item.MilkVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CleansingConfig {

    private static volatile CleansingConfig instance = new CleansingConfig();

    public static CleansingConfig get() {
        return instance;
    }

    public static void set(CleansingConfig cfg) {
        instance = cfg;
    }

    // Colour is deliberately not configurable: the item tint is picked by the model from the
    // cleanse_mode component, and a resource pack cannot read runtime config. See CleanseMode#rgb.
    public static final class Appearance {
        public String name;
        public List<String> lore;

        public Appearance() {
        }

        public Appearance(String name, List<String> lore) {
            this.name = name;
            this.lore = new ArrayList<>(lore);
        }
    }

    public Appearance cleansing = new Appearance("Potion of Cleansing",
        List.of("Clears every potion effect."));
    public Appearance soothing = new Appearance("Potion of Soothing",
        List.of("Clears harmful effects."));
    public Appearance purging = new Appearance("Potion of Purging",
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

    public boolean checkForUpdates = true;

    public boolean metrics = true;

    /**
     * Brings every value into its supported range. Radius stays within 0.5 to 32, because a vanilla area
     * effect cloud clamps its radius to 32 and discards itself below 0.5, and a non-finite radius becomes
     * the default. Cooldown stays within 0 to 3600 seconds. Both force lists end up non-null and hold no
     * null entries.
     */
    public void sanitize() {
        radius = Double.isFinite(radius) ? Math.clamp(radius, 0.5, 32.0) : 4.0;
        cooldownSeconds = Math.clamp(cooldownSeconds, 0L, 3600L);
        forceRemove = cleaned(forceRemove);
        forceKeep = cleaned(forceKeep);
    }

    private static List<String> cleaned(List<String> ids) {
        List<String> out = new ArrayList<>();
        if (ids != null) {
            ids.stream().filter(Objects::nonNull).forEach(out::add);
        }
        return out;
    }

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

    public boolean variantEnabled(MilkVariant variant) {
        return switch (variant) {
            case REGULAR -> true;
            case SPLASH -> splashEnabled;
            case LINGERING -> lingeringEnabled;
        };
    }

}
