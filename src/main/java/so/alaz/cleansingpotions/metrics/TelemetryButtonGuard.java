package so.alaz.cleansingpotions.metrics;

import dev.faststats.screen.onboarding.ConsentText;
import dev.faststats.screen.registry.FastStatsRegistry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;

import java.util.List;

// Client only. SDK 0.31.0 adds a FastStats button to the telemetry screen, and the ConsentScreen
// constructor that button calls throws when no FastStats context is registered, which crashes the game.
public final class TelemetryButtonGuard {

    /** Deactivates that button while no context is registered. Call after the screen's init has finished. */
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
