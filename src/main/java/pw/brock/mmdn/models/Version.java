package pw.brock.mmdn.models;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import pw.brock.mmdn.util.Log;

/**
 * @author BrockWS
 */
@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class Version implements IDataModel {

    public int formatVersion = 0;
    public String id = "";
    public String releaseType = "";
    public String changelog = "";
    public String side = "";
    public List<Relationship> relationships = new ArrayList<>();
    public List<Artifact> artifacts = new ArrayList<>();
    public Map<String, String> hashes = new HashMap<>();
    public long size;
    public String filename = "";
    public String releaseTime = "";

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

        @SuppressWarnings("unchecked")
        public List<String> version() {
            if (this.version == null)
                this.version = Collections.emptyList();

            if (this.version instanceof String)
                return Collections.singletonList((String) this.version);
            else if (this.version instanceof List) {
                return (List<String>) this.version;
            }
            Log.error("{}", this.version);
            throw new RuntimeException("Versions should be a string or list but isn't!");
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

    public int formatVersion() {
        return this.formatVersion;
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

    @Override
    public void populateDefaults() {
        if (this.releaseType == null || this.releaseType.isEmpty())
            this.releaseType = "release";
        if (this.side == null || this.side.isEmpty())
            this.side = "universal";
    }

    @Override
    public void prepareForMinify() {
        if (this.changelog != null && this.changelog.isEmpty())
            this.changelog = null;
        if (this.side != null && (this.side.isEmpty() || this.side.equalsIgnoreCase("universal")))
            this.side = null;
        if (this.relationships != null) {
            if (this.relationships.isEmpty()) {
                this.relationships = null;
            } else {
                this.relationships.forEach(r -> {
                    if (r.version != null && r.version().isEmpty())
                        r.version = null;
                });
            }
        }
        if (this.artifacts != null && this.artifacts.isEmpty())
            this.artifacts = null;
        if (this.hashes != null && this.hashes.isEmpty())
            this.hashes = null;
        if (this.filename != null && this.filename.isEmpty())
            this.filename = null;
        if (this.releaseTime != null && this.releaseTime.isEmpty())
            this.releaseTime = null;
    }

    public boolean verify() {
        // Probably could just use a json schema instead
        AtomicBoolean valid = new AtomicBoolean(true);
        if (this.formatVersion < 0) {
            Log.error("formatVersion is < 0! {}", this.formatVersion);
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
        return this.formatVersion + " | " + this.id + " | " + this.changelog + " | " + this.side + " | " +
                this.relationships.stream().map(Object::toString).collect(Collectors.joining(" | ")) + " | " +
                this.artifacts.stream().map(Object::toString).collect(Collectors.joining(" | ")) + " | " +
                this.hashes.toString();
    }
}
