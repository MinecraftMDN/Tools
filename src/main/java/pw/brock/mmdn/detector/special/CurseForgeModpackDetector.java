package pw.brock.mmdn.detector.special;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import pw.brock.mmdn.api.IDetector;
import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;

/**
 * @author BrockWS
 */
public class CurseForgeModpackDetector implements IDetector {
    @Override
    public List<Version> detect(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        if (!data.containsKey("id"))
            return Collections.emptyList();
        return Collections.emptyList();
    }
}
