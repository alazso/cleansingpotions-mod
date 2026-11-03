package so.alaz.cleansingpotions.metrics;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The nested FastStats mod can be rejected at load time, so the mod must link without any
 * {@code dev.faststats} class. The Fabric checks need Minecraft on the test classpath, and only the Fabric
 * nodes have it.
 */
class FastStatsIsolationTest {

    private static final String PACKAGE = "so.alaz.cleansingpotions.";
    private static final String FABRIC_ENTRYPOINT = PACKAGE + "CleansingPotionsFabric";
    private static final String NEOFORGE_ENTRYPOINT = PACKAGE + "CleansingPotionsNeoForge";

    /** Redefines the mod classes, so the JVM verifies them, and refuses every FastStats class. */
    private static final class HidingLoader extends ClassLoader {

        HidingLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("dev.faststats.")) {
                throw new ClassNotFoundException(name);
            }
            if (!name.startsWith(PACKAGE)) {
                return super.loadClass(name, resolve);
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded != null) {
                    return loaded;
                }
                try (InputStream in = getParent().getResourceAsStream(resourceOf(name))) {
                    if (in == null) {
                        throw new ClassNotFoundException(name);
                    }
                    byte[] bytes = in.readAllBytes();
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
        }
    }

    private static String resourceOf(String className) {
        return className.replace('.', '/') + ".class";
    }

    private static boolean compiled(String className) {
        return FastStatsIsolationTest.class.getClassLoader().getResource(resourceOf(className)) != null;
    }

    private static ClassLoader hidingLoader() {
        return new HidingLoader(FastStatsIsolationTest.class.getClassLoader());
    }

    @Test
    void fabricEntrypointLinksWithoutFastStats() throws ClassNotFoundException {
        assumeTrue(compiled(FABRIC_ENTRYPOINT), "the Fabric entrypoint exists only on Fabric nodes");
        Class.forName(FABRIC_ENTRYPOINT, true, hidingLoader());
    }

    @Test
    void fabricHolderFailsToLinkWithoutFastStats() {
        assumeTrue(compiled(FABRIC_ENTRYPOINT), "the Fabric holder exists only on Fabric nodes");
        assertThrows(LinkageError.class, () -> Class.forName(PACKAGE + "metrics.FabricFastStats", true, hidingLoader()));
    }

    @Test
    void entrypointAndAlwaysLoadedClassesNameNoFastStatsClass() throws IOException {
        String entrypoint = compiled(FABRIC_ENTRYPOINT) ? FABRIC_ENTRYPOINT : NEOFORGE_ENTRYPOINT;
        List<String> classes = List.of(entrypoint, PACKAGE + "CleansingPotions", PACKAGE + "metrics.ErrorReporter",
            PACKAGE + "metrics.CleansingMetrics");
        for (String className : classes) {
            try (InputStream in = FastStatsIsolationTest.class.getClassLoader().getResourceAsStream(resourceOf(className))) {
                assertThat(in).as(className).isNotNull();
                // Class names sit in the constant pool as plain ASCII.
                String constants = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
                assertThat(constants.contains("dev/faststats")).as(className + " names a FastStats class").isFalse();
            }
        }
    }
}
