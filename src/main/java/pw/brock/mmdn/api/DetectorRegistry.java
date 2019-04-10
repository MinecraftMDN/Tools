package pw.brock.mmdn.api;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * @author BrockWS
 */
public class DetectorRegistry {

    private static Map<String, IDetector> detectors = new HashMap<>();

    public static void register(String id, IDetector detector) {
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(!id.isEmpty());
        Preconditions.checkNotNull(detector);
        DetectorRegistry.detectors.put(id, detector);
    }

    public static IDetector getDetector(String id) {
        return DetectorRegistry.detectors.get(id);
    }
}
