package pw.brock.mmdn.detector.special;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import pw.brock.mmdn.Globals;
import pw.brock.mmdn.api.IDetector;
import pw.brock.mmdn.models.LibraryVersion;
import pw.brock.mmdn.models.MinecraftVersion;
import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.models.mojang.MojangVersion;
import pw.brock.mmdn.models.mojang.MojangVersionManifest;
import pw.brock.mmdn.util.*;

import com.google.common.base.Preconditions;

/**
 * @author BrockWS
 */
public class MinecraftDetector implements IDetector {

    private File mcCache;
    private File versionsCache;
    private File librariesCache;
    private MojangVersionManifest manifest;
    private Pattern urlPattern = Pattern.compile("(?:packages/)(.+)(?:/)(.+)");
    private List<String> lwjglGroups = Arrays.asList("org.lwjgl", "org.lwjgl.lwjgl", "net.java.jinput", "net.java.jutils");

    public MinecraftDetector() {
        this.mcCache = new File(Globals.CACHE, "minecraft");
        this.versionsCache = new File(this.mcCache, "versions");
        this.librariesCache = new File(this.mcCache, "libraries");

        Consumer<File> fileVerify = file -> {
            if (!file.exists())
                if (!file.mkdirs())
                    throw new RuntimeException("Failed to create directory " + file.getAbsolutePath());
            if (!file.isDirectory())
                throw new RuntimeException("Directory " + file.getAbsolutePath() + " is not a directory!");
        };
        fileVerify.accept(this.mcCache);
        fileVerify.accept(this.versionsCache);
        fileVerify.accept(this.librariesCache);
    }

