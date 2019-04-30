package pw.brock.mmdn.detector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pw.brock.mmdn.Globals;
import pw.brock.mmdn.api.DetectorRegistry;
import pw.brock.mmdn.api.IDetector;
import pw.brock.mmdn.models.MLVersion;
import pw.brock.mmdn.models.UpstreamPackage;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.util.FileUtil;
import pw.brock.mmdn.util.Log;
import pw.brock.mmdn.util.Util;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;

/**
 * Version Discoverer will read the package file and discover, merge and save every version it can find.
 *
 * @author BrockWS
 */
public class VersionDiscoverer implements Runnable {

    private String id;

    public VersionDiscoverer(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        Log.info("Starting Version Discoverer, discovering versions for {}", this.id);
        Preconditions.checkArgument(FileUtil.isDir(Globals.UPSTREAM_DIR), "Upstream dir does not exist! " + Globals.UPSTREAM_DIR);
        UpstreamPackage pack;
        try {
            pack = Util.fromJsonFile(FileUtil.file(Globals.UPSTREAM_DIR, "active", this.id, "package.json"), UpstreamPackage.class);
        } catch (IOException e) {
            Log.error("Failed to read package file!");
            e.printStackTrace();
            return;
        }
        Preconditions.checkNotNull(pack, "Failed to read package file!");
        Preconditions.checkArgument(pack.formatVersion() == 0, "Unsupported spec version " + pack.formatVersion());
        Preconditions.checkArgument(!pack.detectors().isEmpty(), "Package " + this.id + " does not have any detectors!");

        List<Version> versions = Globals.FRESH ? new ArrayList<>() : this.readExisting(pack.type());
        Log.info("{} existing versions", versions.size());

        pack.detectors().forEach(detect -> {
            String type = detect.type().toLowerCase();
            Map<String, String> data = detect.data();
            Log.trace("Found {} detectors", type);
            IDetector detector = DetectorRegistry.getDetector(type);
            if (detector == null)
                throw new RuntimeException("Unknown detector " + type);
            List<Version> found = detector.detect(pack, data, versions.stream().map(version -> version.id).collect(Collectors.toList()), Globals.UPDATE);
            if (found == null)
                throw new NullPointerException("Detector " + detector.getClass() + " returned null!");
            this.mergeVersions(found, versions);
        });
        if (versions.isEmpty()) {
            Log.info("No files found!");
            return;
        }
        File dir = FileUtil.file(Globals.UPSTREAM_DIR, "active", this.id);
        /*if (Globals.FRESH) { // Deleted everything inside of the folder
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                Log.error("Failed to delete {}", dir.getAbsolutePath());
                e.printStackTrace();
                return;
            }
        }*/
        if (!dir.exists() && !dir.mkdirs()) {
            Log.error("Failed to create {}", dir.getAbsolutePath());
            return;
        }
        versions.forEach(version -> {
            Log.trace("{}", version.toString());
        });

        versions.forEach(version -> {
            Log.trace("Got version info {}", version);
            try {
                Util.toJsonFile(FileUtil.file(Globals.UPSTREAM_DIR, "active", this.id, version.id + ".json"), version);
            } catch (IOException e) {
                Log.error("Failed to save json file!");
                e.printStackTrace();
            }
        });
    }

    private void mergeVersions(List<Version> from, List<Version> to) {
        Log.info("Merging versions!");
        Collection<Version> list = Stream.concat(to.stream(), from.stream()).collect(Collectors.toMap(Version::id, Function.identity(), (o1, o2) -> {
            // TODO: Merge data
            // Remove any duplicate relationship data and only add new found relationships
            o2.relationships.removeIf(relationship -> o1.relationships.stream().map(r -> r.id).anyMatch(s -> relationship.id.equalsIgnoreCase(s)));
            o1.relationships.addAll(o2.relationships);
            return o1;
        })).values();
        to.clear();
        to.addAll(list);
    }

    private List<Version> readExisting(String type) {
        File dir = FileUtil.file(Globals.UPSTREAM_DIR, "active", this.id);
        if (!dir.exists())
            return new ArrayList<>();
        List<File> files = FileUtil.getFiles(dir, file -> !file.getName().equalsIgnoreCase("package.json"));
        return files.stream().map(file -> {
            try {
                Class<? extends Version> clazz = type.equalsIgnoreCase("modloader") ? MLVersion.class : Version.class;
                return Util.fromJsonFile(file, clazz);
            } catch (Exception e) {
                Log.info("Failed to read existing version file! {}", file.getAbsolutePath());
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
