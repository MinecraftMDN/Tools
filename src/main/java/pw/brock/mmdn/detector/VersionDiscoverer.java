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
import pw.brock.mmdn.models.Package;
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
        Preconditions.checkArgument(FileUtil.isDir(Globals.PACKAGES_DIR), "Packages dir does not exist! " + Globals.PACKAGES_DIR);
        Preconditions.checkArgument(FileUtil.isDir(Globals.VERSIONS_DIR), "Versions dir does not exist! " + Globals.VERSIONS_DIR);
        Package pack;
        try {
            pack = Util.fromJsonFile(FileUtil.file(Globals.PACKAGES_DIR, "active", this.id + ".json"), Package.class);
        } catch (IOException e) {
            Log.error("Failed to read package file!");
            e.printStackTrace();
            return;
        }
        Preconditions.checkNotNull(pack, "Failed to read package file!");
        Preconditions.checkArgument(pack.specVersion() == 0, "Unsupported spec version " + pack.specVersion());
        Preconditions.checkArgument(!pack.detectors().isEmpty(), "Package " + this.id + " does not have any detectors!");

        List<Version> existingVersions = Globals.FRESH ? new ArrayList<>() : this.readExisting(pack.type());
        Log.info("{} existing versions", existingVersions.size());

        pack.detectors().forEach(detect -> {
            String type = detect.type().toLowerCase();
            Map<String, String> data = detect.data();
            Log.trace("Found {} detectors", type);
            IDetector detector = DetectorRegistry.getDetector(type);
            if (detector == null)
                throw new RuntimeException("Unknown detector " + type);
            List<Version> found = detector.detect(pack, data, existingVersions.stream().map(version -> version.id).collect(Collectors.toList()));
            if (found == null)
                throw new NullPointerException("Detector " + detector.getClass() + " returned null!");
            this.mergeVersions(found, existingVersions);
        });
        if (existingVersions.isEmpty()) {
            Log.info("No files found!");
            return;
        }
        File dir = FileUtil.file(Globals.VERSIONS_DIR, "active", this.id);
        if (Globals.FRESH) { // Deleted everything inside of the folder
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                Log.error("Failed to deleted {}", dir.getAbsolutePath());
                e.printStackTrace();
                return;
            }
        }
        if (!dir.exists() && !dir.mkdirs()) {
            Log.error("Failed to create {}", dir.getAbsolutePath());
            return;
        }
        existingVersions.forEach(version -> {
            Log.trace("{}", version.toString());
        });
        /*
        existingVersions.forEach(version -> {
            Log.info("Got version info {}", version);
            try {
                Util.toJsonFile(FileUtil.file(Globals.VERSIONS_DIR, "active", this.id, version.id + ".json"), version);
            } catch (IOException e) {
                Log.error("Failed to save json file!");
                e.printStackTrace();
            }
        });*/
    }

    private void mergeVersions(List<Version> from, List<Version> to) {
        Log.info("Merging versions!");
        Collection<Version> list = Stream.concat(to.stream(), from.stream()).collect(Collectors.toMap(Version::id, Function.identity(), (o1, o2) -> {
            // TODO: Merge data

            return o1;
        })).values();
        to.clear();
        to.addAll(list);
    }

    private List<Version> readExisting(String type) {
        File dir = FileUtil.file(Globals.VERSIONS_DIR, "active", this.id);
        if (!dir.exists())
            return new ArrayList<>();
        List<File> files = FileUtil.getFiles(dir);
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
