package pw.brock.mmdn.models.fabric;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class FabricMetaIntermedairy {
    @Key
    public String maven;
    @Key
    public String version;
    @Key
    public boolean stable;
}
