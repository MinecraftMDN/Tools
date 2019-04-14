package pw.brock.mmdn.detector.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pw.brock.mmdn.api.IDetector;
import pw.brock.mmdn.models.Package;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.util.Log;

/**
 * This detector simply reads the version string and adds a minecraft relationship with the initial part of the version
 *
 * @author BrockWS
 */
public class YarnDetector implements IDetector {

    @Override
    public List<Version> detect(Package pack, Map<String, String> data, List<String> existingVersions, boolean updateExisting) {
        if (!pack.id().equalsIgnoreCase("yarn"))
            throw new RuntimeException("This detector is only for yarn!");
        List<Version> versions = new ArrayList<>();
        Pattern pattern = Pattern.compile(data.getOrDefault("regex", "\\."));
        existingVersions.forEach(s -> {
            Log.trace("Version: {}", s);
            Matcher matcher = pattern.matcher(s);
            if (!matcher.find()) {
                Log.error("Failed to find match for {} using {}", s, pattern.pattern());
                return;
            }
            String mcVersion = matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
            Log.trace("Found mc version {}", mcVersion);
            Version version = new Version();
            version.id = s;
            version.relationships.add(new Version.Relationship("required", "minecraft", mcVersion));
            versions.add(version);
        });
        return versions;
    }
}
