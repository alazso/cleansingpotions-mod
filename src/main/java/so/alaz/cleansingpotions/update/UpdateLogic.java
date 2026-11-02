package so.alaz.cleansingpotions.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import so.alaz.cleansingpotions.Constants;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Builds the Modrinth version query, reads its response, and compares versions and changelog markers.
 * Uses no Minecraft types and does no IO. The request and the toast live in UpdateNotifier.
 */
public final class UpdateLogic {

    public record RemoteVersion(String version, String changelog) {
    }

    public record UpdateInfo(String latestVersion, String bugLevel) {
    }

    private static final String VERSIONS_URL =
        "https://api.modrinth.com/v2/project/" + Constants.MODRINTH_ID + "/version";
    private static final Pattern BUG_MARKER = Pattern.compile("\\[@bug:(critical|high|medium)]", Pattern.CASE_INSENSITIVE);
    private static final List<String> LEVELS = List.of("medium", "high", "critical");

    private UpdateLogic() {
    }

    /**
     * The Modrinth query for versions that list this loader and any of this install's game versions.
     * Modrinth matches a game version exactly.
     *
     * <p>On Fabric, a game id with more than two dot-separated parts also asks for its first two parts:
     * 26.3.1 sends 26.3.1 and 26.3, because a Fabric jar loads on every patch release of its Minecraft
     * drop and is tagged with the drop alone. Every other case sends the game id alone. A NeoForge jar
     * pins one exact version, so a jar tagged 26.3 would refuse to load on 26.3.1.
     */
    public static String queryUrl(String loader, String game) {
        return VERSIONS_URL + "?loaders=" + jsonArray(List.of(loader))
            + "&game_versions=" + jsonArray(gameVersions(loader, game));
    }

    private static List<String> gameVersions(String loader, String game) {
        String[] parts = game.split("\\.");
        if ("fabric".equals(loader) && parts.length > 2) {
            return List.of(game, parts[0] + "." + parts[1]);
        }
        return List.of(game);
    }

    /**
     * Reads a Modrinth version list. Skips any entry with no version number, or whose {@code version_type}
     * is not {@code release}.
     *
     * @throws RuntimeException if the text is not a JSON array of objects
     */
    public static List<RemoteVersion> parse(String json) {
        List<RemoteVersion> out = new ArrayList<>();
        for (JsonElement element : JsonParser.parseString(json).getAsJsonArray()) {
            JsonObject obj = element.getAsJsonObject();
            String version = stringOrNull(obj, "version_number");
            if (version != null && "release".equals(stringOrNull(obj, "version_type"))) {
                String changelog = stringOrNull(obj, "changelog");
                out.add(new RemoteVersion(version, changelog == null ? "" : changelog));
            }
        }
        return out;
    }

    private static String stringOrNull(JsonObject obj, String key) {
        JsonElement value = obj.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    private static String jsonArray(List<String> values) {
        String quoted = values.stream().map(value -> "\"" + value + "\"").collect(Collectors.joining(","));
        return URLEncoder.encode("[" + quoted + "]", StandardCharsets.UTF_8);
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

    private static int[] semver(String version) {
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
