package pw.brock.mmdn.util;

import java.io.File;

import pw.brock.mmdn.Globals;
import pw.brock.mmdn.models.curse.CurseAddonFile;
import pw.brock.mmdn.models.curse.CurseAddonFiles;
import pw.brock.mmdn.models.curse.CurseApiFingerprint;

import com.google.api.client.http.GenericUrl;
import com.google.common.base.Preconditions;

/**
 * @author BrockWS
 */
public class CurseApi {

    private static final String URL_GET_ADDON_BY_FINGERPRINT = "https://addons-ecs.forgesvc.net/api/v2/fingerprint";
    private static final String URL_GET_ADDON_FILES = "https://addons-ecs.forgesvc.net/api/v2/addon/{addonID}/files";
    private static final String URL_GET_ADDON_FILE = "https://addons-ecs.forgesvc.net/api/v2/addon/{addonID}/file/{fileID}";
    private static final String URL_GET_ADDON_FILE_DOWNLOAD_URL = "https://addons-ecs.forgesvc.net/api/v2/addon/{addonID}/file/{fileID}/download-url";

    public static CurseApiFingerprint searchByFingerprint(String... fingerprint) {
        return Downloader.postGson(Downloader.buildUrl(URL_GET_ADDON_BY_FINGERPRINT), fingerprint, CurseApiFingerprint.class);
    }

    public static CurseAddonFiles getAddonFiles(Object addonID) {
        return Downloader.getGson(CurseApi.urlReplace(URL_GET_ADDON_FILES,
                "addonID", addonID),
                CurseAddonFiles.class);
    }

    public static CurseAddonFile getAddonFile(Object addonID, Object fileID) {
        return Downloader.getGson(CurseApi.urlReplace(URL_GET_ADDON_FILE,
                "addonID", addonID,
                "fileID", fileID),
                CurseAddonFile.class);
    }

    public static String getAddonFileDownloadUrl(Object addonID, Object fileID) {
        return Downloader.getString(CurseApi.urlReplace(URL_GET_ADDON_FILE_DOWNLOAD_URL,
                "addonID", addonID,
                "fileID", fileID));
    }

    public static File getOrDownloadAddonFile(Object addonID, Object fileID) {
        File file = FileUtil.file(Globals.CACHE, "curseforge-mods", String.valueOf(addonID), String.valueOf(fileID));
        if (!file.exists()) {
            // Download
            String url = CurseApi.getAddonFileDownloadUrl(addonID, fileID);
            Downloader.getFile(Downloader.buildUrl(url), file);
        }
        return file;
    }

    private static GenericUrl urlReplace(String url, Object... args) {
        Preconditions.checkArgument(args.length % 2 == 0, "Invalid amount of arguments!");
        for (int i = 0; i < args.length; i += 2) {
            url = url.replace("{" + args[i] + "}", String.valueOf(args[i + 1]));
        }
        return Downloader.buildUrl(url);
    }
}
