package pw.brock.mmdn;

import java.util.HashMap;
import java.util.Map;

/**
 * Everything in this class can be changed using arguments
 *
 * @author BrockWS
 */
public class Globals {

    // *************************************
    // Upstream Repository
    // *************************************
    public static String UPSTREAM_DIR = "Upstream";

    // *************************************
    // Meta Repository (Compiled)
    // *************************************
    public static String META_DIR = "Meta";

    // *************************************
    // Curse Meta
    // *************************************
    public static String CURSEMETA = "https://staging_cursemeta.dries007.net";

    // *************************************
    // Fabric Meta
    // *************************************
    public static String FABIRC_META = "https://meta.fabricmc.net/v1";
    public static String FABRIC_YARN_ID = "net.fabricmc.yarn";
    public static String FABRIC_LOADER_ID = "net.fabricmc.loader";

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
