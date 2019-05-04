package pw.brock.mmdn.meta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pw.brock.mmdn.models.MLVersion;
import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.util.FileUtil;
import pw.brock.mmdn.util.Log;
import pw.brock.mmdn.util.Util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * @author BrockWS
 */
public class MetaGenerator implements Runnable {

    private String metaDir;
    // To many maps....
    private Map<String, SourceType> sources;
    private Map<String, List<String>> existingVersions = new HashMap<>();
    private Map<String, List<String>> packageVersions = new HashMap<>();
    private Map<String, List<String>> frozenVersions = new HashMap<>();
    private Map<String, String> packagesJson = new HashMap<>();
    private Map<String, Map<String, String>> versionsJson = new HashMap<>();

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
        Preconditions.checkArgument(file.isDirectory(), "Path " + file.getAbsolutePath() + " is not a directory! " + type);

        this.sources.put(file.getAbsolutePath(), type);
        return this;
    }

    @Override
    public void run() {
        Preconditions.checkArgument(!this.sources.isEmpty(), "No sources added!");
        this.sources.forEach((s, sourceType) -> Log.info("Using {} as {}", s, sourceType));
        Log.info("Using {} as output", this.metaDir);
        existingVersions.clear();
        packageVersions.clear();
        frozenVersions.clear();
        this.readExisting(this.metaDir, this.existingVersions);
        this.sources.forEach(this::processSource);
        Log.info("Processing Upstream Packages!");
        this.processUpstreamPackages(this.existingVersions, this.packageVersions, this.frozenVersions);
        this.generateIndexes();
    }

    private void readExisting(String path, Map<String, List<String>> map) {
        FileUtil.getDirs(path, file -> !file.getName().equalsIgnoreCase(".git")).forEach(dir -> {
            String id = dir.getName();
            List<String> versions = map.computeIfAbsent(id, s -> new ArrayList<>());
            FileUtil.getFiles(dir, file ->
                    !file.getName().equalsIgnoreCase("package.json") &&
                            !file.getName().equalsIgnoreCase("index.json")
            ).forEach(file -> {
                versions.add(file.getName().replace(".json", ""));
            });
        });
    }

    private void processSource(String path, SourceType type) {
        Map<String, List<String>> map =
                type.toString().toLowerCase().contains("frozen") ?
                        frozenVersions : packageVersions;
        FileUtil.getDirs(path, file -> !file.getName().equalsIgnoreCase(".git")).forEach(file -> {
            List<String> versions = map.computeIfAbsent(file.getName(), s -> new ArrayList<>());
            this.processSourcePackage(file, versions);
        });
    }

    private void processSourcePackage(File path, List<String> versions) {
        this.packagesJson.computeIfAbsent(path.getName(), s -> {
            File packageFile = FileUtil.file(path.getAbsolutePath(), "package.json");
            if (packageFile.exists())
                return packageFile.getAbsolutePath();
            Log.debug("Package file is missing from {}", packageFile.getAbsolutePath());
            return null;
        });
        Map<String, String> versionJsons = this.versionsJson.computeIfAbsent(path.getName(), s -> new HashMap<>());
        List<File> files = FileUtil.getFiles(path, file -> !file.getName().equalsIgnoreCase("package.json"));
        files.forEach(file -> {
            String v = file.getName().replace(".json", "");
            versions.add(v);
            versionJsons.put(v, file.getAbsolutePath());
        });
    }

    private void processUpstreamPackages(Map<String, List<String>> existing, Map<String, List<String>> active, Map<String, List<String>> frozen) {
        // Delete existing versions that no longer exist
        List<String> upstreamPackages = Stream.concat(active.keySet().stream(), frozen.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
        List<String> deletedPackages = new ArrayList<>();
        List<String> deletedVersions = new ArrayList<>();
        existing.forEach((id, versions) -> {
            if (upstreamPackages.contains(id)) {
                List<String> upstreamVersions = Stream.concat(
                        active.getOrDefault(id, Collections.emptyList()).stream(),
                        frozen.getOrDefault(id, Collections.emptyList()).stream())
                        .distinct()
                        .collect(Collectors.toList());
                versions.forEach(s -> {
                    if (!upstreamVersions.contains(s)) {
                        FileUtil.delete(this.metaDir, id, s);
                        Log.info("Deleting {} as it no longer exists in upstream", FileUtil.file(this.metaDir, id, s + ".json").getAbsolutePath());
                        deletedVersions.add(s);
                    }
                });
                // Remove deleted versions from existing
                versions.removeIf(deletedVersions::contains);
                deletedVersions.clear();
            } else { // Package doesn't exist in upstream anymore, so delete it
                FileUtil.delete(this.metaDir, id);
                Log.info("Deleting {} as it no longer exists in upstream", FileUtil.file(this.metaDir, id).getAbsolutePath());
                deletedPackages.add(id);
            }
        });
        // Remove deleted packages from existing
        deletedPackages.forEach(existing::remove);
        deletedPackages.clear();
        active.forEach((id, versions) -> {
            File dir = FileUtil.file(this.metaDir, id);
            if (!dir.exists() && !dir.mkdir())
                throw new RuntimeException("Failed to create directory " + FileUtil.file(this.metaDir, id).getAbsolutePath());
            if (!this.packagesJson.containsKey(id))
                throw new RuntimeException("Missing package.json file for " + id);
            // Replace package.json
            Package pack;
            try {
                pack = Util.fromJsonFile(FileUtil.file(this.packagesJson.get(id)), Package.class);
                Util.toJsonFile(FileUtil.file(this.metaDir, id, "package.json"), pack, true);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy and minify package.json for " + id, e);
            }
            Map<String, String> versionJsons = this.versionsJson.get(id);
            versions.forEach(s -> {
                try {
                    // FIXME: Expand
                    Class<? extends Version> clazz = pack.type().equalsIgnoreCase("modloader") ? MLVersion.class : Version.class;
                    Version version = Util.fromJsonFile(FileUtil.file(versionJsons.get(s)), clazz);
                    Util.toJsonFile(FileUtil.file(this.metaDir, id, s + ".json"), version, true);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy and minify "+s+".json for " + id, e);
                }
            });
        });
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
                Log.warn("Package {} was removed!", pack.id());
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
            Util.toJsonFile(indexFile, index, true);
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
            Util.toJsonFile(indexFile, index, true);
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
