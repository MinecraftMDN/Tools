package pw.brock.mmdn.models.mojang;

import java.util.List;
import java.util.Map;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class MojangVersion {

    @Key
    public AssetIndex assetIndex;
    @Key
    public String assets;
    @Key
    public Map<String, Download> downloads;
    @Key
    public String id;
    @Key
    public List<Library> libraries;
    @Key
    public String mainClass;
    @Key
    public String minecraftArguments;
    @Key
    public int minimumLauncherVersion;
    @Key
    public String releaseTime;
    @Key
    public String time;
    @Key
    public String type;
    @Key
    public Map<String, Logging> logging;


    public static class AssetIndex {
        @Key
        public String id;
        @Key
        public String sha1;
        @Key
        public int size;
        @Key
        public int totalSize;
        @Key
        public String url;
    }

    public static class Download {
        @Key
        public String sha1;
        @Key
        public int size;
        @Key
        public String url;
    }

    public static class Library {
        @Key
        public String name;
        @Key
        public Downloads downloads;

        public static class Downloads {
            @Key
            public Artifact artifact;
            @Key
            public Map<String, Artifact> classifiers;
        }

        public static class Artifact {
            @Key
            public String path;
            @Key
            public String sha1;
            @Key
            public int size;
            @Key
            public String url;
        }
    }

    public static class Logging {
        @Key
        public String argument;
        @Key
        public File file;

        public static class File {
            @Key
            public String id;
            @Key
            public String sha1;
            @Key
            public int size;
            @Key
            public String url;
        }
    }
}
