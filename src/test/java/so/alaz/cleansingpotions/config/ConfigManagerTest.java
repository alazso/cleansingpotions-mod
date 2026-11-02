package so.alaz.cleansingpotions.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import so.alaz.cleansingpotions.metrics.MetricsTestAccess;

import static org.assertj.core.api.Assertions.assertThat;

/** No test here loads a file, so {@link ConfigManager} stays in its state before any successful load. */
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
}
