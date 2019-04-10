package pw.brock.mmdn.detector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pw.brock.mmdn.Globals;
import pw.brock.mmdn.api.IDetector;
import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.models.curse.CurseAddonFile;
import pw.brock.mmdn.models.curse.CurseAddonFiles;
import pw.brock.mmdn.util.Downloader;
import pw.brock.mmdn.util.Log;

import com.google.common.base.Preconditions;

/**
 * @author BrockWS
 */
public class CurseForgeDetector implements IDetector {
/*
    public static void start() {
        Preconditions.checkArgument(!Globals.PROJECTS.isEmpty(), "Missing curseforge project ids!");
        Preconditions.checkArgument(!Globals.CURSEMETA.isEmpty(), "Missing cursemeta url!");
        //ModRepo.init();
       // VersionsRepo.init();

        List<String> ids = Arrays.asList(Globals.PROJECTS.split(","));
        ids.forEach(CurseForgeDetector::indexProject);
    }

    public static void indexProject(String id) {
        MetaMod mod = ModRepo.getMod(id);
        if (mod == null)
            throw new IllegalArgumentException("Cannot find mod with id " + id);
        AtomicReference<String> curseId = new AtomicReference<>();
        mod.detectors().forEach(indexer -> {
            if (indexer.type().equalsIgnoreCase("curseforge"))
                curseId.set(indexer.id());
        });
        if (curseId.get().isEmpty())
            throw new IllegalArgumentException("Mod " + id + " is missing curseforge id in the mod file!");
        Log.debug("Detected versions with regex {}", mod.versionRegex());
        CurseAddonFiles files = CurseForgeDetector.getFiles(curseId.get());
        Pattern pattern = Pattern.compile(mod.versionRegex());
        Log.trace("Pattern {}", pattern.pattern());
        AtomicInteger i = new AtomicInteger();
        files.forEach(curseAddonFile -> {
            String fileName = curseAddonFile.fileNameOnDisk;
            Log.trace("Checking {} file {}", i.getAndIncrement(), fileName);
            Matcher m = pattern.matcher(fileName);
            if (!m.find()) {
                Log.error("File {} does not match regex {}! Ignoring file...", fileName, pattern.pattern());
                return;
            }
            Versions v = Versions.createVersion(m.group(1));
            v.addDownload("curseforge", String.valueOf(curseAddonFile.id));
            String gameVersion = !curseAddonFile.gameVersion.isEmpty() ? curseAddonFile.gameVersion.get(0) : null;
            v.addDependency("minecraft", "minecraft", gameVersion, gameVersion, null);
            try {
                File path = new File(Globals.VERSIONS_DIR, "active" + File.separator + id);
                path.mkdirs();
                Util.toJsonFile(new File(path, v.id() + ".json"), v);
            } catch (IOException e) {
                Log.error("Failed to save version file! {}", v.id() + ".json");
                e.printStackTrace();
            }
        });
    }

    public static CurseAddonFiles getFiles(String projectId) {
        Log.debug("Getting files from {}", Globals.CURSEMETA + "/api/v3/direct/addon/" + projectId + "/files");
        return Downloader.getGson(
                new GenericUrl(Globals.CURSEMETA + "/api/v3/direct/addon/" + projectId + "/files"),
                CurseAddonFiles.class);
    }*/

    @Override
    public List<Version> detect(Package pack, Map<String, String> data, List<String> ignoredVersions) {
        Preconditions.checkArgument(data.containsKey("id"), "Missing curseforge project id!");
        List<Version> versions = new ArrayList<>();
        String id = data.get("id");
        // The default regex will match 3 or 4 digit version string followed by .jar. Capture group will only match the 3-4 digit part
        String regex = data.getOrDefault("regex", "((?:\\d+\\.){2,3}\\d+).jar");
        Log.trace("ID: {} | Regex: {}", id, regex);
        ArrayList<CurseAddonFile> files = Downloader.getGson(
                Downloader.buildUrl(Globals.CURSEMETA, "api/v3/direct/addon", id, "files"),
                CurseAddonFiles.class);
        if (files.isEmpty()) {
            Log.error("No files found for {} with id of ", pack.id(), id);
            return versions;
        }
        Log.info("Found {} files on curseforge for {}", files.size(), id);
        Pattern pattern = Pattern.compile(regex);

        files.forEach(cf -> {
            String fileName = cf.fileNameOnDisk;
            Log.debug("Checking {} from Curseforge", fileName);
            Matcher matcher = pattern.matcher(fileName);
            if (!matcher.find()) {
                Log.error("Unable to match! {} using {}", fileName, pattern.pattern());
                return;
            }
            Log.debug("groupCount {}", matcher.groupCount());
            String version = matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
            Log.info("Found {}", version);
            Version v = new Version();
            v.id = version;
            // Relationships
            Object mcVersion = cf.gameVersion.size() == 1 ? cf.gameVersion.get(0) : cf.gameVersion;
            v.relationships.add(new Version.Relationship("required", "minecraft", mcVersion));
            // TODO: CurseID to package id
            // Artifacts
            v.artifacts.add(new Version.Artifact("curseforge", String.valueOf(cf.id)));
            v.artifacts.add(new Version.Artifact("direct", cf.downloadUrl));
            // Hashes
            // Curse uses murmur2....
            versions.add(v);
        });

        return versions;
    }
}
