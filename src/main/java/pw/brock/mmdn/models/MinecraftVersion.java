package pw.brock.mmdn.models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BrockWS
 */
public class MinecraftVersion extends Version {

    public String mainClass;
    public String launchArguments;
    public List<Library> libraries = new ArrayList<>();
    public AssetIndex assetIndex = new AssetIndex();

    {
        this.client = new SidedOverride();
        this.server = new SidedOverride();
    }

    public void setClientMainClass(String mainClass) {
        ((SidedOverride) this.client).mainClass = mainClass;
    }

    public void setClientLaunchArgs(String launchArgs) {
        ((SidedOverride) this.client).launchArguments = launchArgs;
    }

    public void setServerMainClass(String mainClass) {
        ((SidedOverride) this.server).mainClass = mainClass;
    }

    public void setServerLaunchArgs(String launchArgs) {
        ((SidedOverride) this.server).launchArguments = launchArgs;
    }

    public static class SidedOverride extends Version.SidedOverride {
        public String mainClass;
        public String launchArguments;
    }

    public static class AssetIndex {
        public String id;
        public String url;
        public String sha1;
        public long size = -1;
        public long totalSize = -1;
    }
}
