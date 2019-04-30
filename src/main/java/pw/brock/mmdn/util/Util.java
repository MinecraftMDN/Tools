package pw.brock.mmdn.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pw.brock.mmdn.models.IDataModel;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author BrockWS
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class Util {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson GSON_MIN = new GsonBuilder().create();

    public static <T> T fromJsonFile(File file, Class<T> clazz) throws IOException, JsonSyntaxException {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, clazz);
        }
    }

    public static void toJsonFile(File file, Object obj) throws IOException, JsonSyntaxException {
        Util.toJsonFile(file, obj, false);
    }

    public static void toJsonFile(File file, Object obj, boolean minify) throws IOException, JsonSyntaxException {
        try (FileWriter writer = new FileWriter(file)) {
            if (minify) {
                if (obj instanceof IDataModel)
                    ((IDataModel) obj).prepareForMinify();
                GSON_MIN.toJson(obj, writer);
            } else {
                GSON.toJson(obj, writer);
            }
        }
    }

    // Based on gradle format <groupId>:<artifactId>[:<classifier>]:<version>[@extension]
    public static String toMavenUrl(String base, String maven) {
        String[] split = maven.split(":");
        String[] versionExtension = (split.length == 3 ? split[2] : split[3]).split("@");
        List<String> url = new ArrayList<>();
        url.add(base);

        url.addAll(Arrays.asList(split[0].split("\\.")));

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

    public static String calculateSHA256(File file) throws IOException {
        Preconditions.checkNotNull(file);
        String sha256 = DigestUtils.sha256Hex(new FileInputStream(file));
        if (sha256.isEmpty())
            throw new RuntimeException("Failed to calculate sha256 for " + file.getAbsolutePath());
        Log.trace("sha256 for {} is {}", file.getAbsolutePath(), sha256);
        return sha256;
    }

    public static Gson gson() {
        return Util.GSON;
    }

    public static Gson gsonMin() {
        return Util.GSON_MIN;
    }
}
