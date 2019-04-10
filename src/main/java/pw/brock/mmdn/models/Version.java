package pw.brock.mmdn.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import pw.brock.mmdn.util.Log;

/**
 * @author BrockWS
 */
@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class Version {

    public int specVersion = 0;
    public String id = "";
    public String changelog = "";
    public String side = "universal";
    public List<Relationship> relationships = new ArrayList<>();
    public List<Artifact> artifacts = new ArrayList<>();
    public Map<String, String> hashes = new HashMap<>();

    public Version() {
    }

    public static class Relationship {
        public String type = "";
        public String id = "";
        public Object version = "*";

        public Relationship() {
        }

        public Relationship(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public Relationship(String type, String id, Object version) {
            this.type = type;
            this.id = id;
            this.version = version;
        }

        public String type() {
            return this.type;
        }

        public String id() {
            return this.id;
        }

        public Object version() {
            return this.version;
        }

        @Override
        public String toString() {
            return this.type + " | " + this.id + " | " + this.version;
        }
    }

    public static class Artifact {
        public String type = "";
        public String id = "";

        public Artifact() {
        }

        public Artifact(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public String type() {
            return this.type;
        }

        public String id() {
            return this.id;
        }

        @Override
        public String toString() {
            return this.type + " | " + this.id;
        }
    }

    public int specVersion() {
        return this.specVersion;
    }

    public String id() {
        return this.id;
    }

    public String changelog() {
        return this.changelog;
    }

    public String side() {
        return this.side;
    }

    public List<Relationship> relationships() {
        return this.relationships;
    }

    public List<Artifact> artifacts() {
        return this.artifacts;
    }

    public Map<String, String> hashes() {
        return this.hashes;
    }

    public boolean verify() {
        // Probably could just use a json schema instead
        AtomicBoolean valid = new AtomicBoolean(true);
        if (this.specVersion < 0) {
            Log.error("specVersion is < 0! {}", this.specVersion);
            valid.set(false);
        }
        if (this.id.isEmpty()) {
            Log.error("id string is empty!");
            valid.set(false);
        }
        for (Relationship relationship : this.relationships) {
            if (relationship.id.isEmpty()) {
                Log.error("relationship id is empty!");
                valid.set(false);
            }
            if (relationship.type.isEmpty()) {
                Log.error("relationship type is empty!");
                valid.set(false);
            }
            if ((relationship.version instanceof String && ((String) relationship.version).isEmpty()) ||
                    (relationship.version instanceof String[] && ((String[]) relationship.version).length < 1)) {
                Log.error("relationship version is empty!");
                valid.set(false);
            }
        }
        for (Artifact artifact : this.artifacts) {
            if (artifact.type.isEmpty()) {
                Log.error("artifact type is empty!");
                valid.set(false);
            }
            if (artifact.id.isEmpty()) {
                Log.error("artifact id is empty!");
                valid.set(false);
            }
        }
        this.hashes.forEach((type, hash) -> {
            if (type.isEmpty()) {
                Log.error("hash type is empty!");
                valid.set(false);
            }
            if (hash.isEmpty()) {
                Log.error("hash id is empty!");
                valid.set(false);
            }
        });

        return valid.get();
    }

    @Override
    public String toString() {
        return this.specVersion + " | " + this.id + " | " + this.changelog + " | " + this.side + " | " +
                this.relationships.stream().map(Object::toString).collect(Collectors.joining(" | ")) + " | " +
                this.artifacts.stream().map(Object::toString).collect(Collectors.joining(" | ")) + " | " +
                this.hashes.toString();
    }
}
