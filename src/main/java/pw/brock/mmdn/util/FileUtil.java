package pw.brock.mmdn.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * @author BrockWS
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FileUtil {

    public static File file(String... s) {
        return FileUtils.getFile(s);
    }

    public static boolean isFile(String path) {
        return FileUtil.file(path).isFile();
    }

    public static boolean isDir(String path) {
        return FileUtil.file(path).isDirectory();
    }

    public static List<File> getFiles(String path) {
        return FileUtil.getFiles(path, false, null);
    }

    public static List<File> getFiles(File dir) {
        return FileUtil.getFiles(dir, false, null);
    }

    public static List<File> getFiles(String path, boolean recursive) {
        return FileUtil.getFiles(path, recursive, null);
    }

    public static List<File> getFiles(File dir, boolean recursive) {
        return FileUtil.getFiles(dir, recursive, null);
    }

    public static List<File> getFiles(String path, Predicate<File> filter) {
        return FileUtil.getFiles(path, false, filter);
    }

    public static List<File> getFiles(File dir, Predicate<File> filter) {
        return FileUtil.getFiles(dir, false, filter);
    }

    public static List<File> getFiles(String path, boolean recursive, Predicate<File> filter) {
        return FileUtil.getFiles(FileUtil.file(path), recursive, filter);
    }

    public static List<File> getFiles(File dir, boolean recursive, Predicate<File> filter) {
        return new ArrayList<>(FileUtils.listFiles(dir, new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                if (filter != null)
                    return file.isFile() && filter.test(file);
                return file.isFile();
            }
        }, recursive ? TrueFileFilter.INSTANCE : null));
    }

    public static List<File> getDirs(String path) {
        return FileUtil.getDirs(path, false, null);
    }

    public static List<File> getDirs(String path, Predicate<File> filter) {
        return FileUtil.getDirs(path, false, filter);
    }

    public static List<File> getDirs(String path, boolean recursive) {
        return FileUtil.getDirs(path, recursive, null);
    }

    public static List<File> getDirs(String path, boolean recursive, Predicate<File> filter) {
        File search = FileUtil.file(path);
        Collection<File> files = FileUtils.listFilesAndDirs(search, new NotFileFilter(TrueFileFilter.INSTANCE), new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                if (!recursive && !file.getParentFile().equals(search))
                    return false;
                return filter == null || filter.test(file);
            }
        });
        files.remove(search);
        return Lists.newArrayList(files);
    }

    public static boolean copyFile(File src, File dest) {
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(dest);
        try {
            FileUtils.copyFile(src, dest);
            Log.trace("Util#copyFile {} to {}", src.getAbsolutePath(), dest.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.error("Failed to copy file {} to {}", src.getAbsolutePath(), dest.getAbsolutePath());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean delete(String... path){
        Preconditions.checkNotNull(path);
        return FileUtil.delete(FileUtil.file(path));
    }

    public static boolean delete(File path) {
        Preconditions.checkNotNull(path);
        try {
            if (path.isFile())
                return path.delete();
            FileUtils.deleteDirectory(path);
            return true;
        } catch (IOException e) {
            Log.error("Failed to delete directory {}", path.getAbsolutePath());
            e.printStackTrace();
        }
        return false;
    }
}
