package pw.brock.mmdn.util;

import java.io.File;

/**
 * @author BrockWS
 */
public class FilterUtils {

    /**
     * Checks if the given file is a json file that is not index.json or package.json
     *
     * @param file File to check
     * @return True if file is a package version
     */
    public static boolean filterPackageVersions(File file) {
        String name = file.getName();
        return name.endsWith(".json") && !(name.equals("index.json") || name.equals("package.json"));
    }
}
