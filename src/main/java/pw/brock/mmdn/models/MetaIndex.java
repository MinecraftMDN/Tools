package pw.brock.mmdn.models;

import java.util.List;

/**
 * @author BrockWS
 */
public class MetaIndex {

    public int formatVersion = 0;
    public List<Entry> packages;
    public List<Entry> versions;

    public static class Entry {
        public String id;
        public String sha256;

        public Entry() {
        }

        public Entry(String id, String sha256) {
            this.id = id;
            this.sha256 = sha256;
        }
    }
}
