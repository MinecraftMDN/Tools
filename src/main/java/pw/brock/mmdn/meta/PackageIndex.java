package pw.brock.mmdn.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BrockWS
 */
public class PackageIndex {

    private int formatVersion;
    private List<Version> versions;

    public PackageIndex(int formatVersion) {
        this.formatVersion = formatVersion;
        this.versions = new ArrayList<>();
    }

    public int formatVersion() {
        return this.formatVersion;
    }

    public List<Version> versions() {
        return this.versions;
    }

    public Version addVersion(String id, String sha256) {
        Version version = new Version(id, sha256);
        this.versions.add(version);
        return version;
    }

    public class Version {
        private String id;
        private String sha256;

        public Version(String id, String sha256) {
            this.id = id;
            this.sha256 = sha256;
        }

        public String id() {
            return this.id;
        }

        public String sha256() {
            return this.sha256;
        }
    }
}
