package pw.brock.mmdn.models.curse;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class CurseAddonFile {
    @Key
    public int id;
    @Key
    public String displayName;
    @Key
    public String fileName;
    @Key
    public String fileDate;
    @Key
    public int fileLength;
    @Key
    public int releaseType;
    @Key
    public int fileStatus;
    @Key
    public String downloadUrl;
    @Key
    public boolean isAlternate;
    @Key
    public int alternateFileId;
    @Key
    public boolean isAvailable;
    @Key
    public long packageFingerprint;
    @Key
    public List<String> gameVersion;
    @Key
    public int serverPackFileId;
    @Key
    public boolean hasInstallScript;
    @Key
    public String gameVersionDateReleased;
}
