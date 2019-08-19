package pw.brock.mmdn.util;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import pw.brock.mmdn.models.IDataModel;
import pw.brock.mmdn.models.Version;
import pw.brock.mmdn.util.gson.SidedOverrideSerializer;
import pw.brock.mmdn.util.gson.VersionSerializer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * @author BrockWS
 */
@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class FileUtil {

    public static File file(Object... o) {
        return FileUtils.getFile(Arrays.stream(o).map(Object::toString).toArray(String[]::new));
    }

    public static boolean isFile(Object... path) {
        return FileUtil.file(path).isFile();
    }

    public static boolean isDir(Object... path) {
        return FileUtil.file(path).isDirectory();
    }

    public static File removeExtension(Object... s) {
        return FileUtil.removeExtension(FileUtil.file(s));
    }

    public static File removeExtension(File file) {
        String dir = file.getParent();
        String name = file.getName();
        return FileUtil.file(dir, name.substring(0, name.lastIndexOf(".")));
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

    public static boolean delete(Object... path) {
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

    public static boolean unzipFile(Object... file) {
        return FileUtil.unzipFile(FileUtil.file(file));
    }

    public static boolean unzipFile(File file) {
        Objects.requireNonNull(file);
        if (!file.isFile())
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a file!");
        File dest = FileUtil.removeExtension(file);
        if (dest.exists())
            throw new RuntimeException("Invalid Path! " + dest.getAbsolutePath());
        try {
            ZipFile zipFile = new ZipFile(file);
            zipFile.stream().forEach(entry -> {
                try {
                    File destEntry = new File(dest, entry.getName());
                    if (entry.isDirectory()) {
                        destEntry.mkdirs();
                    } else {
                        destEntry.getParentFile().mkdirs();
                        InputStream in = zipFile.getInputStream(entry);
                        OutputStream out = new FileOutputStream(destEntry);
                        IOUtils.copy(in, out);
                        in.close();
                        out.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            zipFile.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
