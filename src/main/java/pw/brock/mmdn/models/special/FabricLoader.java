package pw.brock.mmdn.models.special;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class FabricLoader extends GenericJson {
    @Key
    public int version;
    @Key
    public Map<String, List<Library>> libraries = new HashMap<>();
    @Key
    public Object mainClass = new HashMap<>();
    @Key
    public Map<String, List<String>> arguments = new HashMap<>();
    @Key("launchwrapper")
    public LaunchWrapper launchwrapper;

    @SuppressWarnings("unchecked")
    public Map<String, String> mainClass() {
        if (this.mainClass == null)
            return Collections.emptyMap();
        if (this.mainClass instanceof Map)
            return (Map<String, String>) this.mainClass;
        Map<String, String> map = new HashMap<>();
        map.put("client", String.valueOf(this.mainClass));
        map.put("server", String.valueOf(this.mainClass));
        return map;
    }

    public Map<String, List<String>> tweakers() {
        if (this.launchwrapper == null || this.launchwrapper.tweakers == null)
            return Collections.emptyMap();
        return this.launchwrapper.tweakers;
    }

    public static class Library {
        @Key
        public String name;
        @Key
        public String url = "";
    }

    public static class LaunchWrapper {
        @Key
        public Map<String, List<String>> tweakers = new HashMap<>();

        @Override
        public String toString() {
            return tweakers.toString();
        }
    }
}
