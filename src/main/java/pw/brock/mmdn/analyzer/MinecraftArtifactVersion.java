package pw.brock.mmdn.analyzer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.versioning.ArtifactVersion;

/**
 * @author BrockWS
 */
public class MinecraftArtifactVersion implements ArtifactVersion {

    private static final Pattern PATTERN_SNAPSHOT = Pattern.compile("(?:(\\d+)w(\\d+)([a-z]))|(?:(\\d+)\\.*(\\d+)*\\.*(\\d+)*(?:(?:-pre)|(?: Pre-Release )|(?:RV-Pre))*(\\d+)*)");
    private int major;
    private int minor;
    private int incremental;
    private int build;
    private String qualifier;

    public MinecraftArtifactVersion(String version) {
        this.parseVersion(version);
    }

    public MinecraftArtifactVersion(int major, int minor, int incremental, int build, String qualifier) {
        this.major = major;
        this.minor = minor;
        this.incremental = incremental;
        this.build = build;
        this.qualifier = qualifier;
    }

    @Override
    public int getMajorVersion() {
        return this.major;
    }

    @Override
    public int getMinorVersion() {
        return this.minor;
    }

    @Override
    public int getIncrementalVersion() {
        return this.incremental;
    }

    @Override
    public int getBuildNumber() {
        return this.build;
    }

    @Override
    public String getQualifier() {
        return this.qualifier;
    }

    @Override
    public void parseVersion(String version) {
        Matcher PATTERN_MATCHER = PATTERN_SNAPSHOT.matcher(version);
        if (PATTERN_MATCHER.matches()) { // Snapshot

        }
    }

    @Override
    public int compareTo(ArtifactVersion o) {
        return 0;
    }
}
