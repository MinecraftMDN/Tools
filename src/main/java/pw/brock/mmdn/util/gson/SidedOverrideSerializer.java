package pw.brock.mmdn.util.gson;

import java.lang.reflect.Type;

import pw.brock.mmdn.models.MinecraftVersion;
import pw.brock.mmdn.models.Version;

import com.google.gson.*;

/**
 * @author BrockWS
 */
public class SidedOverrideSerializer implements JsonSerializer<Version.SidedOverride> {

    private boolean addDefaults;

    public SidedOverrideSerializer(boolean addDefaults) {
        this.addDefaults = addDefaults;
    }

    @Override
    public JsonElement serialize(Version.SidedOverride src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject o = new JsonObject();

        if (src.hashes != null && !src.hashes.isEmpty())
            o.add("hashes", context.serialize(src.hashes));

        if (src.size > -1)
            o.add("size", context.serialize(src.size));

        if (src.filename != null && !src.filename.isEmpty())
            o.add("filename", context.serialize(src.filename));

        if (src instanceof MinecraftVersion.SidedOverride)
            this.serializeLibrary(o, (MinecraftVersion.SidedOverride) src, typeOfSrc, context);

        return o.size() > 0 ? o : JsonNull.INSTANCE;
    }

    public void serializeLibrary(JsonObject o, MinecraftVersion.SidedOverride src, Type type, JsonSerializationContext context) {
        if (src.mainClass != null && !src.mainClass.isEmpty())
            o.add("mainClass", context.serialize(src.mainClass));

        if (src.launchArguments != null && !src.launchArguments.isEmpty())
            o.add("launchArguments", context.serialize(src.launchArguments));
    }
}
