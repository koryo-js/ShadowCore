package me.koryov;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ShadowCoreUpdater {
    private static final String RELEASE_API = "https://api.github.com/repos/koryo-js/ShadowCore/releases/latest";
    private static final String ASSET_NAME = "ShadowCore.jar";
    private static volatile boolean updateInProgress;

    private ShadowCoreUpdater() {}

    public static void checkAsync(JavaPlugin plugin) { checkAsync(plugin, null); }

    public static void checkAsync(JavaPlugin plugin, Player requester) {
        if (updateInProgress) return;
        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
                HttpRequest request = HttpRequest.newBuilder(URI.create(RELEASE_API))
                        .header("Accept", "application/vnd.github+json")
                        .header("User-Agent", "ShadowCore-Updater").GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    notifyResult(plugin, requester, "Không kiểm tra được GitHub Releases (HTTP " + response.statusCode() + ").");
                    return;
                }

                String json = response.body();
                String tag = find(json, "\\\"tag_name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
                Matcher asset = Pattern.compile("\\\"name\\\"\\s*:\\s*\\\""
                        + Pattern.quote(ASSET_NAME)
                        + "\\\"[\\s\\S]*?\\\"browser_download_url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
                if (tag == null || !asset.find()) {
                    notifyResult(plugin, requester, "Release mới nhất không có asset tên ShadowCore.jar.");
                    return;
                }
                String current = plugin.getDescription().getVersion();
                if (compareVersions(stripV(tag), stripV(current)) <= 0) {
                    notifyResult(plugin, requester, "ShadowCore đang ở phiên bản mới nhất (" + current + ").");
                    return;
                }

                updateInProgress = true;
                HttpRequest downloadRequest = HttpRequest.newBuilder(URI.create(asset.group(1)))
                        .header("User-Agent", "ShadowCore-Updater").GET().build();
                HttpResponse<byte[]> download = client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());
                if (download.statusCode() != 200 || download.body().length == 0) {
                    updateInProgress = false;
                    notifyResult(plugin, requester, "Tải ShadowCore.jar thất bại.");
                    return;
                }

                Path updateDir = plugin.getDataFolder().toPath().getParent().resolve("update");
                Files.createDirectories(updateDir);
                Files.write(updateDir.resolve(ASSET_NAME), download.body());
                notifyResult(plugin, requester, "Đã tải ShadowCore " + tag
                        + ". Server sẽ tắt để Paper áp dụng bản cập nhật khi khởi động lại.");
                Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);
            } catch (Exception ex) {
                updateInProgress = false;
                plugin.getLogger().warning("ShadowCore updater: " + ex.getMessage());
                notifyResult(plugin, requester, "Lỗi cập nhật: " + ex.getMessage());
            }
        });
    }

    private static void notifyResult(JavaPlugin plugin, Player requester, String message) {
        plugin.getLogger().info(message);
        if (requester != null) Bukkit.getScheduler().runTask(plugin, () -> {
            if (requester.isOnline()) requester.sendMessage("§6[ShadowCore] §e" + message);
        });
    }

    private static String find(String input, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String stripV(String version) {
        return version.startsWith("v") || version.startsWith("V") ? version.substring(1) : version;
    }

    private static int compareVersions(String left, String right) {
        String[] a = left.split("[.-]");
        String[] b = right.split("[.-]");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int av = i < a.length ? number(a[i]) : 0;
            int bv = i < b.length ? number(b[i]) : 0;
            if (av != bv) return Integer.compare(av, bv);
        }
        return 0;
    }

    private static int number(String value) {
        Matcher matcher = Pattern.compile("\\d+").matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }
}
