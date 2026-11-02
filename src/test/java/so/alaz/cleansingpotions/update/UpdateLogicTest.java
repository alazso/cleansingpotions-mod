package so.alaz.cleansingpotions.update;

import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static so.alaz.cleansingpotions.update.UpdateLogic.RemoteVersion;

class UpdateLogicTest {

    @Test
    void noUpdateWhenNothingNewer() {
        List<RemoteVersion> remote = List.of(
            new RemoteVersion("0.2.0+1.21.9-1.21.11", ""),
            new RemoteVersion("0.1.3+1.21.9-1.21.11", "[@bug:critical] old"));
        assertThat(UpdateLogic.evaluate("0.2.0+1.21.9-1.21.11", remote)).isEmpty();
    }

    @Test
    void detectsNewerVersionAndStripsBuildMetadata() {
        List<RemoteVersion> remote = List.of(new RemoteVersion("0.3.0+26.1.2", "tidy release"));
        var info = UpdateLogic.evaluate("0.2.0+1.21.9-1.21.11", remote);
        assertThat(info).isPresent();
        assertThat(info.get().latestVersion()).isEqualTo("0.3.0");
        assertThat(info.get().bugLevel()).isNull();
    }

    @Test
    void picksHighestBugLevelAcrossNewerVersions() {
        List<RemoteVersion> remote = List.of(
            new RemoteVersion("0.2.1+x", "> [@bug:medium]\n- a fix"),
            new RemoteVersion("0.2.2+x", "[@bug:critical] urgent"),
            new RemoteVersion("0.2.3+x", "features only, no marker"));
        var info = UpdateLogic.evaluate("0.2.0", remote);
        assertThat(info).isPresent();
        assertThat(info.get().latestVersion()).isEqualTo("0.2.3");
        assertThat(info.get().bugLevel()).isEqualTo("critical");
    }

    @Test
    void ignoresMarkersOnOlderOrEqualVersions() {
        List<RemoteVersion> remote = List.of(
            new RemoteVersion("0.1.3+x", "[@bug:critical] old fix"),
            new RemoteVersion("0.2.0+x", "current"));
        assertThat(UpdateLogic.evaluate("0.2.0", remote)).isEmpty();
    }

    @Test
    void markerIsCaseInsensitiveAndOptional() {
        assertThat(UpdateLogic.highestBug("[@BUG:High]")).isEqualTo("high");
        assertThat(UpdateLogic.highestBug("nothing to see")).isNull();
    }

    @Test
    void queryUrlEncodesLoaderAndGameVersionAsJsonArrays() {
        assertThat(UpdateLogic.queryUrl("fabric", "26.2")).isEqualTo(
            "https://api.modrinth.com/v2/project/gZfP9OGb/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%2226.2%22%5D");
    }

    @Test
    void queryUrlDecodesToTheValuesItWasGiven() {
        String query = UpdateLogic.queryUrl("neoforge", "26.3");
        query = query.substring(query.indexOf('?') + 1);
        assertThat(URLDecoder.decode(query, StandardCharsets.UTF_8))
            .isEqualTo("loaders=[\"neoforge\"]&game_versions=[\"26.3\"]");
    }

    private static String decodedQuery(String loader, String game) {
        String url = UpdateLogic.queryUrl(loader, game);
        return URLDecoder.decode(url.substring(url.indexOf('?') + 1), StandardCharsets.UTF_8);
    }

    @Test
    void fabricDropSendsOneGameVersion() {
        assertThat(decodedQuery("fabric", "26.3")).isEqualTo("loaders=[\"fabric\"]&game_versions=[\"26.3\"]");
    }

    @Test
    void fabricPatchReleaseAlsoSendsItsDrop() {
        assertThat(decodedQuery("fabric", "26.3.1"))
            .isEqualTo("loaders=[\"fabric\"]&game_versions=[\"26.3.1\",\"26.3\"]");
    }

    @Test
    void fabricPatchReleaseKeepsTheUrlEncoding() {
        assertThat(UpdateLogic.queryUrl("fabric", "26.3.1")).isEqualTo(
            "https://api.modrinth.com/v2/project/gZfP9OGb/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%2226.3.1%22%2C%2226.3%22%5D");
    }

    @Test
    void neoForgePatchReleaseSendsOnlyItsExactVersion() {
        assertThat(decodedQuery("neoforge", "26.3.1"))
            .isEqualTo("loaders=[\"neoforge\"]&game_versions=[\"26.3.1\"]");
    }

    @Test
    void fabricSnapshotSendsOneGameVersion() {
        assertThat(decodedQuery("fabric", "26.4-snapshot-6"))
            .isEqualTo("loaders=[\"fabric\"]&game_versions=[\"26.4-snapshot-6\"]");
    }

    @Test
    void parseSkipsEverythingButReleasesWithAVersionNumber() {
        List<RemoteVersion> parsed = UpdateLogic.parse("""
            [{"version_number":"1.0.0+x","version_type":"release","changelog":"[@bug:high]"},
             {"version_number":"1.1.0-beta.1","version_type":"beta","changelog":"b"},
             {"version_number":"1.2.0","version_type":"alpha"},
             {"version_number":"1.3.0"},
             {"version_number":"1.4.0","version_type":null},
             {"version_number":null,"version_type":"release"},
             {"version_type":"release"}]
            """);
        assertThat(parsed).containsExactly(new RemoteVersion("1.0.0+x", "[@bug:high]"));
    }

    @Test
    void parseReadsAMissingOrNullChangelogAsEmpty() {
        List<RemoteVersion> parsed = UpdateLogic.parse("""
            [{"version_number":"1.5.0","version_type":"release","changelog":null},
             {"version_number":"1.6.0","version_type":"release"}]
            """);
        assertThat(parsed).containsExactly(new RemoteVersion("1.5.0", ""), new RemoteVersion("1.6.0", ""));
    }

    @Test
    void parseReadsAnEmptyListAsNoVersions() {
        assertThat(UpdateLogic.parse("[]")).isEmpty();
    }

    @Test
    void parseThrowsOnAnythingButAnArrayOfObjects() {
        assertThatThrownBy(() -> UpdateLogic.parse("{\"error\":\"not found\"}")).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> UpdateLogic.parse("[1,2]")).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> UpdateLogic.parse("not json [")).isInstanceOf(RuntimeException.class);
    }
}
