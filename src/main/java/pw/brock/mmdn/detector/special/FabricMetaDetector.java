package pw.brock.mmdn.detector.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pw.brock.mmdn.Globals;
import pw.brock.mmdn.api.IDetector;
import pw.brock.mmdn.models.MLVersion;
import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.models.fabric.FabricMetaLoaderVersion;
import pw.brock.mmdn.models.fabric.FabricMetaVersions;
import pw.brock.mmdn.util.Downloader;
import pw.brock.mmdn.util.Log;
import pw.brock.mmdn.util.MavenUtil;

import com.google.api.client.http.HttpResponse;
import com.google.common.base.Preconditions;

/**
 * @author BrockWS
 */
public class FabricMetaDetector implements IDetector {

    private String VERSIONS_URL = Downloader.combineUrl(Globals.FABIRC_META, "versions");
    private String LOADER_URL = Downloader.combineUrl(Globals.FABIRC_META, "versions", "loader");
    private FabricMetaVersions VERSIONS;

    @Override
    public List<Version> detect(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        VERSIONS = Downloader.getGson(Downloader.buildUrl(VERSIONS_URL), FabricMetaVersions.class);
        Preconditions.checkNotNull(VERSIONS);
        Preconditions.checkNotNull(VERSIONS.mappings);
        Preconditions.checkArgument(!VERSIONS.mappings.isEmpty(), "No mappings found!");
        if (pack.id.equalsIgnoreCase(Globals.FABRIC_LOADER_ID))
            return this.detectLoader(pack, data, existingVersions, updateExisting);
        if (pack.id.equalsIgnoreCase(Globals.FABRIC_YARN_ID))
            return this.detectYarn(pack, data, existingVersions, updateExisting);
        if (pack.id.equalsIgnoreCase(Globals.FABRIC_INTERMEDIARY_ID))
            return this.detectIntermediary(pack, data, existingVersions, updateExisting);
        return new ArrayList<>();
    }

    @Override
    public boolean supports(Package pack) {
        return pack.id.equalsIgnoreCase(Globals.FABRIC_LOADER_ID) ||
                pack.id.equalsIgnoreCase(Globals.FABRIC_YARN_ID) ||
                pack.id.equalsIgnoreCase(Globals.FABRIC_INTERMEDIARY_ID);
    }

    private List<Version> detectLoader(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        List<Version> versions = new ArrayList<>();
        VERSIONS.loader.forEach(m -> {
            try {
                if (!updateExisting && existingVersions.contains(m.version))
                    return;
                Log.info("New version of {} found: {}", pack.id, m.version);
                FabricMetaLoaderVersion loaderVersion = Downloader.getGson(Downloader.buildUrl(LOADER_URL, "1.14", m.version), FabricMetaLoaderVersion.class);
                MLVersion version = new MLVersion();
                version.id = m.version;
                version.releaseType = m.stable ? "release" : "beta";
                Version.Relationship yarnRelationship = new Version.Relationship();
                yarnRelationship.type = "required";
                yarnRelationship.id = "net.fabricmc.yarn";
                version.relationships.add(yarnRelationship);
                List<MLVersion.Library> libs = version.libraries;
                loaderVersion.launcher.libraries.forEach((s, libraries) -> libraries.stream().map(library -> {
                    MLVersion.Library lib = new MLVersion.Library();
                    lib.id = library.name;
                    if (library.url == null || library.url.isEmpty())
                        lib.url = "https://libraries.minecraft.net/";
                    else
                        lib.url = library.url;
                    lib.side = s;
                    this.fillLibraryDetails(lib);
                    return lib;
                }).forEach(libs::add));
                if (loaderVersion.launcher.containsKey("mainClass")) {
                    Object mainClass = loaderVersion.launcher.get("mainClass");
                    if (mainClass instanceof String) {
                        version.mainClass.put("client", (String) mainClass);
                        version.mainClass.put("server", (String) mainClass);
                    } else if (mainClass instanceof Map) {
                        version.mainClass.putAll((Map) mainClass);
                    } else {
                        Log.error("Unknown mainClass type {}", mainClass);
                    }
                }
                if (loaderVersion.launcher.launchWrapper != null) {
                    version.tweakers = loaderVersion.launcher.launchWrapper.tweakers;
                }
                version.artifacts.add(new Version.Artifact("maven", m.maven));
                version.artifacts.add(new Version.Artifact("direct", MavenUtil.toMavenUrl("https://maven.fabricmc.net/", m.maven)));
                versions.add(version);
            } catch (Exception e) {
                Log.error("Failed to get information for fabricloader {}", m.version);
                e.printStackTrace();
            }
        });
        return versions;
    }

