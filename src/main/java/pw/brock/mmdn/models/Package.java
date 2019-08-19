package pw.brock.mmdn.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import pw.brock.mmdn.util.Log;

/**
 * Package Spec implementation https://github.com/MinecraftMDN/Spec
 *
 * @author BrockWS
 */
@SuppressWarnings({"FieldCanBeLocal", "unused", "Duplicates", "WeakerAccess"})
public class Package implements IDataModel {

    public int formatVersion = 0;
    public String type = "mod";
    public String id = "";
    public String name = "";
    public String description = "";
    public String icon = "";
    public Object licenses = new ArrayList<>();
    public List<String> authors = new ArrayList<>();
    public List<ProjectLink> projectLinks = new ArrayList<>();

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

    public static class ProjectLink {
        public String type;
        public String url;
    }
}
