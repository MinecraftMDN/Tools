package pw.brock.mmdn;

import java.util.HashMap;
import java.util.Map;

/**
 * @author BrockWS
 */
public class Globals {

    // *************************************
    // Packages Repository
    // *************************************
    public static String PACKAGES_DIR = "Mods";

    // *************************************
    // Versions Repository
    // *************************************
    public static String VERSIONS_DIR = "Versions";

    // *************************************
    // Meta Repository (Compiled)
    // *************************************
    public static String META_DIR = "Meta";

    // *************************************
    // Curse
    // *************************************
    public static String CURSEMETA = "https://staging_cursemeta.dries007.net";

    // *************************************
    // Misc
    // *************************************
    public static String PROJECTS = "";
    // Forgot about existing versions
    public static boolean FRESH = false;
    // Merge with existing versions
    public static boolean UPDATE = false;
    public static Map<String, String> MIRRORS = new HashMap<>();

}
