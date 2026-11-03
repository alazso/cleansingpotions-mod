package so.alaz.cleansingpotions.metrics;

import dev.faststats.screen.onboarding.ConsentText;
import dev.faststats.screen.registry.FastStatsRegistry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;

import java.util.List;

// Client only. SDK 0.31.0 adds a FastStats button to the telemetry screen, and the ConsentScreen
// constructor that button calls throws when no FastStats context is registered, so a press opens nothing
// and the game logs an error.
public final class TelemetryButtonGuard {

    /**
     * Keeps that button inactive while no context is registered and active otherwise. Call after the
     * screen's init has finished and again while it stays open, because a /cleanse reload from a LAN guest
     * or a command block can stop FastStats during that time. That stop runs on the server thread, so it
     * can still land between two calls.
     */
    public static void apply(Screen screen, List<?> widgets) {
        if (!(screen instanceof TelemetryInfoScreen)) {
            return;
        }
        for (Object widget : widgets) {
            if (widget instanceof Button button && ConsentText.BUTTON.equals(button.getMessage().getString())) {
                try {
                    button.active = !FastStatsRegistry.instance().isEmpty();
                } catch (LinkageError registryGone) {
                    // Another mod can nest a different FastStats without this registry.
                    return;
                }
            }
        }
    }

    private TelemetryButtonGuard() {
    }
}
