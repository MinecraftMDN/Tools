package pw.brock.mmdn.models;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public SidedOverride client = new SidedOverride();
    public SidedOverride server = new SidedOverride();
    public Map<String, String> hashes = new HashMap<>();
    public long size = -1;
    public String filename = "";
    public String releaseTime = "";

    public Version() {
    }

    // TODO: Remove
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

    // TODO: Remove
    @Override
    public void populateDefaults() {
        if (this.releaseType == null || this.releaseType.isEmpty())
            this.releaseType = "release";
        if (this.side == null || this.side.isEmpty())
            this.side = "universal";
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Version) && this.id.equals(((Version) obj).id);
    }

    // TODO: Remove
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

    public static class Relationship {
        public String type = "";
        public String id = "";
        public Object version = "*";
        public String side = "universal";

        public Relationship() {
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
    }

    public static class Artifact {
        public String type = "";
        public String id = "";
        public String side = "universal";

        public Artifact() {
        }

        public Artifact(String type, String id) {
            this(type, id, "universal");
        }

        public Artifact(String type, String id, String side) {
            this.type = type;
            this.id = id;
            this.side = side;
        }

        public String type() {
            return this.type;
        }

        public String id() {
            return this.id;
        }

        public String side() {
            return this.side;
        }
    }

    public static class SidedOverride {
        public Map<String, String> hashes = new HashMap<>();
        public long size = -1;
        public String filename;
    }

    public static class Library {
        public String type = "jar";
        public String id;
        public String url;
        public String side = "universal";
        public long size = -1;
        public Map<String, String> hashes = new HashMap<>();

        public Library() {
        }

        public String id() {
            return this.id;
        }
    }
}
