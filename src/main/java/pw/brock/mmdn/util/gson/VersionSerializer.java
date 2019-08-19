package pw.brock.mmdn.util.gson;

import java.lang.reflect.Type;

import pw.brock.mmdn.models.LibraryVersion;
import pw.brock.mmdn.models.MinecraftVersion;
import pw.brock.mmdn.models.Version;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author BrockWS
 */
public class VersionSerializer implements JsonSerializer<Version> {

    private boolean addDefault;

    public VersionSerializer(boolean addDefault) {
        this.addDefault = addDefault;
    }

    @Override
    public JsonElement serialize(Version src, Type type, JsonSerializationContext context) {
        JsonObject o = new JsonObject();

        o.add("formatVersion", context.serialize(src.formatVersion));
        o.add("id", context.serialize(src.id));

        if (src.releaseType != null && !src.releaseType.isEmpty())
            o.add("releaseType", context.serialize(src.releaseType));
        else if (addDefault)
            o.add("releaseType", context.serialize("release"));

        if (src.changelog != null && !src.changelog.isEmpty())
            o.add("changelog", context.serialize(src.changelog));

        if (src.side != null && !src.side.isEmpty())
            o.add("side", context.serialize(src.side));
        else if (addDefault)
            o.add("side", context.serialize("universal"));

        if (src.relationships != null && !src.relationships.isEmpty())
            o.add("relationships", context.serialize(src.relationships));

        if (src.artifacts != null && !src.artifacts.isEmpty())
            o.add("artifacts", context.serialize(src.artifacts));

        if (src.client != null) // TODO: Check fields as well
            o.add("client", context.serialize(src.client));

        if (src.server != null) // TODO: Check fields as well
            o.add("server", context.serialize(src.server));

        if (src.hashes != null && !src.hashes.isEmpty())
            o.add("hashes", context.serialize(src.hashes));

        if (src.size > -1)
            o.add("size", context.serialize(src.size));

        if (src.filename != null && !src.filename.isEmpty())
            o.add("filename", context.serialize(src.filename));

        if (src.releaseTime != null && !src.releaseTime.isEmpty())
            o.add("releaseTime", context.serialize(src.releaseTime));

        if (src instanceof LibraryVersion)
            this.serializeLibrary(o, (LibraryVersion) src, type, context);
        else if (src instanceof MinecraftVersion)
            this.serializeMinecraft(o, (MinecraftVersion) src, type, context);

        return o;
    }

    public void serializeLibrary(JsonObject o, LibraryVersion src, Type type, JsonSerializationContext context) {
        if (src.libraries != null && !src.libraries.isEmpty())
            o.add("libraries", context.serialize(src.libraries));
    }

    public void serializeMinecraft(JsonObject o, MinecraftVersion src, Type type, JsonSerializationContext context) {
        if (src.mainClass != null && !src.mainClass.isEmpty())
            o.add("mainClass", context.serialize(src.mainClass));

        if (src.launchArguments != null && !src.launchArguments.isEmpty())
            o.add("launchArguments", context.serialize(src.launchArguments));

        if (src.libraries != null && !src.libraries.isEmpty())
            o.add("libraries", context.serialize(src.libraries));

        if (src.assetIndex != null)
            o.add("assetIndex", context.serialize(src.assetIndex));
    }
}