    private List<Version> detectYarn(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        List<Version> versions = new ArrayList<>();
        VERSIONS.mappings.forEach(m -> {
            if (!updateExisting && existingVersions.contains(m.version))
                return;
            Log.info("New version of {} found: {}", pack.id, m.version);
            Version version = new Version();
            version.id = m.version;
            version.releaseType = m.stable ? "release" : "beta";
            Version.Relationship relationship = new Version.Relationship();
            relationship.type = "required";
            relationship.id = "com.mojang.minecraft";
            relationship.version = m.gameVersion;
            version.relationships.add(relationship);
            version.artifacts.add(new Version.Artifact("maven", m.maven));
            version.artifacts.add(new Version.Artifact("direct", MavenUtil.toMavenUrl("https://maven.fabricmc.net/", m.maven)));
            versions.add(version);
        });
        return versions;
    }

    private List<Version> detectIntermediary(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        List<Version> versions = new ArrayList<>();
        VERSIONS.intermedairy.forEach(m -> {
            if (!updateExisting && existingVersions.contains(m.version))
                return;
            Log.info("New version of {} found: {}", pack.id, m.version);
            Version version = new Version();
            version.id = m.version;
            version.releaseType = "release";
            Version.Relationship relationship = new Version.Relationship();
            relationship.type = "required";
            relationship.id = "com.mojang.minecraft";
            relationship.version = m.gameVersion;
            version.relationships.add(relationship);
            version.artifacts.add(new Version.Artifact("maven", m.maven));
            version.artifacts.add(new Version.Artifact("direct", MavenUtil.toMavenUrl("https://maven.fabricmc.net/", m.maven)));
            versions.add(version);
        });
        return versions;
    }

    private void fillLibraryDetails(MLVersion.Library lib) {
        String jarUrl = MavenUtil.toMavenUrl(lib.url, lib.id);
        String sha1Url = MavenUtil.toMavenUrl(lib.url, lib.id + "@jar.sha1");
        String md5Url = MavenUtil.toMavenUrl(lib.url, lib.id + "@jar.md5");

        HttpResponse response = Downloader.head(Downloader.buildUrl(jarUrl));
        Preconditions.checkNotNull(response, "Failed to request " + jarUrl);
        Preconditions.checkArgument(response.isSuccessStatusCode(), "Status Code: " + response.getStatusCode() + ", Status Message: " + response.getStatusMessage());
        lib.size = response.getHeaders().getContentLength();

        Map<String, String> hashes = lib.hashes;
        try {
            hashes.put("md5", Downloader.getString(Downloader.buildUrl(md5Url)));
        } catch (Exception ignored) {
            Log.error("Failed to get md5 hash for {}", md5Url);
        }
        try {
            hashes.put("sha1", Downloader.getString(Downloader.buildUrl(sha1Url)));
        } catch (Exception ignored) {
            Log.error("Failed to get sha1 hash for {}", sha1Url);
        }

        if (hashes.getOrDefault("md5", "").isEmpty())
            Log.error("Failed to get md5 for {} from {}", lib.id, lib.url);
        if (hashes.getOrDefault("sha1", "").isEmpty())
            Log.error("Failed to get sha1 for {} from {}", lib.id, lib.url);
    }
}
