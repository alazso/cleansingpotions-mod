package so.alaz.cleansingpotions.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import so.alaz.cleansingpotions.metrics.MetricsTestAccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigManagerTest {

    private static final String BROKEN_WITHOUT_METRICS = """
        {"radius":
        """;
    private static final String BROKEN_WITH_METRICS = """
        {"metrics": true, "radius":
        """;

    @BeforeEach
    @AfterEach
    void reset() {
        ConfigManager.resetLoadState();
        MetricsTestAccess.backendActive(false);
        CleansingConfig.set(new CleansingConfig());
    }

    @Test
    void withoutFastStatsTheFileTextDecidesMetrics() {
        ConfigManager.applyMetricsFromText(BROKEN_WITHOUT_METRICS);
        assertThat(CleansingConfig.get().metrics).isFalse();

        ConfigManager.applyMetricsFromText(BROKEN_WITH_METRICS);
        assertThat(CleansingConfig.get().metrics).isTrue();
    }

    @Test
    void withoutFastStatsAnUnreadableFileTurnsMetricsOff() {
        ConfigManager.applyMetricsFromText(null);
        assertThat(CleansingConfig.get().metrics).isFalse();
    }

    @Test
    void whileFastStatsRunsAFailedLoadLeavesMetricsAlone() {
        MetricsTestAccess.backendActive(true);

        ConfigManager.applyMetricsFromText(BROKEN_WITHOUT_METRICS);
        assertThat(CleansingConfig.get().metrics).isTrue();

        ConfigManager.applyMetricsFromText(null);
        assertThat(CleansingConfig.get().metrics).isTrue();
    }

    @TempDir
    Path dir;

    private Path file() {
        return dir.resolve("cleansingpotions.json");
    }

    private Path backup() {
        return dir.resolve("cleansingpotions.json.bak");
    }

    /** Metrics are off first so that the failed load queues nothing with ErrorReporter. */
    private boolean load() {
        CleansingConfig.get().metrics = false;
        return ConfigManager.load(this::file);
    }

    private void save() {
        ConfigManager.save(this::file);
    }

    @Test
    void aSaveAfterALoadKeepsAHandEdit() throws IOException {
        Files.writeString(file(), """
            {"forceKeep": ["minecraft:night_vision"], "radius": 6.0}
            """);

        assertThat(load()).isTrue();
        CleansingConfig.get().radius = 8.0;
        save();

        assertThat(Files.readString(file())).contains("minecraft:night_vision").contains("8.0");
        assertThat(backup()).doesNotExist();
    }

    @Test
    void aFailedLoadLeavesTheFileUntilASaveKeepsItAsABackup() throws IOException {
        Files.writeString(file(), BROKEN_WITHOUT_METRICS);

        assertThat(load()).isFalse();
        assertThat(file()).hasContent(BROKEN_WITHOUT_METRICS);
        assertThat(backup()).doesNotExist();

        save();

        assertThat(backup()).hasContent(BROKEN_WITHOUT_METRICS);
        assertThat(load()).isTrue();
    }

    @Test
    void onlyTheFirstSaveAfterAFailedLoadWritesTheBackup() throws IOException {
        Files.writeString(file(), BROKEN_WITHOUT_METRICS);
        load();

        save();
        Files.writeString(backup(), "kept");
        save();

        assertThat(backup()).hasContent("kept");
    }

    @Test
    void aSuccessfulLoadCancelsTheBackup() throws IOException {
        Files.writeString(file(), BROKEN_WITHOUT_METRICS);
        load();

        Files.writeString(file(), "{}");
        assertThat(load()).isTrue();
        save();

        assertThat(backup()).doesNotExist();
    }

    @Test
    void aFailedBackupStopsTheSaveAndIsRetried() throws IOException {
        Files.writeString(file(), BROKEN_WITHOUT_METRICS);
        load();
        Path blocker = Files.createDirectories(backup()).resolve("blocker");
        Files.writeString(blocker, "");

        save();
        assertThat(file()).hasContent(BROKEN_WITHOUT_METRICS);

        Files.delete(blocker);
        Files.delete(backup());
        save();
        assertThat(backup()).hasContent(BROKEN_WITHOUT_METRICS);
        assertThat(load()).isTrue();
    }

    @Test
    void aMissingFileIsCreatedWithNoBackup() {
        assertThat(load()).isTrue();

        assertThat(file()).exists();
        assertThat(backup()).doesNotExist();
        assertThat(CleansingConfig.get().forceKeep).isEqualTo(List.of("minecraft:conduit_power"));
    }
}
