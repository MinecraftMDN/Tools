package pw.brock.mmdn;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import pw.brock.mmdn.analyzer.CurseModAnalyzer;
import pw.brock.mmdn.analyzer.CursePackAnalyzer;
import pw.brock.mmdn.api.DetectorRegistry;
import pw.brock.mmdn.detector.CurseForgeDetector;
import pw.brock.mmdn.detector.MavenDetector;
import pw.brock.mmdn.detector.VersionDiscoverer;
import pw.brock.mmdn.detector.special.FabricMetaDetector;
import pw.brock.mmdn.detector.special.MinecraftDetector;
import pw.brock.mmdn.meta.MetaGenerator;
import pw.brock.mmdn.meta.SourceType;
import pw.brock.mmdn.util.Log;

import org.apache.commons.lang3.NotImplementedException;

/**
 * @author BrockWS
 */
public class Main {

    public static void main(String[] args) {
        Log.info("Starting...");
        // FIXME: Use something much better
        boolean meta = Main.parseArg(args, "meta", false);
        boolean index = Main.parseArg(args, "index", false);
        boolean analyzer = Main.parseArg(args, "analyzer", false);
        boolean cfModpack = Main.parseArg(args, "curseModpack", false);
        boolean cfMod = Main.parseArg(args, "curseMod", false);
        boolean verify = Main.parseArg(args, "verify", false);

        // Global
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

        List<String> params = Main.parseArg(args);
        Log.debug("Parsed Parameters");

        DetectorRegistry.register("maven", new MavenDetector());
        DetectorRegistry.register("curseforge", new CurseForgeDetector());
        DetectorRegistry.register("fabricmeta", new FabricMetaDetector());
        DetectorRegistry.register("minecraft", new MinecraftDetector());

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
                    .addSource(SourceType.ACTIVE, Globals.UPSTREAM_DIR + "/active")
                    .addSource(SourceType.FROZEN, Globals.UPSTREAM_DIR + "/frozen")
                    .run();
        } else if (analyzer) {
            String workingPath = new File("").getAbsolutePath();
            if (cfModpack) {
                if (params.size() != 2) {
                    Log.fatal("Invalid amount of parameters!");
                    System.exit(-1);
                }
                new CursePackAnalyzer(Integer.parseInt(params.get(0)), Integer.parseInt(params.get(1))).run();
            } else if (cfMod) {
                if (params.size() != 2) {
                    Log.fatal("Invalid amount of parameters!");
                    System.exit(-1);
                }
                new CurseModAnalyzer(Integer.parseInt(params.get(0)), Integer.parseInt(params.get(1))).run();
            }
//            if (cfModpack) {
//                // TODO: Move to own class
//                File modpackZip = FileUtil.file(workingPath, "Files", "FTBInteractions-1.5.0-1.12.2.zip");
//                File modsDir = FileUtil.file("C:\\Users\\Brock\\Documents\\MultiMC\\Instances\\FTB Interactions\\minecraft\\mods");
//                String manifest = null;
//                try {
//                    ZipInputStream stream = new ZipInputStream(new FileInputStream(modpackZip));
//                    ZipEntry entry;
//                    while ((entry = stream.getNextEntry()) != null) {
//                        if (!entry.getName().equalsIgnoreCase("manifest.json"))
//                            continue;
//                        manifest = IOUtils.toString(stream, "UTF-8");
//                        break;
//                    }
//                    stream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//                Preconditions.checkNotNull(manifest, "Failed to read manifest.json in {}", modpackZip.getAbsolutePath());
//                CurseModpackManifest curseManifest = JsonUtil.fromJson(manifest, CurseModpackManifest.class);
//
//
//                Map<String, File> files = FileUtil.getFiles(modsDir, true, file -> file.getName().endsWith(".jar"))
//                        .stream()
//                        .collect(Collectors.toMap(File::getName, Function.identity()));
//                Map<File, CurseModpackManifest.File> matches = curseManifest.files
//                        .stream()
//                        .filter(file -> files.containsKey(file.fileData.fileName))
//                        .collect(Collectors.toMap(o -> files.get(o.fileData.fileName), Function.identity()));
//
//                matches.forEach((file, data) -> {
//                    Pair<UpstreamPackage, Version> result;
//                    try {
//                        result = new Analyzer().analyzeFile(file.getAbsolutePath());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        return;
//                    }
//                    UpstreamPackage pack = result.getLeft();
//                    Version version = result.getRight();
//                    if (StringUtils.isEmpty(pack.id))
//                        pack.id = data.addonData.slug;
//                    if (StringUtils.isEmpty(pack.name))
//                        pack.name = data.addonData.name;
//                    // TODO: Add cf project link if missing
//                    if (version.artifacts.stream().noneMatch(artifact -> artifact.id.contains("forgecdn")))
//                        version.artifacts.add(new Version.Artifact("direct", data.fileData.downloadUrl));
//                    if (StringUtils.isEmpty(pack.icon))
//                        pack.icon = data.addonData.avatarUrl;
//                    if (StringUtils.isEmpty(pack.icon))
//                        data.addonData.attachments.stream()
//                                .filter(attachment -> attachment.isDefault)
//                                .findAny()
//                                .ifPresent(attachment -> pack.icon = attachment.url);
//
//                    try {
//                        File path = FileUtil.file(Globals.UPSTREAM_DIR, "active", pack.id);
//                        File packageFile = new File(path, "package.json");
//                        File versionFile = new File(path, version.id + ".json");
//                        if (!path.exists() && !path.mkdirs())
//                            throw new RuntimeException("Failed to mkdirs " + path.getAbsolutePath());
//                        if (Globals.FRESH || !packageFile.exists())
//                            JsonUtil.toJsonFile(packageFile, pack);
//                        if (Globals.FRESH || !versionFile.exists())
//                            JsonUtil.toJsonFile(versionFile, version);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });
//            } else {
//                //String path = "C:\\Users\\Brock\\Projects\\MMDN\\Files\\FTB-Interactions-Mods\\thaumicenergistics-2.2.1.jar";
//                String path = "C:\\Users\\Brock\\Projects\\MMDN\\Files\\FTB-Interactions-Mods";
//                File file = FileUtil.file(path);
//                List<File> files = file.isDirectory() ? FileUtil.getFiles(file, true, f -> f.getName().endsWith(".jar")) : Collections.singletonList(file);
//                files.forEach(f -> {
//                    if (!f.isFile())
//                        throw new RuntimeException(f.getAbsolutePath() + " is not a file!");
//                    try {
//                        Pair<UpstreamPackage, Version> result = Analyzer.analyzeFile(f);
//                        if (result == null || result.getKey() == null || result.getValue() == null) {
//                            Log.error("Failed to analyze file!");
//                        } else {
//                            String versionId = result.getRight().id;
//                            if (versionId == null || versionId.isEmpty() || versionId.equalsIgnoreCase("@VERSION@")) {
//                                Log.warn("Didn't get version id for {}! Got: {}", f.getName(), versionId);
//                                return;
//                            }
//                            Log.info(JsonUtil.gson().toJson(result));
//                        }
//                    } catch (Exception e) {
//                        Log.error("Failed to analyze {}: {}", f.getAbsolutePath(), e.getMessage());
//                    }
//                });
//            }
        } else if (verify) {
            throw new NotImplementedException("Not yet implemented");
        } else {
            Log.error("Specify an argument or something...");
            //GuiApp.launch(GuiApp.class, args);
        }
    }

    private static List<String> parseArg(String[] args) {
        List<String> params = new ArrayList<>(Arrays.asList(args));
        params.removeIf(s -> s.startsWith("--"));
        return params;
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
