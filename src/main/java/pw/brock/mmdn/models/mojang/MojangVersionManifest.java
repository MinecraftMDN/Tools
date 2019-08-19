package pw.brock.mmdn.models.mojang;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class MojangVersionManifest {
    @Key
    public Latest latest;
    @Key
    public List<Version> versions;

    public static class Latest {
        @Key
        public String release;
        @Key
        public String snapshot;
    }

    public static class Version {
        @Key
        public String id;
        @Key
        public String type;
        @Key
        public String url;
        @Key
        public String time;
        @Key
        public String releaseTime;
    }
}
