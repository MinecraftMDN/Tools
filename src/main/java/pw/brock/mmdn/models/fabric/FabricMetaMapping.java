package pw.brock.mmdn.models.fabric;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class FabricMetaMapping {
    @Key
    public String gameVersion;
    @Key
    public String separator;
    @Key
    public int build;
    @Key
    public String maven;
    @Key
    public String version;
    @Key
    public boolean stable;
}
