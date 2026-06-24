package so.alaz.cleansingpotions.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.platform.Services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client-only update check: on the first world join of a session, asks Modrinth whether a
 * newer version exists and, if so, shows a toast that highlights a fix for a [@bug:<level>].
 * All network work runs on a daemon thread; the toast is posted back to the client thread.
 */
public final class UpdateNotifier {

    private static final String BASE = "https://api.modrinth.com/v2/project/" + Constants.MODRINTH_ID + "/version";
    private static final AtomicBoolean RAN = new AtomicBoolean(false);

    private UpdateNotifier() {
    }

    public static void onJoin() {
        if (!RAN.compareAndSet(false, true)) {
            return;
        }
        String preview = System.getProperty("cleansingpotions.previewToast");
        if (preview != null) {
            show(new UpdateLogic.UpdateInfo("9.9.9", validLevel(preview)));
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
            String current = Services.PLATFORM.getModVersion();
            String url = BASE + "?loaders=" + enc("[\"" + loader + "\"]");
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
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
            Optional<UpdateLogic.UpdateInfo> info = UpdateLogic.evaluate(current, parse(response.body()));
            info.ifPresent(UpdateNotifier::show);
        } catch (Throwable t) {
            Constants.LOG.debug("CleansingPotions update check failed", t);
        }
    }

    static List<UpdateLogic.RemoteVersion> parse(String json) {
        List<UpdateLogic.RemoteVersion> out = new ArrayList<>();
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            if (!obj.has("version_number") || obj.get("version_number").isJsonNull()) {
                continue;
            }
            String version = obj.get("version_number").getAsString();
            String changelog = obj.has("changelog") && !obj.get("changelog").isJsonNull()
                ? obj.get("changelog").getAsString() : "";
            out.add(new UpdateLogic.RemoteVersion(version, changelog));
        }
        return out;
    }

    private static void show(UpdateLogic.UpdateInfo info) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            try {
                Component title = Component.translatable("toast.cleansingpotions.update.title");
                Component message = info.bugLevel() != null
                    ? Component.translatable("toast.cleansingpotions.update.bug", info.latestVersion(), info.bugLevel())
                    : Component.translatable("toast.cleansingpotions.update.normal", info.latestVersion());
                SystemToast.add(mc.getToastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, message);
            } catch (Throwable t) {
                // The toast API changed between 26.1.2 (our 26.x compile target) and 26.2, so the
                // call can be absent at runtime. Degrade to no toast rather than crash the client.
                Constants.LOG.warn("CleansingPotions: update toast unavailable on this version ({})", t.toString());
            }
        });
    }

    private static String validLevel(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        return (v.equals("critical") || v.equals("high") || v.equals("medium")) ? v : null;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
