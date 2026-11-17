package so.alaz.cleansingpotions.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class CleansePolicy {

    private final CleanseMode mode;
    private final Set<String> forceRemove;
    private final Set<String> forceKeep;

    public CleansePolicy(CleanseMode mode, Collection<String> forceRemove, Collection<String> forceKeep) {
        this.mode = mode;
        this.forceRemove = normalize(forceRemove);
        this.forceKeep = normalize(forceKeep);
    }

    public boolean shouldRemove(String effectId, EffectDisposition disposition) {
        if (forceKeep.contains(effectId)) {
            return false;
        }
        if (forceRemove.contains(effectId)) {
            return true;
        }
        return mode.removes(disposition);
    }

    private static Set<String> normalize(Collection<String> ids) {
        Set<String> out = new HashSet<>();
        for (String id : ids) {
            if (!id.isBlank()) {
                String entry = id.trim().toLowerCase(Locale.ROOT);
                out.add(entry.indexOf(':') < 0 ? "minecraft:" + entry : entry);
            }
        }
        return Set.copyOf(out);
    }
}
