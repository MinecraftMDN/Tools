package pw.brock.mmdn.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.fabricmc.loader.metadata.LoaderModMetadata;

import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.UpstreamPackage;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.util.JsonUtil;
import pw.brock.mmdn.util.Log;
import pw.brock.mmdn.util.Util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import pl.asie.modalyze.ModAnalyzer;
import pl.asie.modalyze.ModMetadata;

/**
 * @author BrockWS
 */
public class Analyzer {

    public static List<String> parseMinecraftVersionRange(String s, boolean onlyMainReleases) {
        List<String> mcVersions = Util.getPackageVersionIds("com.mojang.minecraft");
        if (mcVersions.contains(s))
            return Collections.singletonList(s);
        List<String> versions = new ArrayList<>();
        if (s.startsWith("[") || s.endsWith("]") || s.startsWith("(") || s.endsWith(")")) { // Assume Maven version
            try {
                VersionRange range = VersionRange.createFromVersionSpec(s);
                return mcVersions.stream()
                        .filter(s1 -> !onlyMainReleases || s1.matches("^\\d+\\.\\d+\\.*\\d*$"))
                        .map(DefaultArtifactVersion::new)
                        .filter(range::containsVersion)
                        .map(DefaultArtifactVersion::toString)
                        .collect(Collectors.toList());
            } catch (InvalidVersionSpecificationException e) {
                e.printStackTrace();
            }
        } else if (s.contains(",")) { // Assume comma separated
            String[] split = s.split(",");
            for (String s1 : split) {
                if (!mcVersions.contains(s1))
                    throw new RuntimeException("Unknown Minecraft Version " + s1);
                if (!onlyMainReleases || s1.matches("^\\d+\\.\\d+\\.*\\d*$"))
                    versions.add(s1);
            }
        }
        return versions;
    }

    public static Pair<UpstreamPackage, Version> analyzeFile(File file) {
        LoaderModMetadata[] fabricData = new FabricModAnalyzer(file).analyze();
        if (fabricData != null) {
            Log.info("Analyzed {} using FabricModAnalyzer", file.getAbsolutePath());
            for (LoaderModMetadata data : fabricData) {
                Log.info(JsonUtil.gson().toJson(data));
            }
        } else {
            ModAnalyzer analyzer = new ModAnalyzer(file);
            analyzer.setIsVerbose(true);
            ModMetadata data = analyzer.analyze();
            if (!data.valid)
                throw new RuntimeException("Failed to get data using ModAnalyzer " + file.getAbsolutePath());
            Log.info("Analyzed {} using ModAnalyzer", file.getAbsolutePath());
            UpstreamPackage pack = new UpstreamPackage();
            Version version = new Version();
            pack.id = data.modid;
            pack.name = data.name;
            pack.description = data.description;
            version.id = data.version;
            version.side = data.side;
            if (data.homepage != null && !data.homepage.isEmpty()) {
                Package.ProjectLink link = new Package.ProjectLink();
                if (data.homepage.contains("curseforge.com"))
                    link.type = "curseforge";
                else if (data.homepage.contains("github.com"))
                    link.type = data.homepage.contains("issues") ? "issues" : "source";
                else
                    link.type = "website";

                link.url = data.homepage;
                pack.projectLinks.add(link);
            }
            //if (data.provides != null && !data.provides.isEmpty()) // Don't need it
            //    Log.warn("Not sure what provides is.... " + data.provides.toString());
            if (data.sha256 != null && !data.sha256.isEmpty())
                version.hashes.put("sha256", data.sha256);
            if (data.authors != null && !data.authors.isEmpty())
                pack.authors.addAll(data.authors);
            if (data.dependencies != null && !data.dependencies.isEmpty())
                data.dependencies.forEach((id, versionString) -> {

                });
            return Pair.of(pack, version);
        }
        return Pair.of(null, null);
    }
}
