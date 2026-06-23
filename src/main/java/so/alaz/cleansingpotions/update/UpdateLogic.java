package so.alaz.cleansingpotions.update;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure version-comparison and changelog-marker logic, with no Minecraft or network
 * dependencies so it can be unit tested. The IO and toast live in UpdateNotifier.
 */
public final class UpdateLogic {

    public record RemoteVersion(String version, String changelog) {
    }

    public record UpdateInfo(String latestVersion, String bugLevel) {
    }

    private static final Pattern BUG_MARKER = Pattern.compile("\\[@bug:(critical|high|medium)]", Pattern.CASE_INSENSITIVE);
    private static final List<String> LEVELS = List.of("medium", "high", "critical");

    private UpdateLogic() {
    }

    public static Optional<UpdateInfo> evaluate(String currentVersion, List<RemoteVersion> remote) {
        int[] current = semver(currentVersion);
        String latest = null;
        int[] latestParsed = null;
        String bug = null;
        for (RemoteVersion rv : remote) {
            int[] parsed = semver(rv.version());
            if (compare(parsed, current) <= 0) {
                continue;
            }
            if (latestParsed == null || compare(parsed, latestParsed) > 0) {
                latest = stripBuildMetadata(rv.version());
                latestParsed = parsed;
            }
            bug = higher(bug, highestBug(rv.changelog()));
        }
        return latest == null ? Optional.empty() : Optional.of(new UpdateInfo(latest, bug));
    }

    public static String highestBug(String changelog) {
        if (changelog == null) {
            return null;
        }
        String best = null;
        Matcher m = BUG_MARKER.matcher(changelog);
        while (m.find()) {
            best = higher(best, m.group(1).toLowerCase());
        }
        return best;
    }

    private static String higher(String a, String b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return LEVELS.indexOf(b) > LEVELS.indexOf(a) ? b : a;
    }

    static int[] semver(String version) {
        String core = stripBuildMetadata(version);
        String[] parts = core.split("\\.");
        int[] out = {0, 0, 0};
        for (int i = 0; i < 3 && i < parts.length; i++) {
            out[i] = parseLeadingInt(parts[i]);
        }
        return out;
    }

    private static String stripBuildMetadata(String version) {
        String v = version == null ? "" : version.trim();
        int plus = v.indexOf('+');
        if (plus >= 0) {
            v = v.substring(0, plus);
        }
        int dash = v.indexOf('-');
        if (dash >= 0) {
            v = v.substring(0, dash);
        }
        return v;
    }

    private static int parseLeadingInt(String s) {
        int end = 0;
        while (end < s.length() && Character.isDigit(s.charAt(end))) {
            end++;
        }
        return end == 0 ? 0 : Integer.parseInt(s.substring(0, end));
    }

    private static int compare(int[] a, int[] b) {
        for (int i = 0; i < 3; i++) {
            if (a[i] != b[i]) {
                return Integer.compare(a[i], b[i]);
            }
        }
        return 0;
    }
}
