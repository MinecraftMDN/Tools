package pw.brock.mmdn.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author BrockWS
 */
public class MLVersion extends Version {

    public List<Library> libraries = new ArrayList<>();

    public static class Library {
        public String id;
        public String url;
        public String side = "common";
        public Map<String, String> hashes = new HashMap<>();

        public Library() {
        }

        @Override
        public String toString() {
            return this.id + " | " + this.url + " | " + this.side + " | " + this.hashes.toString();
        }
    }

    @Override
    public String toString() {
        return super.toString() + "|" + this.libraries.toString();
    }
}
