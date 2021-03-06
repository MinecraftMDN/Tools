package pw.brock.mmdn.detector;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import pw.brock.mmdn.util.MavenUtil;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
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
        List<String> ignoreData = Arrays.asList(data.getOrDefault("ignoreData", "").split(","));
        MavenMeta meta = Downloader.getXml(Downloader.buildUrl(url, MavenUtil.toMavenPathBase(id), "maven-metadata.xml"), MavenMeta.class);
        List<String> newVersions = meta.versions()
                .stream()
                .filter(s -> updateExisting || !existingVersions.contains(s))
                .filter(s -> !s.endsWith("SNAPSHOT"))
                .collect(Collectors.toList());

        newVersions.forEach(s -> {
            Log.debug("Need to get information of {}", s);
            // <groupId>:<artifactId>[:<classifier>]:<version>[@extension]
            String urlBase = MavenUtil.toMavenUrl(url, id + ":" + s + "@pom").replace(".pom", "");
            MavenPom pom = Downloader.getXml(Downloader.buildUrl(urlBase + ".pom"), MavenPom.class);
            if (pom == null) {
                Log.error("Pom is null!");
                return;
            }
            Version v = pack.type.equalsIgnoreCase("modloader") ? new MLVersion() : new Version();
            v.id = pom.version;

            // Dependencies
            List<MavenPom.Dependency> dependencies = pom.dependencies();
            if (v instanceof MLVersion && !dependencies.isEmpty() && !ignoreData.contains("dependencies")) {
                Log.info("Found {} dependencies", dependencies.size());
                MLVersion mlv = (MLVersion) v;
                dependencies.forEach(dependency -> {
                    Log.trace("dependency group {} artifact {} version {} scope {}", dependency.groupId, dependency.artifactId, dependency.version, dependency.scope);
                    MLVersion.Library library = new MLVersion.Library();
                    library.id = dependency.groupId + ":" + dependency.artifactId + ":" + dependency.version;
                    library.url = data.get("url"); // We will assume the maven repo we are looking at contains the libraries instead, since there's no way for us to know otherwise.
                    // TODO: Add library hashes
                    mlv.libraries.add(library);
                });
            }

            // Artifacts
            if (!ignoreData.contains("artifacts")) {
                HttpResponse headResponse = Downloader.head(Downloader.buildUrl(urlBase + ".jar"));
                Preconditions.checkNotNull(headResponse, "Error for HEAD request!");
                HttpHeaders headers = headResponse.getHeaders();
                if (!headResponse.isSuccessStatusCode()) {
                    Log.error("{} for {} is missing {}", s, pack.id, headResponse.getRequest().getUrl().build());
                    return;
                }
                v.artifacts.add(new Version.Artifact("direct", Downloader.combineUrl(urlBase + ".jar")));

                // Hashes
                if (!ignoreData.contains("hashes")) {
                    String md5 = Downloader.getString(Downloader.buildUrl(urlBase + ".jar.md5"));
                    String sha1 = Downloader.getString(Downloader.buildUrl(urlBase + ".jar.sha1"));
                    if (md5.isEmpty())
                        throw new NullPointerException("MD5 is empty!");
                    if (sha1.isEmpty())
                        throw new NullPointerException("SHA1 is empty!");
                    v.hashes.put("md5", md5);
                    v.hashes.put("sha1", sha1);
                }
                if (!ignoreData.contains("size"))
                    v.size = headers.getContentLength();
                if (!ignoreData.contains("releaseTime")) {
                    ZonedDateTime d = ZonedDateTime.parse(headers.getLastModified(), DateTimeFormatter.RFC_1123_DATE_TIME);
                    v.releaseTime = d.toOffsetDateTime().toString();
                }
            }
            versions.add(v);
        });

        return versions;
    }
}
