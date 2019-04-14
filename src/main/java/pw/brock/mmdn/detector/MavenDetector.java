package pw.brock.mmdn.detector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pw.brock.mmdn.api.IDetector;
import pw.brock.mmdn.models.MLVersion;
import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.models.maven.MavenMeta;
import pw.brock.mmdn.models.maven.MavenPom;
import pw.brock.mmdn.util.Downloader;
import pw.brock.mmdn.util.Log;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * @author BrockWS
 */
@SuppressWarnings("Duplicates")
public class MavenDetector implements IDetector {

    @Override
    public List<Version> detect(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        Preconditions.checkArgument(data.containsKey("url"), "Missing url!");
        Preconditions.checkArgument(data.containsKey("id"), "Missing id!");
        List<Version> versions = Lists.newArrayList();
        String id = data.get("id");
        String url = data.get("url");
        // com.example:mod
        // com.example.mod:modlib
        String[] split = id.split("[:.]");
        if (split.length < 2)
            throw new RuntimeException(id + " is not a valid id!");
        List<String> urlParts = new ArrayList<>();
        urlParts.add(url);
        urlParts.addAll(Arrays.asList(split));
        urlParts.add("maven-metadata.xml");
        MavenMeta meta = Downloader.getXml(Downloader.buildUrl(url, split[0], split[1], split[2], "maven-metadata.xml"), MavenMeta.class);
        List<String> newVersions = meta.versions()
                .stream()
                .filter(s -> updateExisting || !existingVersions.contains(s))
                .filter(s -> !s.endsWith("SNAPSHOT"))
                .collect(Collectors.toList());

        newVersions.forEach(s -> {
            Log.debug("Need to get information of {}", s);
            urlParts.clear();
            urlParts.add(url);
            urlParts.addAll(Arrays.asList(split));
            urlParts.add(s);
            urlParts.add(split[split.length - 1] + "-" + s);
            String urlBase = Downloader.combineUrl(urlParts.toArray(new String[0]));
            MavenPom pom = Downloader.getXml(Downloader.buildUrl(urlBase + ".pom"), MavenPom.class);
            if (pom == null) {
                Log.error("Pom is null!");
                return;
            }
            Version v = pack.type().equalsIgnoreCase("modloader") ? new MLVersion() : new Version();
            v.id = pom.version;

            // Dependencies
            List<MavenPom.Dependency> dependencies = pom.dependencies();
            if (v instanceof MLVersion && !dependencies.isEmpty()) {
                Log.info("Found {} dependencies", dependencies.size());
                MLVersion mlv = (MLVersion) v;
                dependencies.forEach(dependency -> {
                    //Log.info("dependency group {} artifact {} version {} scope {}", dependency.groupId, dependency.artifactId, dependency.version, dependency.scope);
                    MLVersion.Library library = new MLVersion.Library();
                    library.id = dependency.groupId + ":" + dependency.artifactId + ":" + dependency.version;
                    library.url = data.get("url"); // We will assume the maven repo we are looking at contains the libraries instead, since there's no way for us to know otherwise.
                    // TODO: Add library hashes
                    mlv.libraries.add(library);
                });
            }

            // Artifacts
            // For now we assume the jar is in the maven. TODO: Check
            // Hashes
            String md5 = Downloader.getString(Downloader.buildUrl(urlBase + ".jar.md5"));
            String sha1 = Downloader.getString(Downloader.buildUrl(urlBase + ".jar.sha1"));
            if (md5.isEmpty())
                throw new NullPointerException("MD5 is empty!");
            if (sha1.isEmpty())
                throw new NullPointerException("SHA1 is empty!");
            v.artifacts.add(new Version.Artifact("direct", Downloader.combineUrl(urlBase + ".jar")));
            v.hashes.put("md5", md5);
            v.hashes.put("sha1", sha1);
            versions.add(v);
        });

        return versions;
    }
}
