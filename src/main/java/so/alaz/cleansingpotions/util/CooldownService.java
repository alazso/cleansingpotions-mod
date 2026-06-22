package so.alaz.cleansingpotions.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownService {

    private final Map<UUID, Long> expiry = new ConcurrentHashMap<>();

    public boolean isCooling(UUID player) {
        Long until = expiry.get(player);
        return until != null && until > System.currentTimeMillis();
    }

    public long remainingSeconds(UUID player) {
        Long until = expiry.get(player);
        if (until == null) {
            return 0;
        }
        long remaining = until - System.currentTimeMillis();
        return remaining <= 0 ? 0 : (remaining + 999) / 1000;
    }

    public void start(UUID player, long seconds) {
        expiry.put(player, System.currentTimeMillis() + seconds * 1000L);
    }
}
