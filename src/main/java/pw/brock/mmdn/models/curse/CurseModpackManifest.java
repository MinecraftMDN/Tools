package pw.brock.mmdn.models.curse;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class CurseModpackManifest {
    @Key
    public int manifestVersion;
    @Key
    public String manifestType;
    @Key
    public int projectID;
    @Key
    public String name;
    @Key
    public String version;
    @Key
    public Minecraft minecraft;
    @Key
    public String author;
    @Key
    public String overrides;
    @Key
    public List<File> files;

    public static class Minecraft {
        @Key
        public List<ModLoader> modLoaders;
        @Key
        public String version;
    }

    public static class ModLoader {
        @Key
        public String id;
        @Key
        public boolean primary;
    }

    public static class File {
        @Key
        public int projectID;
        @Key
        public int fileID;
        @Key
        public boolean required;
    }
}
