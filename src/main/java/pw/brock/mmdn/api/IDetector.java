package pw.brock.mmdn.api;

import java.util.List;
import java.util.Map;

import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;

/**
 * @author BrockWS
 */
public interface IDetector {

    List<Version> detect(Package pack, Map<String, String> data, List<String> ignoredVersions);
}
