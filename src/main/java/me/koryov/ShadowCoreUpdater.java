package me.koryov;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ShadowCoreUpdater {
    private static final String LATEST_RELEASE_PAGE =
            "https://github.com/koryo-js/ShadowCore/releases/latest";
    private static final String USER_AGENT = "ShadowCore-Updater";
    private static final String UPDATE_FILE_NAME = "ShadowCore.jar";
    private static volatile boolean updateInProgress;

    private ShadowCoreUpdater() {}

    public static void checkAsync(JavaPlugin plugin) {
        checkAsync(plugin, null);
    }

    public static void checkAsync(JavaPlugin plugin, Player requester) {
        if (updateInProgress) return;

        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build();

                // Use the public Releases webpage instead of the REST API.
                // This avoids the unauthenticated API's shared 60-requests/hour limit.
                HttpRequest releaseRequest = HttpRequest.newBuilder(URI.create(LATEST_RELEASE_PAGE))
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "text/html")
                        .GET()
                        .build();

                HttpResponse<String> releaseResponse =
                        client.send(releaseRequest, HttpResponse.BodyHandlers.ofString());

                if (releaseResponse.statusCode() != 200) {
                    notifyResult(plugin, requester,
                            "Không đọc được trang GitHub Releases (HTTP "
                                    + releaseResponse.statusCode() + ").");
                    return;
                }

                ReleaseAsset latest = findLatestAsset(releaseResponse.body());
                if (latest == null) {
                    notifyResult(plugin, requester,
                            "Không tìm thấy file .jar ShadowCore trong release mới nhất.");
                    return;
                }

                String currentVersion = plugin.getDescription().getVersion();
                if (compareVersions(stripV(latest.tag), stripV(currentVersion)) <= 0) {
                    notifyResult(plugin, requester,
                            "ShadowCore đang ở phiên bản mới nhất (" + currentVersion + ").");
                    return;
                }

                updateInProgress = true;

                HttpRequest downloadRequest = HttpRequest.newBuilder(latest.downloadUri)
                        .header("User-Agent", USER_AGENT)
                        .GET()
                        .build();

                HttpResponse<byte[]> downloadResponse =
                        client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());

                if (downloadResponse.statusCode() != 200 || downloadResponse.body().length == 0) {
                    updateInProgress = false;
                    notifyResult(plugin, requester, "Tải JAR ShadowCore thất bại.");
                    return;
                }

                String packagedVersion = readPackagedVersion(downloadResponse.body());
                if (packagedVersion == null
                        || compareVersions(stripV(packagedVersion), stripV(latest.tag)) != 0) {
                    updateInProgress = false;
                    plugin.getLogger().warning("ShadowCore updater: Release tag " + latest.tag
                            + " không khớp version trong plugin.yml của JAR ("
                            + (packagedVersion == null ? "không đọc được" : packagedVersion)
                            + "). Bỏ qua cập nhật để tránh restart loop.");
                    notifyResult(plugin, requester,
                            "Version trong JAR không khớp release; đã bỏ qua cập nhật.");
                    return;
                }

                if (compareVersions(stripV(packagedVersion), stripV(currentVersion)) <= 0) {
                    updateInProgress = false;
                    notifyResult(plugin, requester,
                            "JAR trên GitHub không mới hơn bản đang chạy; đã bỏ qua.");
                    return;
                }

                Path updateDirectory = plugin.getDataFolder().toPath()
                        .getParent().resolve("update");
                Files.createDirectories(updateDirectory);
                Files.write(updateDirectory.resolve(UPDATE_FILE_NAME), downloadResponse.body());

                notifyResult(plugin, requester,
                        "Đã tải ShadowCore " + latest.tag
                                + ". Server sẽ tắt để Paper áp dụng bản mới khi khởi động lại.");
                Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);

            } catch (Exception exception) {
                updateInProgress = false;
                plugin.getLogger().warning("ShadowCore updater: " + exception.getMessage());
                notifyResult(plugin, requester,
                        "Lỗi cập nhật ShadowCore: " + exception.getMessage());
            }
        });
    }

    private static ReleaseAsset findLatestAsset(String html) {
        Pattern assetPattern = Pattern.compile(
                "href=\\"([^\\"]*/releases/download/([^/\\"]+)/([^/\\"]+\\.jar))",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = assetPattern.matcher(html);

        while (matcher.find()) {
            String fileName = matcher.group(3);
            if (!fileName.toLowerCase().startsWith("shadowcore")) continue;

            String downloadUrl = matcher.group(1);
            if (downloadUrl.startsWith("/")) {
                downloadUrl = "https://github.com" + downloadUrl;
            }

            try {
                return new ReleaseAsset(matcher.group(2), URI.create(downloadUrl));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed links and continue looking for a valid JAR.
            }
        }

        return null;
    }

    private static void notifyResult(JavaPlugin plugin, Player requester, String message) {
        plugin.getLogger().info(message);
        if (requester != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (requester.isOnline()) {
                    requester.sendMessage("§6[ShadowCore] §e" + message);
                }
            });
        }
    }

    private static String readPackagedVersion(byte[] jarBytes) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(jarBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("plugin.yml".equals(entry.getName())) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    zip.transferTo(output);
                    String pluginYml = output.toString(java.nio.charset.StandardCharsets.UTF_8);
                    return find(pluginYml, "(?m)^version:\\s*([^\\s#]+)");
                }
            }
        }
        return null;
    }

    private static String find(String input, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String stripV(String version) {
        return version.startsWith("v") || version.startsWith("V")
                ? version.substring(1)
                : version;
    }

    private static int compareVersions(String left, String right) {
        String[] leftParts = left.split("[.-]");
        String[] rightParts = right.split("[.-]");

        for (int i = 0; i < Math.max(leftParts.length, rightParts.length); i++) {
            int leftNumber = i < leftParts.length ? number(leftParts[i]) : 0;
            int rightNumber = i < rightParts.length ? number(rightParts[i]) : 0;
            if (leftNumber != rightNumber) {
                return Integer.compare(leftNumber, rightNumber);
            }
        }
        return 0;
    }

    private static int number(String value) {
        Matcher matcher = Pattern.compile("\\d+").matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }

    private static final class ReleaseAsset {
        private final String tag;
        private final URI downloadUri;

        private ReleaseAsset(String tag, URI downloadUri) {
            this.tag = tag;
            this.downloadUri = downloadUri;
        }
    }
}
