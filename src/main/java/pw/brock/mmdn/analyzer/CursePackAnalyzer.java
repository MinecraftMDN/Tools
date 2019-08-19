package pw.brock.mmdn.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import pw.brock.mmdn.Globals;
import pw.brock.mmdn.models.MPVersion;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.models.curse.CurseAddonFile;
import pw.brock.mmdn.models.curse.CurseAddonFiles;
import pw.brock.mmdn.models.curse.CurseModpackManifest;
import pw.brock.mmdn.util.*;

import com.google.common.base.Preconditions;


/**
 * @author BrockWS
 */
public class CursePackAnalyzer implements Runnable {

    private int modpackProjectId = -1;
    private int modpackFileId = -1;
    private File zip;
    private File dir;

    public CursePackAnalyzer(int modpackProjectId, int modpackFileId) {
        this.modpackProjectId = modpackProjectId;
        this.modpackFileId = modpackFileId;
    }

    public CursePackAnalyzer(File zip) {
        this.zip = zip;
        this.dir = FileUtil.removeExtension(zip);
    }

    public CursePackAnalyzer(File zip, File dir) {
        this.zip = zip;
        this.dir = dir;
    }

    @Override
    public void run() {
        if (this.zip == null) { // Need to download the modpack files
            this.zip = CurseApi.getOrDownloadAddonFile(this.modpackProjectId, this.modpackFileId);
        }
        if (this.dir == null) {
            this.dir = FileUtil.removeExtension(this.zip);
        }
        Objects.requireNonNull(this.zip, "Modpack Path is null!");
        Objects.requireNonNull(this.dir, "Modpack Path is null!");
        Log.debug("Using {} as modpack zip", this.zip);
        Log.debug("Using {} as modpack dir", this.dir);
        if (!this.dir.exists()) {
            Log.info("Unzipping {}", this.zip);
            if (!FileUtil.unzipFile(this.zip))
                throw new RuntimeException("Failed to extract zip file " + this.zip);
        }

        // Read modpack manifest
        File manifestFile = FileUtil.file(this.dir.getAbsolutePath(), "manifest.json");
        if (!manifestFile.exists())
            throw new RuntimeException("Missing manifest.json! " + manifestFile.getAbsolutePath());
        if (!manifestFile.isFile())
            throw new RuntimeException("manifest.json is not a file! " + manifestFile.getAbsolutePath());

        CurseModpackManifest manifest;
        try {
            manifest = JsonUtil.fromJsonFile(manifestFile, CurseModpackManifest.class);
        } catch (IOException e) {
            Log.fatal("Failed to read Curseforge Modpack Manifest File!");
            e.printStackTrace();
            return;
        }
        if (manifest.manifestVersion != 1)
            throw new RuntimeException("Unknown Modpack Manifest Version! " + manifest.manifestVersion);
        CurseAddonFile curseAddonFile;
        if (this.modpackFileId < 0) {
            CurseAddonFiles curseAddonFiles = CurseApi.getAddonFiles(String.valueOf(manifest.projectID));
            Optional<CurseAddonFile> optional = curseAddonFiles.stream()
                    .filter(f -> f.fileName.contains(manifest.version)) // Maybe use the fingerprint instead
                    .findAny();
            curseAddonFile = optional.orElseThrow(() -> new RuntimeException("Unable to match " + manifest.version + " to a modpack version"));
            this.modpackFileId = curseAddonFile.id;
        } else {
            curseAddonFile = CurseApi.getAddonFile(this.modpackProjectId, this.modpackFileId);
        }
        Preconditions.checkNotNull(curseAddonFile);
        Log.info("{}", curseAddonFile);

        MPVersion version = new MPVersion();
        version.id = manifest.version;
        version.releaseType = curseAddonFile.releaseType > 0 ? "beta" : "release";
        version.artifacts.add(new Version.Artifact("direct", curseAddonFile.downloadUrl, "client"));
        version.relationships = this.parseDependencies(manifest.files);
        Version.Relationship mcRelationship = new Version.Relationship();
        mcRelationship.type = "required";
        mcRelationship.id = "com.mojang.minecraft";
        mcRelationship.version = manifest.minecraft.version;
        version.relationships.add(0, mcRelationship);

        try {
            version.client.hashes = Util.calculateCommonHashes(this.zip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        version.client.size = this.zip.length();
        version.releaseTime = curseAddonFile.fileDate;

        // Will need to combine client and server dependencies and then check sidedness

        Log.info(JsonUtil.gson().toJson(version));
    }

    private List<Version.Relationship> parseDependencies(List<CurseModpackManifest.File> dependencies) {
        List<Version.Relationship> relationships = new ArrayList<>();
        dependencies.forEach(manifestFile -> {
            // TODO: Check if already in the MMDN using murmur2. Then check if its downloaded to calculate better hashes for checking in MMDN
            File file = CurseApi.getOrDownloadAddonFile(manifestFile.projectID, manifestFile.fileID);

            String sha256;
            try {
                sha256 = Util.calculateSHA256(file);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Objects.requireNonNull(sha256, "Failed to calculate sha256 for " + file.getAbsolutePath());
            String pvid = Util.lookupHash("sha256", sha256);
            Objects.requireNonNull(pvid, "Failed to find " + manifestFile.fileID + " for project " + manifestFile.projectID);
            String[] split = pvid.split("@");

            Version.Relationship relationship = new Version.Relationship();
            relationship.type = manifestFile.required ? "required" : "recommended";
            relationship.id = split[0];
            relationship.version = split[1];
            relationship.side = "client";
            relationships.add(relationship);
        });
        return relationships;
    }
}
