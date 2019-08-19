package pw.brock.mmdn.models;

import java.util.List;
import java.util.Map;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class HashDatabase {
    @Key
    public Map<String, Map<String, String>> hashes;
    @Key
    public Map<String, Map<String, List<String>>> collisions;
}
