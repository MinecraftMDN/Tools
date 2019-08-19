package pw.brock.mmdn.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import pw.brock.mmdn.Globals;
import pw.brock.mmdn.models.HashDatabase;
import pw.brock.mmdn.models.MetaIndex;

import com.google.common.base.Preconditions;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author BrockWS
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class Util {

    public static List<String> getPackageVersionIds(String p) {
        Preconditions.checkNotNull(p);
        Preconditions.checkArgument(!p.isEmpty());
        File path = FileUtil.file(Globals.META_DIR, p, "index.json");
        Preconditions.checkArgument(path.exists(), "Path does not exist! " + path.getAbsolutePath());
        MetaIndex index;
        try {
            index = JsonUtil.fromJsonFile(path, MetaIndex.class);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        if (index.versions == null)
            return Collections.emptyList();
        return index.versions.stream().map(pack -> pack.id).collect(Collectors.toList());
    }

    public static String lookupHash(String type, String hash) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(hash);

        File file = FileUtil.file(Globals.META_DIR, "hashDatabase.json");
        HashDatabase hashDatabase;
        try {
            hashDatabase = JsonUtil.fromJsonFile(file, HashDatabase.class);
        } catch (IOException e) {
            Log.error("Failed to read hashDatabase.json");
            e.printStackTrace();
            return null;
        }
        // FIXME Check collisions first
        Map<String, String> map = hashDatabase.hashes.getOrDefault(type, Collections.emptyMap());
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (hash.equalsIgnoreCase(e.getKey()))
                return e.getValue();
        }
        return null;
    }

    public static String calculateMurmur2(File file) throws IOException {
        Preconditions.checkNotNull(file);
        int cHash = Murmur2.hash32(Util.removeWhitespaces(file));
        String hash = String.valueOf(cHash & 0x00000000ffffffffL); // Since the murmur returns a signed int, need to convert to unsigned long
        if (hash.isEmpty())
            throw new RuntimeException("Failed to calculate murmur2 for " + file.getAbsolutePath());
        Log.trace("murmur2 for {} is {}", file.getAbsolutePath(), hash);
        return hash;
    }

    public static String calculateMD5(File file) throws IOException {
        Preconditions.checkNotNull(file);
        String hash = DigestUtils.md5Hex(new FileInputStream(file));
        if (hash.isEmpty())
            throw new RuntimeException("Failed to calculate md5 for " + file.getAbsolutePath());
        Log.trace("md5 for {} is {}", file.getAbsolutePath(), hash);
        return hash;
    }

    public static String calculateSHA1(File file) throws IOException {
        Preconditions.checkNotNull(file);
        String hash = DigestUtils.sha1Hex(new FileInputStream(file));
        if (hash.isEmpty())
            throw new RuntimeException("Failed to calculate sha1 for " + file.getAbsolutePath());
        Log.trace("sha1 for {} is {}", file.getAbsolutePath(), hash);
        return hash;
    }

    public static String calculateSHA256(File file) throws IOException {
        Preconditions.checkNotNull(file);
        String hash = DigestUtils.sha256Hex(new FileInputStream(file));
        if (hash.isEmpty())
            throw new RuntimeException("Failed to calculate sha256 for " + file.getAbsolutePath());
        Log.trace("sha256 for {} is {}", file.getAbsolutePath(), hash);
        return hash;
    }

    public static Map<String, String> calculateCommonHashes(File file) throws IOException {
        Preconditions.checkNotNull(file);
        InputStream in = new FileInputStream(file);
        String md5 = DigestUtils.md5Hex(in);
        String sha1 = DigestUtils.sha1Hex(in);
        String sha256 = DigestUtils.sha256Hex(in);
        if (md5.isEmpty())
            throw new RuntimeException("Failed to calculate md5 for " + file.getAbsolutePath());
        if (sha1.isEmpty())
            throw new RuntimeException("Failed to calculate sha1 for " + file.getAbsolutePath());
        if (sha256.isEmpty())
            throw new RuntimeException("Failed to calculate sha256 for " + file.getAbsolutePath());
        Map<String, String> map = new HashMap<>();
        map.put("md5", md5);
        map.put("sha1", sha1);
        map.put("sha256", sha256);
        return map;
    }

    public static byte[] removeWhitespaces(File file) throws IOException {
        return Util.removeWhitespaces(new FileInputStream(file));
    }

    public static byte[] removeWhitespaces(InputStream stream) throws IOException {
        return Util.removeWhitespaces(IOUtils.toByteArray(stream));
    }

    public static byte[] removeWhitespaces(byte[] buffer) { // There should be a easier way than this
        byte[] out = new byte[buffer.length];
        int i = 0;
        for (byte b : buffer) {
            if (!Util.isWhitespace(b)) {
                out[i] = b;
                i++;
            }
        }
        return Arrays.copyOf(out, i);
    }

    public static boolean isWhitespace(byte b) {
        return b == 9 || b == 10 || b == 13 || b == 32;
    }
}
