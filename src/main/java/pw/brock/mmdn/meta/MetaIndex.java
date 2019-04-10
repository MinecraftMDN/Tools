package pw.brock.mmdn.meta;

import java.util.List;

/**
 * @author BrockWS
 */
public class MetaIndex {

    private int formatVersion;
    private List<Package> packages;

    public MetaIndex(int formatVersion) {
        this.formatVersion = formatVersion;
    }

    public int formatVersion() {
        return this.formatVersion;
    }

    public List<Package> packages() {
        return this.packages;
    }

    public Package addPackage(String id,String sha256) {
        Package pack = new Package(id,sha256);
        this.packages.add(pack);
        return pack;
    }

    public class Package {
        private String id;
        private String sha256;

        public Package(String id, String sha256) {
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
