package pw.brock.mmdn.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import pw.brock.mmdn.models.IDataModel;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.util.gson.SidedOverrideSerializer;
import pw.brock.mmdn.util.gson.VersionSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * @author BrockWS
 */
public class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Version.class, new VersionSerializer(true))
            .registerTypeHierarchyAdapter(Version.SidedOverride.class, new SidedOverrideSerializer(true))
            .create();
    private static final Gson GSON_MIN = new GsonBuilder()
            .registerTypeHierarchyAdapter(Version.class, new VersionSerializer(false))
            .registerTypeHierarchyAdapter(Version.SidedOverride.class, new SidedOverrideSerializer(false))
            .create();

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T fromJsonFile(File file, Class<T> clazz) throws IOException, JsonSyntaxException {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, clazz);
        }
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static String toJsonMin(Object obj) {
        if (obj instanceof IDataModel)
            ((IDataModel) obj).prepareForMinify();
        return GSON_MIN.toJson(obj);
    }

    public static void toJsonFile(File file, Object obj) throws IOException, JsonSyntaxException {
        toJsonFile(file, obj, false);
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

    public static Gson gson() {
        return GSON;
    }

    public static Gson gsonMin() {
        return GSON_MIN;
    }
}
