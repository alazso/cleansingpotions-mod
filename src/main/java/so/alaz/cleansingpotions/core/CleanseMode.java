package so.alaz.cleansingpotions.core;

import java.util.Locale;

public enum CleanseMode {

    ALL,
    NEGATIVE_ONLY,
    POSITIVE_ONLY;

    public boolean removes(EffectDisposition disposition) {
        return switch (this) {
            case ALL -> true;
            case NEGATIVE_ONLY -> disposition == EffectDisposition.HARMFUL;
            case POSITIVE_ONLY -> disposition == EffectDisposition.BENEFICIAL;
        };
    }

    public static CleanseMode fromConfig(String raw, CleanseMode fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public String tag() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CleanseMode fromTag(String raw) {
        return fromConfig(raw, null);
    }

    public String potionName() {
        return switch (this) {
            case ALL -> "cleansing";
            case NEGATIVE_ONLY -> "soothing";
            case POSITIVE_ONLY -> "purging";
        };
    }

    public static CleanseMode fromPotionName(String name) {
        if (name == null) {
            return null;
        }
        return switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "cleansing" -> ALL;
            case "soothing" -> NEGATIVE_ONLY;
            case "purging" -> POSITIVE_ONLY;
            default -> null;
        };
    }
}
