package so.alaz.cleansingpotions.update;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
}
