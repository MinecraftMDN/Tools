package pw.brock.mmdn.models.maven;

import java.util.Collections;
import java.util.List;

import com.google.api.client.util.Key;
import com.google.api.client.xml.GenericXml;

/**
 * @author BrockWS
 */
public class MavenPom extends GenericXml {

    @Key("2:groupId")
    public String groupId;
    @Key("2:artifactId")
    public String artifactId;
    @Key("2:version")
    public String version;
    @Key("2:dependencies")
    public Dependencies dependencies;

    public List<Dependency> dependencies() {
        if (this.dependencies == null || this.dependencies.dependency == null)
            return Collections.emptyList();
        return this.dependencies.dependency;
    }

    public static class Dependencies extends GenericXml {
        @Key("2:dependency")
        private List<Dependency> dependency;
    }

    public static class Dependency extends GenericXml {
        @Key("2:groupId")
        public String groupId;
        @Key("2:artifactId")
        public String artifactId;
        @Key("2:version")
        public String version;
        @Key("2:scope")
        public String scope;
    }
}
