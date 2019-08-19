package pw.brock.mmdn.models.fabric;

import java.util.List;
import java.util.Map;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class FabricMetaLauncher extends GenericJson {
    @Key
    public int version;
    @Key
    public Map<String, List<Library>> libraries;
    @Key("launchwrapper")
    public LaunchWrapper launchWrapper;

    public static class Library {
        @Key
        public String name;
        @Key
        public String url;
    }

    public static class LaunchWrapper {
        @Key
        public Map<String, List<String>> tweakers;
    }
}
