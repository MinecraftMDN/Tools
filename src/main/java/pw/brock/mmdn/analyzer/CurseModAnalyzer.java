package pw.brock.mmdn.analyzer;

import java.io.File;

import pw.brock.mmdn.models.UpstreamPackage;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.util.CurseApi;
import pw.brock.mmdn.util.JsonUtil;
import pw.brock.mmdn.util.Log;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;

/**
 * WIP
 *
 * @author BrockWS
 */
public class CurseModAnalyzer implements Runnable {

    private int projectID;
    private int fileID;
    private File file;

    public CurseModAnalyzer(int projectID, int fileID) {
        Preconditions.checkArgument(projectID > -1);
        Preconditions.checkArgument(fileID > -1);
        this.projectID = projectID;
        this.fileID = fileID;
    }

    @Override
    public void run() {
        this.file = CurseApi.getOrDownloadAddonFile(this.projectID, this.fileID);
        Pair<UpstreamPackage, Version> pair = Analyzer.analyzeFile(this.file);
        Log.info(JsonUtil.toJson(pair.getLeft()));
        Log.info(JsonUtil.toJson(pair.getRight()));
    }
}
