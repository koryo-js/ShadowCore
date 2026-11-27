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
    private static final String LATEST_RELEASE_API =
            "https://api.github.com/repos/koryo-js/ShadowCore/releases/latest";
    private static final String USER_AGENT = "ShadowCore-Updater";
    private static final String UPDATE_FILE_NAME = "ShadowCore.jar";
    private static volatile boolean updateInProgress;

    private ShadowCoreUpdater() {}

    /**
     * Starts automatic release checks every 10 minutes. The initial check is
     * still performed separately during plugin startup.
     */
    public static void startAutoChecks(JavaPlugin plugin) {
        long intervalTicks = 20L * 60L * 10L;
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> checkAsync(plugin),
                intervalTicks,
                intervalTicks
        );
        plugin.getLogger().info("ShadowCore updater: automatic checks scheduled every 10 minutes.");
    }

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

                // Read release metadata from the GitHub API. The latest-release HTML page
                // may not contain asset download links in its initial response.
                HttpRequest releaseRequest = HttpRequest.newBuilder(URI.create(LATEST_RELEASE_API))
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "application/vnd.github+json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .GET()
                        .build();

                HttpResponse<String> releaseResponse =
                        client.send(releaseRequest, HttpResponse.BodyHandlers.ofString());

                ReleaseAsset latest = null;
                if (releaseResponse.statusCode() == 200) {
                    latest = findLatestAsset(releaseResponse.body());
                }

                // GitHub may rate-limit unauthenticated API requests (HTTP 403).
                // Fall back to /releases/latest, whose redirect reveals the latest tag.
                if (latest == null) {
                    HttpRequest latestPageRequest = HttpRequest.newBuilder(
                                    URI.create("https://github.com/koryo-js/ShadowCore/releases/latest"))
                            .header("User-Agent", USER_AGENT)
                            .GET()
                            .build();
                    HttpResponse<String> latestPageResponse = client.send(
                            latestPageRequest, HttpResponse.BodyHandlers.ofString());

                    String tag = extractTagFromUri(latestPageResponse.uri());
                    if (latestPageResponse.statusCode() >= 200
                            && latestPageResponse.statusCode() < 400
                            && tag != null) {
                        URI assetUri = URI.create(
                                "https://github.com/koryo-js/ShadowCore/releases/download/"
                                        + tag + "/ShadowCore-" + tag + ".jar");
                        latest = new ReleaseAsset(tag, assetUri);
                    }
                }

                if (latest == null) {
                    notifyResult(plugin, requester,
                            "Unable to determine the latest release or find the ShadowCore JAR.");
                    return;
                }

                String currentVersion = plugin.getDescription().getVersion();
                if (compareVersions(stripV(latest.tag), stripV(currentVersion)) <= 0) {
                    notifyResult(plugin, requester,
                            "ShadowCore is already on the latest version (" + currentVersion + ").");
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
                    notifyResult(plugin, requester, "Failed to download the ShadowCore JAR.");
                    return;
                }

                String packagedVersion = readPackagedVersion(downloadResponse.body());
                if (packagedVersion == null
                        || compareVersions(stripV(packagedVersion), stripV(latest.tag)) != 0) {
                    updateInProgress = false;
                    plugin.getLogger().warning("ShadowCore updater: Release tag " + latest.tag
                            + " does not match the version in the JAR's plugin.yml ("
                            + (packagedVersion == null ? "unreadable" : packagedVersion)
                            + "). Skipping update to prevent a restart loop.");
                    notifyResult(plugin, requester,
                            "Version in the JAR does not match the release; update skipped.");
                    return;
                }

                if (compareVersions(stripV(packagedVersion), stripV(currentVersion)) <= 0) {
                    updateInProgress = false;
                    notifyResult(plugin, requester,
                            "JAR on GitHub is not newer than the version currently running; skipped.");
                    return;
                }

                Path updateDirectory = plugin.getDataFolder().toPath()
                        .getParent().resolve("update");
                Files.createDirectories(updateDirectory);
                Files.write(updateDirectory.resolve(UPDATE_FILE_NAME), downloadResponse.body());

                notifyResult(plugin, requester,
                        "Downloaded ShadowCore " + latest.tag
                                + ". Server will shut down so Paper can apply the latest version.");
                Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);

            } catch (Exception exception) {
                updateInProgress = false;
                plugin.getLogger().warning("ShadowCore updater: " + exception.getMessage());
                notifyResult(plugin, requester,
                        "Error updating ShadowCore: " + exception.getMessage());
            }
        });
    }

    private static String extractTagFromUri(URI uri) {
        String path = uri.getPath();
        if (path == null) return null;
        Matcher matcher = Pattern.compile("/releases/tag/([^/]+)").matcher(path);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static ReleaseAsset findLatestAsset(String json) {
        String tag = find(json, "\\\"tag_name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
        if (tag == null) return null;

        // Asset metadata is contained in the release JSON. Match the JAR name
        // with its browser_download_url; do not depend on the Releases page HTML.
        Pattern assetPattern = Pattern.compile(
                "\\\"name\\\"\\s*:\\s*\\\"([^\\\"]+\\.jar)\\\""
                        + "[\\s\\S]*?\\\"browser_download_url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = assetPattern.matcher(json);

        while (matcher.find()) {
            String fileName = matcher.group(1);
            if (!fileName.toLowerCase().startsWith("shadowcore")) continue;

            String downloadUrl = matcher.group(2).replace("\\\\/", "/");
            try {
                return new ReleaseAsset(tag, URI.create(downloadUrl));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed asset URLs and continue looking.
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
