package pw.brock.mmdn.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import pw.brock.mmdn.util.Log;

/**
 * Package Spec implementation https://github.com/MinecraftMDN/Spec
 *
 * @author BrockWS
 */
@SuppressWarnings({"FieldCanBeLocal", "unused", "Duplicates", "WeakerAccess"})
public class Package implements IDataModel {

    private int formatVersion = -1;
    private String type = "mod";
    private String id = "";
    private String name = "";
    private String description = "";
    private Object licenses = Collections.emptyList();
    private List<String> authors = Collections.emptyList();
    private List<ProjectLink> projectLinks = Collections.emptyList();

    public class ProjectLink {
        private String type;
        private String url;

        public String type() {
            return this.type;
        }

        public String url() {
            return this.url;
        }
    }

    public int formatVersion() {
        return this.formatVersion;
    }

    public String type() {
        return this.type;
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    @SuppressWarnings("unchecked")
    public List<String> licenses() {
        if (this.licenses == null)
            this.licenses = Collections.emptyList();

        if (this.licenses instanceof String)
            return Collections.singletonList((String) this.licenses);
        else if (this.licenses instanceof List) {
            return (List<String>) this.licenses;
        }
        Log.error("{}", this.licenses);
        throw new RuntimeException("Licenses should be a string or list but isn't!");
    }

    public List<String> authors() {
        return this.authors;
    }

    public List<ProjectLink> projectLinks() {
        return this.projectLinks;
    }

    @Override
    public void prepareForMinify() {
        if (this.name != null && this.name.isEmpty())
            this.name = null;
        if (this.description != null && this.description.isEmpty())
            this.description = null;
        if (this.licenses != null && this.licenses().isEmpty())
            this.licenses = null;
        if (this.authors != null && this.authors.isEmpty())
            this.authors = null;
        if (this.projectLinks != null && this.projectLinks.isEmpty())
            this.projectLinks = null;
    }

    public boolean verify() {
        // Probably could just use a json schema instead
        AtomicBoolean valid = new AtomicBoolean(true);
        if (this.formatVersion < 0) {
            Log.error("formatVersion is < 0! {}", this.formatVersion);
            valid.set(false);
        }
        if (this.id.isEmpty()) {
            Log.error("id is empty!");
            valid.set(false);
        }
        for (String license : this.licenses()) {
            if (license.isEmpty()) {
                Log.error("license string is empty!");
                valid.set(false);
            }
        }
        for (String author : this.authors) {
            if (author.isEmpty()) {
                Log.error("author string is empty!");
                valid.set(false);
            }
        }
        for (ProjectLink link : this.projectLinks) {
            if (link.type.isEmpty()) {
                Log.error("project link type string is empty!");
                valid.set(false);
            }
            if (link.url.isEmpty()) {
                Log.error("project link url string is empty!");
                valid.set(false);
            }
        }

        return valid.get();
    }
}
