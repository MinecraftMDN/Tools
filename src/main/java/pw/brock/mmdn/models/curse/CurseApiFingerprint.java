package pw.brock.mmdn.models.curse;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class CurseApiFingerprint {

    @Key
    public List<Match> exactMatches;
    @Key
    public List<Long> exactFingerprints;
    @Key
    public List<Long> partialFingerprints;
    @Key
    public List<Match> partialMatches;
    @Key
    public List<Long> installedFingerprints;
    @Key
    public List<Long> unmatchedFingerprints;

    public static class Match {
        @Key
        public int id;
        @Key
        public CurseAddonFile file;
        @Key
        public CurseAddonFiles latestFile;
    }
}
