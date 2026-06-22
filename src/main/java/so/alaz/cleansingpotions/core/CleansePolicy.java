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
        String id = effectId == null ? "" : effectId.toLowerCase(Locale.ROOT);
        if (forceKeep.contains(id)) {
            return false;
        }
        if (forceRemove.contains(id)) {
            return true;
        }
        return mode.removes(disposition);
    }

    public CleanseMode mode() {
        return mode;
    }

    private static Set<String> normalize(Collection<String> ids) {
        Set<String> out = new HashSet<>();
        if (ids != null) {
            for (String id : ids) {
                if (id != null && !id.isBlank()) {
                    out.add(id.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return Set.copyOf(out);
    }
}