    @Override
    public List<Version> detect(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        // Download remote version_manifest
        this.manifest = Downloader.getGson(Downloader.buildUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json"), MojangVersionManifest.class);
        Preconditions.checkNotNull(this.manifest);
        Preconditions.checkNotNull(this.manifest.latest);
        Preconditions.checkNotNull(this.manifest.versions);
        Preconditions.checkArgument(!this.manifest.versions.isEmpty());

        this.manifest.versions.forEach(version -> {
            Matcher matcher = urlPattern.matcher(version.url);
            if (!matcher.find())
                throw new RuntimeException("Failed to find hash in URL");
            String hash = matcher.group(1);
            File versionFile = new File(this.versionsCache, matcher.group(2));
            if (versionFile.exists()) {
                try {
                    if (Util.calculateSHA1(versionFile).equalsIgnoreCase(hash)) {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.error("Verification failed!");
                if (!versionFile.delete())
                    throw new RuntimeException("Failed to delete " + versionFile.getAbsolutePath());
            }
            // Download File
            boolean success = Downloader.getFile(Downloader.buildUrl(version.url), versionFile);
            if (!success)
                throw new RuntimeException("Failed to download file!");
        });

        if (pack.id.equalsIgnoreCase("com.mojang.minecraft"))
            return this.detectMinecraft(pack, data, existingVersions, updateExisting);
        if (pack.id.equalsIgnoreCase("org.lwjgl"))
            return this.detectLwjgl(pack, data, existingVersions, updateExisting);

        return Collections.emptyList();
    }

    private List<Version> detectMinecraft(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        if (!updateExisting)
            this.manifest.versions.removeIf(version -> existingVersions.contains(version.id));
        List<Version> versions = new ArrayList<>();
        this.manifest.versions.forEach(version -> {
            Matcher matcher = urlPattern.matcher(version.url);
            if (!matcher.find())
                throw new RuntimeException("Failed to find hash in URL");
            try {
                MojangVersion mojangVersion = JsonUtil.fromJsonFile(new File(this.versionsCache, matcher.group(2)), MojangVersion.class);
                Log.info("Version: {}", mojangVersion.id);

                MinecraftVersion mcVersion = new MinecraftVersion();
                mcVersion.id = mojangVersion.id;
                mcVersion.releaseType = mojangVersion.type;
                mcVersion.releaseTime = mojangVersion.releaseTime;
                mcVersion.assetIndex.id = mojangVersion.assetIndex.id;
                mcVersion.assetIndex.url = mojangVersion.assetIndex.url;
                mcVersion.assetIndex.sha1 = mojangVersion.assetIndex.sha1;
                mcVersion.assetIndex.size = mojangVersion.assetIndex.size;
                mcVersion.assetIndex.totalSize = mojangVersion.assetIndex.totalSize;
                mcVersion.hashes = null;
                mcVersion.filename = null;
                mcVersion.launchArguments = null;
                int lastIndex = matcher.group(2).lastIndexOf(".");

                List<Version.Artifact> artifacts = mcVersion.artifacts = new ArrayList<>();
                mojangVersion.downloads.forEach((side, download) -> {
                    String fileLocation = "com.mojang:minecraft:" + side + ":" + mojangVersion.id +
                            (!download.url.endsWith(".jar") ? "@" + download.url.substring(download.url.lastIndexOf(".") + 1) : "");

                    boolean success = this.downloadAndVerifyLibrary(fileLocation, download.sha1, download.url);

                    if (side.equalsIgnoreCase("client")) {
                        mcVersion.client.size = download.size;
                        mcVersion.client.filename = "com.mojang:minecraft:client:" + matcher.group(2).substring(0, lastIndex) + "@jar";
                        mcVersion.setClientMainClass(mojangVersion.mainClass);
                        mcVersion.setClientLaunchArgs(mojangVersion.minecraftArguments);
                        try {
                            mcVersion.client.hashes = Util.calculateCommonHashes(FileUtil.file(this.librariesCache.getAbsolutePath(), MavenUtil.toMavenPath(fileLocation)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (side.equalsIgnoreCase("server")) {
                        mcVersion.server.size = download.size;
                        mcVersion.server.filename = "minecraft_server_" + matcher.group(2).substring(0, lastIndex) + ".jar";
                        mcVersion.setServerMainClass("net.minecraft.server.MinecraftServer");
                        try {
                            mcVersion.server.hashes = Util.calculateCommonHashes(FileUtil.file(this.librariesCache.getAbsolutePath(), MavenUtil.toMavenPath(fileLocation)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (success)
                        Log.info("Successfully downloaded and/or verified file!");
                    else
                        throw new RuntimeException("Failed to download file!");
                    // Add to version
                    artifacts.add(new Version.Artifact("direct", download.url, side));
                });

                List<Version.Library> libraries = mcVersion.libraries = new ArrayList<>();
                AtomicReference<String> lwjglVersionAtomic = new AtomicReference<>("");
                mojangVersion.libraries.forEach(library -> {
                    String name = library.name;
                    if (lwjglGroups.stream().anyMatch(name::startsWith)) {
                        if (lwjglVersionAtomic.get().isEmpty() && name.startsWith(lwjglGroups.get(0))) {
                            MavenObject mavenObject = MavenUtil.toMavenObject(name);
                            lwjglVersionAtomic.set(mavenObject.version);
                        }
                        return;
                    }
                    if (library.downloads.artifact != null)
                        libraries.add(this.parseLibraryArtifact(library, "", library.downloads.artifact));
                    if (library.downloads.classifiers != null)
                        library.downloads.classifiers.forEach((s, artifact) -> libraries.add(this.parseLibraryArtifact(library, s, artifact)));
                });
                String lwjglVersion = lwjglVersionAtomic.get();
                if (lwjglVersion.isEmpty())
                    throw new RuntimeException("Failed to get lwjgl version!");
                Version.Relationship lwjglRelationship = new Version.Relationship();
                lwjglRelationship.id = "org.lwjgl";
                lwjglRelationship.type = "required";
                lwjglRelationship.version = lwjglVersion;
                lwjglRelationship.side = "client";
                mcVersion.relationships.add(lwjglRelationship);

                versions.add(mcVersion);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return versions;
    }

    private List<Version> detectLwjgl(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        List<Version> versions = new ArrayList<>();
        this.manifest.versions.forEach(version -> {
            Matcher matcher = urlPattern.matcher(version.url);
            if (!matcher.find())
                throw new RuntimeException("Failed to find hash in URL");
            try {
                MojangVersion mojangVersion = JsonUtil.fromJsonFile(new File(this.versionsCache, matcher.group(2)), MojangVersion.class);
                Log.info("Checking Minecraft {} for LWJGL", mojangVersion.id);

                LibraryVersion libraryVersion = new LibraryVersion();

                List<Version.Library> libraries = libraryVersion.libraries = new ArrayList<>();
                mojangVersion.libraries
                        .stream()
                        .filter(library -> lwjglGroups.stream().anyMatch(s -> library.name.startsWith(s)))
                        .forEach(library -> {
                            if (library.downloads.artifact != null)
                                libraries.add(this.parseLibraryArtifact(library, "", library.downloads.artifact));
                            if (library.downloads.classifiers != null)
                                library.downloads.classifiers.forEach((s, artifact) -> libraries.add(this.parseLibraryArtifact(library, s, artifact)));
                        });

                libraryVersion.id = this.getLwjglVersion(libraries);
                if (libraryVersion.id.isEmpty())
                    throw new RuntimeException("Failed to get lwjgl version!");

                libraryVersion.side = "client";

                versions.add(libraryVersion);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return versions.stream().distinct().collect(Collectors.toList());
    }

    private Version.Library parseLibraryArtifact(MojangVersion.Library library, String c, MojangVersion.Library.Artifact artifact) {
        Preconditions.checkNotNull(artifact, "Artifact is null! " + library.name);
        boolean success = this.downloadAndVerifyLibrary(artifact.path, artifact.sha1, artifact.url);
        if (success)
            Log.info("Successfully downloaded and/or verified file!");
        else
            throw new RuntimeException("Failed to download file!");
        Version.Library lib = new Version.Library();
        lib.id = library.name;
        lib.url = artifact.url;
        lib.side = "client";
        lib.size = artifact.size;
        try {
            File file = new File(this.librariesCache, artifact.path);
            lib.hashes = Util.calculateCommonHashes(file);
            String name = file.getName();
            if (name.contains("javadoc")) {
                lib.type = "javadocs";
            } else if (name.contains("source") || name.contains("sources")) {
                lib.type = "sources";
            } else if (name.contains("natives")) {
                if (name.contains("windows")) {
                    lib.type = "natives-windows";
                } else if (name.contains("osx") || name.contains("macos")) {
                    lib.type = "natives-macos";
                } else if (name.contains("linux")) {
                    lib.type = "natives-linux";
                } else {
                    throw new RuntimeException("Unknown natives type for " + name);
                }
            } else {
                lib.type = "jar";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lib;
    }

    private boolean downloadAndVerifyLibrary(String name, String hash, String url) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(hash);
        Preconditions.checkNotNull(url);
        Log.info(url);
        String path = name.contains(":") ? MavenUtil.toMavenPath(name) : name;
        File file = new File(this.librariesCache, path);
        if (file.exists()) {
            try {
                if (Util.calculateSHA1(file).equalsIgnoreCase(hash)) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.error("Verification failed!");
            if (!file.delete())
                throw new RuntimeException("Failed to delete " + file.getAbsolutePath());
        } else {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
                throw new RuntimeException("Failed to make directories for " + file.getAbsolutePath());
        }
        boolean success = Downloader.getFile(Downloader.buildUrl(url), file);
        if (!success)
            throw new RuntimeException("Failed to download " + url + " to " + file.getAbsolutePath());
        return true;
    }

    private String getLwjglVersion(List<Version.Library> libraries) {
        AtomicReference<String> lwjglVersionAtomic = new AtomicReference<>("");
        libraries.stream()
                .filter(library -> lwjglGroups.stream().limit(2).anyMatch(s -> library.id.startsWith(s)))
                .forEach(library -> {
                    if (!lwjglVersionAtomic.get().isEmpty())
                        return;
                    MavenObject mavenObject = MavenUtil.toMavenObject(library.id);
                    lwjglVersionAtomic.set(mavenObject.version);
                });
        return lwjglVersionAtomic.get();
    }
}
