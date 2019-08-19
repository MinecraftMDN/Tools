package pw.brock.mmdn.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.ModMetadataParser;

/**
 * @author BrockWS
 */
public class FabricModAnalyzer {

    private File file;

    public FabricModAnalyzer(File file) {
        this.file = file;
    }

    public LoaderModMetadata[] analyze() {
        try {
            ZipInputStream stream = new ZipInputStream(new FileInputStream(this.file));
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (!entry.getName().equalsIgnoreCase("fabric.mod.json"))
                    continue;
                // TODO Remove dependency on FabricLoader
                return ModMetadataParser.getMods(null, stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
