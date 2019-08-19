package pw.brock.mmdn.models.fabric;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class FabricMetaVersions {
    @Key
    public List<FabricMetaLoader> loader = new ArrayList<>();
    @Key
    public List<FabricMetaYarn> mappings = new ArrayList<>();
    @Key
    public List<FabricMetaYarn> intermedairy = new ArrayList<>();
}
