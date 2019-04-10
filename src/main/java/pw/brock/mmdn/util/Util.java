package pw.brock.mmdn.util;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * @author BrockWS
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class Util {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T fromJsonFile(File file, Class<T> clazz) throws IOException, JsonSyntaxException {
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, clazz);
        }
    }

    public static void toJsonFile(File file, Object obj) throws IOException, JsonSyntaxException {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(obj, writer);
        }
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
        return Util.gson;
    }
}
