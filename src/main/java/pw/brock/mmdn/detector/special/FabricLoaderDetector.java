package pw.brock.mmdn.detector.special;

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
import pw.brock.mmdn.models.special.FabricLoader;
import pw.brock.mmdn.util.Downloader;
import pw.brock.mmdn.util.Log;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Special detector for Fabric Loader ONLY
 *
 * @author BrockWS
 */
@SuppressWarnings("Duplicates")
public class FabricLoaderDetector implements IDetector {

    @Override
    public List<Version> detect(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        if (!pack.id().equalsIgnoreCase("fabricloader"))
            throw new RuntimeException("Only fabricloader can use this detector!");
        // Get maven meta
        // Get all versions
        // Ignore existing versions
        // Try get .json files
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
        List<String> newVersions = meta.versions().stream()
                .filter(s -> updateExisting || !existingVersions.contains(s))
                .filter(s -> !s.endsWith("SNAPSHOT")).collect(Collectors.toList());

        newVersions.forEach(s -> {
            Log.debug("Need to get information of {}", s);
            urlParts.clear();
            urlParts.add(url);
            urlParts.addAll(Arrays.asList(split));
            urlParts.add(s);
            urlParts.add(split[split.length - 1] + "-" + s + ".json");
            String combineUrl = Downloader.combineUrl(urlParts.toArray(new String[0]));
            FabricLoader fl = Downloader.getGson(Downloader.buildUrl(combineUrl), FabricLoader.class);
            if (fl == null) {
                Log.error("json is null!");
                return;
            }
            MLVersion version = new MLVersion();
            version.id = s;
            version.mainClass = fl.mainClass();
            version.tweakers = fl.tweakers();
            fl.libraries.forEach((s1, libraries) ->
                    libraries.forEach(library -> {
                        MLVersion.Library lib = new MLVersion.Library();
                        lib.id = library.name;
                        lib.url = library.url;
                        if (!s1.equalsIgnoreCase("common"))
                            lib.side = s1;
                        // TODO: Library hashes
                        version.libraries.add(lib);
                    })
            );

            version.relationships.add(new Version.Relationship("required", "yarn"));

            String jarUrl = combineUrl.replace(".json", ".jar");
            String md5 = Downloader.getString(Downloader.buildUrl(jarUrl + ".md5"));
            String sha1 = Downloader.getString(Downloader.buildUrl(jarUrl + ".sha1"));
            if (md5.isEmpty())
                throw new NullPointerException("MD5 is empty!");
            if (sha1.isEmpty())
                throw new NullPointerException("SHA1 is empty!");
            version.artifacts.add(new Version.Artifact("direct", jarUrl));
            version.hashes.put("md5", md5);
            version.hashes.put("sha1", sha1);

            versions.add(version);
        });
        return versions;
    }
}
