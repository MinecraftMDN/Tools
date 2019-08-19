package pw.brock.mmdn.models.fabric;

import com.google.api.client.util.Key;

/**
 * @author BrockWS
 */
public class FabricMetaLoaderVersion {

    @Key
    public FabricMetaLoader loader;
    @Key
    public FabricMetaYarn mappings;
    @Key("launcherMeta")
    public FabricMetaLauncher launcher;

}
