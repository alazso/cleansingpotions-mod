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

    public String tag() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Reads a value from {@link #tag()}, ignoring case. A null, blank, or unknown value reads as {@link #ALL}. */
    public static CleanseMode fromTag(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ALL;
        }
    }

    public String potionName() {
        return switch (this) {
            case ALL -> "cleansing";
            case NEGATIVE_ONLY -> "soothing";
            case POSITIVE_ONLY -> "purging";
        };
    }

    /** The potion colour as 0xRRGGBB. Must match the constant tints in assets/cleansingpotions/items/*.json. */
    public int rgb() {
        return switch (this) {
            case ALL -> 0xFFFFFF;
            case NEGATIVE_ONLY -> 0xE8A33B;
            case POSITIVE_ONLY -> 0x7E57A8;
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
