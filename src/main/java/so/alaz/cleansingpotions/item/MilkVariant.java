package so.alaz.cleansingpotions.item;

import java.util.Locale;

public enum MilkVariant {

    REGULAR,
    SPLASH,
    LINGERING;

    public String tag() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static MilkVariant fromTag(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
