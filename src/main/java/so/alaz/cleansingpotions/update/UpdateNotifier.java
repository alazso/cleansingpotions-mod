package so.alaz.cleansingpotions.update;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.platform.Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client-only update check: on the first world join of a session, asks Modrinth whether a newer
 * release exists for this loader and game version and, if so, shows a toast that highlights a fix
 * for a [@bug:<level>].
 * All network work runs on a daemon thread; the toast is posted back to the client thread.
 */
public final class UpdateNotifier {

    private static final AtomicBoolean RAN = new AtomicBoolean(false);

    private UpdateNotifier() {
    }

    public static void onJoin() {
        if (!RAN.compareAndSet(false, true)) {
            return;
        }
        if (!CleansingConfig.get().checkForUpdates) {
            return;
        }
        Thread thread = new Thread(UpdateNotifier::run, "cleansingpotions-update-check");
        thread.setDaemon(true);
        thread.start();
    }

    private static void run() {
        try {
            String loader = Services.PLATFORM.getPlatformName().toLowerCase(Locale.ROOT);
            String game = SharedConstants.getCurrentVersion().id();
            String current = Services.PLATFORM.getModVersion();
            HttpRequest request = HttpRequest.newBuilder(URI.create(UpdateLogic.queryUrl(loader, game)))
                .header("User-Agent", "alazso/cleansingpotions/" + current)
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return;
            }
            Optional<UpdateLogic.UpdateInfo> info = UpdateLogic.evaluate(current, UpdateLogic.parse(response.body()));
            info.ifPresent(UpdateNotifier::show);
        } catch (Throwable t) {
            Constants.LOG.debug("CleansingPotions update check failed", t);
        }
    }

    private static void show(UpdateLogic.UpdateInfo info) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            try {
                Component title = Component.translatable("toast.cleansingpotions.update.title");
                Component message = info.bugLevel() != null
                    ? Component.translatable("toast.cleansingpotions.update.bug", info.latestVersion(), info.bugLevel())
                    : Component.translatable("toast.cleansingpotions.update.normal", info.latestVersion());
                SystemToast.add(mc.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, message);
            } catch (Throwable t) {
                // A hotfix inside this node's range can move the accessor, and a missing toast must not
                // crash the client.
                Constants.LOG.warn("CleansingPotions: update toast unavailable on this version ({})", t.toString());
            }
        });
    }
}
