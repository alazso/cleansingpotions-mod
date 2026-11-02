package so.alaz.cleansingpotions.metrics;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class ErrorReporterTest {

    private static final String OWN = "so.alaz.cleansingpotions.item.ModItems";
    private static final String OTHER = "com.example.othermod.Worker";

    private static RuntimeException thrownFrom(String... classNames) {
        RuntimeException error = new RuntimeException();
        StackTraceElement[] frames = new StackTraceElement[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            frames[i] = new StackTraceElement(classNames[i], "run", "Source.java", 1);
        }
        error.setStackTrace(frames);
        return error;
    }

    @Test
    void ownFrameCounts() {
        assertThat(ErrorReporter.hasOwnFrame(thrownFrom(OTHER, OWN))).isTrue();
    }

    @Test
    void classInTheRootPackageCounts() {
        assertThat(ErrorReporter.hasOwnFrame(thrownFrom("so.alaz.cleansingpotions.CleansingPotions"))).isTrue();
    }

    @Test
    void otherFramesDoNotCount() {
        assertThat(ErrorReporter.hasOwnFrame(thrownFrom(OTHER, "java.lang.Thread"))).isFalse();
    }

    @Test
    void packageThatOnlySharesThePrefixDoesNotCount() {
        assertThat(ErrorReporter.hasOwnFrame(thrownFrom("so.alaz.cleansingpotionsaddon.Worker"))).isFalse();
    }

    @Test
    void noFramesDoNotCount() {
        assertThat(ErrorReporter.hasOwnFrame(thrownFrom())).isFalse();
    }

    @Test
    void ownFrameOnlyInACauseCounts() {
        RuntimeException error = thrownFrom(OTHER);
        RuntimeException middle = thrownFrom(OTHER);
        error.initCause(middle);
        middle.initCause(thrownFrom(OWN));
        assertThat(ErrorReporter.hasOwnFrame(error)).isTrue();
    }

    @Test
    void cyclingCauseChainTerminates() {
        RuntimeException first = thrownFrom(OTHER);
        RuntimeException second = thrownFrom(OTHER);
        first.initCause(second);
        second.initCause(first);
        boolean result = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> ErrorReporter.hasOwnFrame(first));
        assertThat(result).isFalse();
    }
}
