package pw.brock.mmdn.models.curse;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class CurseAddon {
    @Key
    public int id;
    @Key
    public String name;
    @Key
    public List<Author> authors;
    @Key
    public List<Attachment> attachments;
    @Key
    public String websiteUrl;
    @Key
    public int gameId;
    @Key
    public String summary;
    @Key
    public int defaultFileId;
    @Key
    public int commentCount;
    @Key
    public int downloadCount;
    @Key
    public int rating;
    @Key
    public int installCount;
    @Key
    public List<File> latestFiles;
    @Key
    public List<Category> categories;
    @Key
    public String primaryAuthorName;
    @Key
    public String externalUrl;
    @Key
    public int status;
    @Key
    public int stage;
    @Key
    public String donationUrl;
    @Key
    public String primaryCategoryName;
    @Key
    public String primaryCategoryAvatarUrl;
    @Key
    public int likes;
    @Key
    public CategorySection categorySection;
    @Key
    public int packageType;
    @Key
    public String avatarUrl;
    @Key
    public String slug;
    @Key
    public String clientUrl;
    @Key
    public int isFeatured;
    @Key
    public double popularityScore;
    @Key
    public int gamePopularityRank;
    @Key
    public String primaryLanguage;
    @Key
    public String fullDescription;
    @Key
    public String gameName;
    @Key
    public String portalName;
    @Key
    public String sectionName;
    @Key
    public String dateModified;
    @Key
    public String dateCreated;
    @Key
    public String dateReleased;
    @Key
    public boolean isAvailable;
    @Key
    public String categoryList;

    public static class Author {
    }

    public static class Attachment {
        @Key
        public int id;
        @Key
        public int projectID;
        @Key
        public String description;
        @Key
        public boolean isDefault;
        @Key
        public String thumbnailUrl;
        @Key
        public String title;
        @Key
        public String url;
    }

    public static class File {
    }

    public static class Category {
    }

    public static class CategorySection {
    }
}
