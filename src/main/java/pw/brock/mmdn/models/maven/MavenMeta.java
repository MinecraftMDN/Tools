package pw.brock.mmdn.models.maven;

import java.util.List;

import com.google.api.client.util.Key;
import com.google.api.client.xml.GenericXml;

/**
 * @author BrockWS
 */
public class MavenMeta extends GenericXml {
    @Key
    private String groupId;
    @Key
    private String artifactId;
    @Key
    private Versioning versioning;

    public String groupId() {
        return this.groupId;
    }

    public String artifactId() {
        return this.artifactId;
    }

    public String release() {
        return this.versioning.release;
    }

    public String lastUpdated() {
        return this.versioning.lastUpdated;
    }

    public List<String> versions() {
        return this.versioning.versions.version;
    }

    public static class Versioning extends GenericXml {

        @Key
        private String release;
        @Key
        private Versions versions;
        @Key
        private String lastUpdated;

        public Versioning() {
        }
    }

    public static class Versions extends GenericXml {

        @Key
        private List<String> version;

        public Versions() {
        }
    }
}
