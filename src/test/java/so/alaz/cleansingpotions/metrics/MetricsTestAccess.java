package so.alaz.cleansingpotions.metrics;

/** Sets the package-private FastStats flag for tests in other packages. */
public final class MetricsTestAccess {

    public static void backendActive(boolean active) {
        CleansingMetrics.backendActive(active);
    }

    private MetricsTestAccess() {
    }
}
