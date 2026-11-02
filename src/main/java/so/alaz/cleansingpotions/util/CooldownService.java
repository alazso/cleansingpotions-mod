package so.alaz.cleansingpotions.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownService {

    private final Map<UUID, Long> lastThrow = new ConcurrentHashMap<>();

    public boolean isCooling(UUID player, long seconds) {
        return remainingMillis(player, seconds) > 0;
    }

    public long remainingSeconds(UUID player, long seconds) {
        long remaining = remainingMillis(player, seconds);
        return remaining <= 0 ? 0 : (remaining + 999) / 1000;
    }

    public void start(UUID player) {
        lastThrow.put(player, System.currentTimeMillis());
    }

    public void clear() {
        lastThrow.clear();
    }

    private long remainingMillis(UUID player, long seconds) {
        Long started = lastThrow.get(player);
        return started == null ? 0 : started + seconds * 1000L - System.currentTimeMillis();
    }
}
