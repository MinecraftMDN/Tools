package pw.brock.mmdn.meta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pw.brock.mmdn.util.FileUtil;
import pw.brock.mmdn.util.Log;
import pw.brock.mmdn.util.Util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * @author BrockWS
 */
@SuppressWarnings("WeakerAccess")
public class MetaGenerator implements Runnable {

    private Map<String, SourceType> sources;
    private String metaDir;

    public MetaGenerator(String metaDir) {
        Preconditions.checkNotNull(metaDir, "metaDir cannot be null!");
        Preconditions.checkArgument(!metaDir.isEmpty(), "metaDir cannot be empty!");
        File file = FileUtil.file(metaDir);
        Preconditions.checkArgument(file.isDirectory(), "Path " + file.getAbsolutePath() + " is not a directory!");
        this.sources = Maps.newHashMap();
        this.metaDir = file.getAbsolutePath();
    }

    public MetaGenerator addSource(SourceType type, String path) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(path);
        Preconditions.checkArgument(!path.isEmpty());
        File file = FileUtil.file(path);
        if (type == SourceType.PACKAGES_ACTIVE || type == SourceType.PACKAGES_FROZEN || type == SourceType.VERSIONS_ACTIVE || type == SourceType.VERSIONS_FROZEN)
            Preconditions.checkArgument(file.isDirectory(), "Path " + file.getAbsolutePath() + " is not a directory! " + type);
        else
            Preconditions.checkArgument(file.isFile(), "Path " + file.getAbsolutePath() + " is not a file! " + type);
        this.sources.put(file.getAbsolutePath(), type);
        return this;
    }

    @Override
    public void run() {
        Preconditions.checkArgument(!this.sources.isEmpty(), "No sources added!");
        this.sources.forEach((s, sourceType) -> Log.info("Using {} as {}", s, sourceType));
        Log.info("Using {} as output", this.metaDir);
        // TODO
        this.generateIndexes();
    }

    private void generateIndexes() {
        List<File> files = FileUtil.getDirs(this.metaDir, file -> !file.getName().equalsIgnoreCase(".git"));
        files.forEach(file -> {
            Log.info("Found directory {}", file.getAbsolutePath());
            this.generatePackageIndex(file.getAbsolutePath());
        });
        File indexFile = FileUtil.file(this.metaDir, "index.json");
        MetaIndex oldIndex = null;
        try {
            oldIndex = Util.fromJsonFile(indexFile, MetaIndex.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MetaIndex index = oldIndex == null ? new MetaIndex(0) : oldIndex;
        Map<String, File> packages = files.stream().collect(Collectors.toMap(File::getName, o -> new File(o, "package.json")));

        List<MetaIndex.Package> verifiedPackages = new ArrayList<>();
        index.packages().forEach(pack -> {
            if (!packages.containsKey(pack.id())) {
                Log.info("Package {} was removed!", pack.id());
                return;
            }
            File file = packages.get(pack.id());
            if (!this.calculateAndVerifySHA256(file, pack.sha256()))
                return;

            Log.trace("Verified {}", pack.id());
            verifiedPackages.add(pack);
            // Remove them since we verified it
            packages.remove(pack.id());
        });
        if (packages.isEmpty()) {
            Log.info("Verified all packages!");
            return;
        }
        index.packages().removeIf(pack -> !verifiedPackages.contains(pack));
        Log.info("{} packages(s) to be added/changed", packages.size());
        packages.forEach((s, file) -> {
            try {
                String sha256 = Util.calculateSHA256(file);
                index.addPackage(s, sha256);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            Util.toJsonFile(indexFile, index);
        } catch (IOException e) {
            Log.error("Failed to save index as json {}", indexFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        Log.info("Successfully generated meta index file!");
    }

    private void generatePackageIndex(String path) {
        File indexFile = FileUtil.file(path, "index.json");
        String name = indexFile.getParentFile().getName();
        List<File> files = FileUtil.getFiles(path,
                file -> file.getName().endsWith(".json") &&
                        !file.getName().equalsIgnoreCase("index.json") &&
                        !file.getName().equalsIgnoreCase("package.json")
        );
        Map<String, File> versionNames = files.stream().collect(Collectors.toMap(o -> o.getName().substring(0, o.getName().length() - 5), o -> o));
        PackageIndex i = null;
        try {
            if (indexFile.exists())
                i = Util.fromJsonFile(indexFile, PackageIndex.class);
        } catch (IOException ignored) {
            Log.error("Failed to read existing package index. Regenerating entire index!");
        }
        PackageIndex index = i == null ? new PackageIndex(0) : i;
        // Verify existing versions
        List<PackageIndex.Version> verifiedVersions = new ArrayList<>();
        index.versions().forEach(version -> {
            if (!versionNames.containsKey(version.id())) {
                Log.info("Versions {} was removed!", version.id());
                return;
            }
            File file = versionNames.get(version.id());
            if (!this.calculateAndVerifySHA256(file, version.sha256()))
                return;
            Log.trace("Verified {} for {}", version.id(), name);
            verifiedVersions.add(version);
            // Remove them since we verified it
            versionNames.remove(version.id());
        });
        if (versionNames.isEmpty()) {
            Log.info("Verified all files of {}", name);
            return;
        }
        index.versions().removeIf(version -> !verifiedVersions.contains(version));
        Log.info("{} version(s) to be added/changed", versionNames.size());
        versionNames.forEach((s, file) -> {
            try {
                String sha256 = Util.calculateSHA256(file);
                index.addVersion(s, sha256);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            Util.toJsonFile(indexFile, index);
        } catch (IOException e) {
            Log.error("Failed to save index as json {}", indexFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        Log.info("Successfully generated index file for {}", name);
    }

    private boolean calculateAndVerifySHA256(File file, String old256) {
        try {
            String sha256 = Util.calculateSHA256(file);
            if (!sha256.equals(old256)) {
                Log.info("{} was modified!", file.getAbsolutePath());
                return false;
            }
        } catch (IOException e) {
            Log.error("Failed to calculate sha256 for {}!", file.getAbsolutePath());
            return false;
        }
        return true;
    }
}
