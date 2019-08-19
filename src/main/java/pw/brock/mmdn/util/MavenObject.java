package pw.brock.mmdn.util;

/**
 * @author BrockWS
 */
public class MavenObject {
    public String group;
    public String artifact;
    public String version;
    public String classifier;
    public String extension;

    public MavenObject(String group, String artifact, String version, String classifier, String extension) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
    }
}
