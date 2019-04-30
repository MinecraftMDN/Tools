package pw.brock.mmdn.detector.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pw.brock.mmdn.Globals;
import pw.brock.mmdn.api.IDetector;
import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.models.fabric.FabricMetaVersions;
import pw.brock.mmdn.util.Downloader;
import pw.brock.mmdn.util.Log;
import pw.brock.mmdn.util.Util;

import com.google.common.base.Preconditions;

/**
 * @author BrockWS
 */
public class FabricMetaDetector implements IDetector {

    private String VERSIONS_URL = Downloader.combineUrl(Globals.FABIRC_META, "versions");
    private FabricMetaVersions VERSIONS;

    @Override
    public boolean supports(Package pack) {
        return pack.id().equalsIgnoreCase(Globals.FABRIC_YARN_ID) || pack.id().equalsIgnoreCase(Globals.FABRIC_LOADER_ID);
    }

    @Override
    public List<Version> detect(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        VERSIONS = Downloader.getGson(Downloader.buildUrl(VERSIONS_URL), FabricMetaVersions.class);
        Preconditions.checkNotNull(VERSIONS);
        Preconditions.checkNotNull(VERSIONS.mappings);
        Preconditions.checkArgument(!VERSIONS.mappings.isEmpty(), "No mappings found!");
        if (pack.id().equalsIgnoreCase(Globals.FABRIC_YARN_ID))
            return this.detectYarn(pack, data, existingVersions, updateExisting);
        if (pack.id().equalsIgnoreCase(Globals.FABRIC_LOADER_ID))
            return this.detectLoader(pack, data, existingVersions, updateExisting);
        return new ArrayList<>();
    }

    private List<Version> detectYarn(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        List<Version> versions = new ArrayList<>();
        VERSIONS.mappings.forEach(m -> {
            if (!updateExisting && existingVersions.contains(m.version))
                return;
            Log.info("New version of {} found: {}", pack.id(), m.version);
            Version version = new Version();
            version.id = m.version;
            version.releaseType = m.stable ? "release" : "beta";
            version.relationships.add(new Version.Relationship("required", "minecraft", m.gameVersion));
            version.artifacts.add(new Version.Artifact("maven", m.maven));
            version.artifacts.add(new Version.Artifact("direct", Util.toMavenUrl("https://maven.fabricmc.net/", m.maven)));
            versions.add(version);
        });
        return versions;
    }

    private List<Version> detectLoader(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        List<Version> versions = new ArrayList<>();
        VERSIONS.loader.forEach(m -> {
            if (!updateExisting && existingVersions.contains(m.version))
                return;
            Log.info("New version of {} found: {}", pack.id(), m.version);
            Version version = new Version();
            version.id = m.version;
            version.releaseType = m.stable ? "release" : "beta";
            version.relationships.add(new Version.Relationship("required", "yarn"));
            version.artifacts.add(new Version.Artifact("maven", m.maven));
            version.artifacts.add(new Version.Artifact("direct", Util.toMavenUrl("https://maven.fabricmc.net/", m.maven)));
            versions.add(version);
        });
        return versions;
    }
}
