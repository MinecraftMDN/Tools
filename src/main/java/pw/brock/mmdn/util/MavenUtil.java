package pw.brock.mmdn.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author BrockWS
 */
public class MavenUtil {
    // Based on gradle format <groupId>:<artifactId>[:<classifier>]:<version>[@extension]
    public static String toMavenUrl(String base, String maven) {
        return Downloader.combineUrl(base, toMavenPath(maven));
    }

    // Based on gradle format <groupId>:<artifactId>[:<classifier>]:<version>[@extension]
    public static String toMavenPathBase(String maven) {
        String[] split = maven.split(":");

        List<String> url = new ArrayList<>(Arrays.asList(split[0].split("\\.")));
        url.add(split[1]);

        return Downloader.combineUrl(url.toArray(new String[0]));
    }

    // Based on gradle format <groupId>:<artifactId>[:<classifier>]:<version>[@extension]
    public static String toMavenPath(String maven) {
        String[] split = maven.split(":");
        String[] versionExtension = (split.length == 3 ? split[2] : split[3]).split("@");

        List<String> url = new ArrayList<>(Arrays.asList(split[0].split("\\.")));

        String artifactId = split[1];
        url.add(artifactId);

        String version = versionExtension[0];
        String extension = versionExtension.length > 1 ? versionExtension[1] : "";
        String classifier = split.length == 4 ? split[2] : "";
        url.add(version);

        String file = artifactId + "-" + version;
        if (!classifier.isEmpty())
            file += "-" + classifier;
        file += extension.isEmpty() ? ".jar" : "." + extension;
        url.add(file);

        return Downloader.combineUrl(url.toArray(new String[0]));
    }

    // Based on gradle format <groupId>:<artifactId>[:<classifier>]:<version>[@extension]
    public static MavenObject toMavenObject(String maven) {
        String[] split = maven.split(":");
        String[] versionExtension = (split.length == 3 ? split[2] : split[3]).split("@");

        String group = split[0];
        String artifactId = split[1];
        String version = versionExtension[0];
        String extension = versionExtension.length > 1 ? versionExtension[1] : "";
        String classifier = split.length == 4 ? split[2] : "";

        return new MavenObject(group, artifactId, version, classifier, extension);
    }
}
