package pw.brock.mmdn;

import java.util.Map;

import pw.brock.mmdn.api.DetectorRegistry;
import pw.brock.mmdn.detector.CurseForgeDetector;
import pw.brock.mmdn.detector.MavenDetector;
import pw.brock.mmdn.detector.VersionDiscoverer;
import pw.brock.mmdn.detector.special.FabricLoaderDetector;
import pw.brock.mmdn.detector.special.FabricMetaDetector;
import pw.brock.mmdn.detector.special.YarnDetector;
import pw.brock.mmdn.meta.MetaGenerator;
import pw.brock.mmdn.meta.SourceType;
import pw.brock.mmdn.util.Log;

/**
 * @author BrockWS
 */
public class Main {

    public static void main(String[] args) {
        Log.info("Starting...");
        // FIXME: Use something much better
        boolean meta = Main.parseArg(args, "meta", false);
        boolean index = Main.parseArg(args, "index", false);

        Globals.UPSTREAM_DIR = Main.parseArg(args, "upstreamDir", Globals.UPSTREAM_DIR);
        Globals.META_DIR = Main.parseArg(args, "metaDir", Globals.META_DIR);
        Globals.PROJECTS = Main.parseArg(args, "projects", Globals.PROJECTS);
        Globals.FRESH = Main.parseArg(args, "fresh", Globals.FRESH);
        Globals.UPDATE = Globals.FRESH;
        Globals.UPDATE = Main.parseArg(args, "update", Globals.UPDATE);
        Globals.CURSEMETA = Main.parseArg(args, "cursemeta", Globals.CURSEMETA);
        if (Globals.CURSEMETA.endsWith("/"))
            Globals.CURSEMETA = Globals.CURSEMETA.substring(0, Globals.CURSEMETA.length() - 1);
        Main.parseArg(args, "mirror", Globals.MIRRORS);
        Log.debug("Parsed arguments!");

        DetectorRegistry.register("maven", new MavenDetector());
        DetectorRegistry.register("curseforge", new CurseForgeDetector());
        DetectorRegistry.register("fabricloader", new FabricLoaderDetector());
        DetectorRegistry.register("yarn", new YarnDetector());
        DetectorRegistry.register("fabricmeta", new FabricMetaDetector());

        if (index) {
            if (Globals.FRESH) {
                Log.warn("FRESHLY GENERATING FILES!");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {

                }
            }
            if (!Globals.PROJECTS.isEmpty()) {
                for (String s : Globals.PROJECTS.split(",")) {
                    new VersionDiscoverer(s).run();
                }
                return;
            }
            Log.error("Missing --projects");
        } else if (meta) {
            new MetaGenerator("Meta")
                    .addSource(SourceType.ACTIVE, "Upstream/active")
                    .addSource(SourceType.FROZEN, "Upstream/frozen")
                    .run();
        } else {
            Log.error("Specify an argument or something...");
        }
    }

    private static String parseArg(String[] args, String arg, String defaultArg) {
        String startsWith = "--" + arg + "=";
        for (String a : args) {
            if (!a.startsWith(startsWith))
                continue;
            a = a.substring(startsWith.length());
            return a.isEmpty() ? defaultArg : a;
        }
        return defaultArg;
    }

    private static boolean parseArg(String[] args, String arg, boolean defaultArg) {
        String startsWith = "--" + arg;
        for (String a : args) {
            if (!a.startsWith(startsWith))
                continue;
            a = a.substring(startsWith.length());
            if (a.isEmpty())
                return true;
            return a.startsWith("=") ? Boolean.parseBoolean(a.substring(1)) : defaultArg;
        }
        return defaultArg;
    }

    private static void parseArg(String[] args, String arg, Map<String, String> map) {
        String startsWith = "--" + arg + "=";
        for (String a : args) {
            if (!a.startsWith(startsWith))
                continue;
            String[] split = a.substring(startsWith.length()).split("\\|", 2);
            map.put(split[0], split[1]);
        }
    }
}
