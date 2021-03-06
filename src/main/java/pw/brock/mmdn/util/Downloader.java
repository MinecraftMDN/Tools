package pw.brock.mmdn.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import pw.brock.mmdn.Globals;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.XmlObjectParser;

/**
 * @author BrockWS
 */
public class Downloader {

    private static final Map<String, HttpRequestFactory> requestFactories = new HashMap<>();
    private static HttpTransport httpTransport;

    public static HttpRequestFactory getRequestFactory(String parser) {
        if (parser != null)
            parser = parser.toLowerCase();
        if (httpTransport == null)
            Downloader.httpTransport = new NetHttpTransport();

        if (!Downloader.requestFactories.containsKey(parser)) {
            HttpRequestFactory factory;
            if ("gson".equals(parser)) {
                factory = Downloader.httpTransport.createRequestFactory(request -> {
                    Downloader.addDefaultHeaders(request);
                    request.setParser(GsonFactory.getDefaultInstance().createJsonObjectParser());
                });
            } else if ("xml".equalsIgnoreCase(parser)) {
                factory = Downloader.httpTransport.createRequestFactory(request -> {
                    Downloader.addDefaultHeaders(request);
                    request.setParser(new XmlObjectParser(new XmlNamespaceDictionary().set("", "")));
                });
            } else {
                factory = Downloader.httpTransport.createRequestFactory(Downloader::addDefaultHeaders);
            }
            Downloader.requestFactories.put(parser, factory);
        }
        return Downloader.requestFactories.get(parser);
    }

    public static HttpResponse get(String factoryName, GenericUrl url) {
        Log.trace("Requesting {}", url.build());
        Preconditions.checkArgument(!url.build().isEmpty(), "URL is empty!");
        HttpRequestFactory factory = Downloader.getRequestFactory(factoryName);
        try {
            HttpRequest request = factory.buildGetRequest(url);
            HttpResponse response = request.execute();
            if (response.getHeaders().containsKey("x-cache-status")) {
                Log.trace("Cache Status: {}", response.getHeaders().get("x-cache-status"));
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T get(String factoryName, GenericUrl url, Class<T> clazz) {
        HttpResponse response = Downloader.get(factoryName, url);
        if (response == null)
            throw new NullPointerException("Failed to get HTTP response: " + factoryName);
        try {
            return response.parseAs(clazz);
        } catch (IOException e) {
            Log.error("Failed to request url as " + factoryName);
            e.printStackTrace();
        }
        return null;
    }

    public static HttpResponse post(String factoryName, GenericUrl url, HttpContent content) {
        String urlString = url.build();
        Log.trace("Requesting {}", urlString);
        Preconditions.checkArgument(!urlString.isEmpty(), "URL is empty!");
        HttpRequestFactory factory = Downloader.getRequestFactory(factoryName);
        try {
            HttpRequest request = factory.buildPostRequest(url, content);
            HttpResponse response = request.execute();
            if (response.getHeaders().containsKey("x-cache-status")) {
                Log.trace("Cache Status: {}", response.getHeaders().get("x-cache-status"));
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T post(String factoryName, GenericUrl url, HttpContent content, Class<T> clazz) {
        HttpResponse response = Downloader.post(factoryName, url, content);
        if (response == null)
            throw new NullPointerException("Failed to get HTTP response: " + factoryName);
        try {
            return response.parseAs(clazz);
        } catch (IOException e) {
            Log.error("Failed to request url as " + factoryName);
            e.printStackTrace();
        }
        return null;
    }

    public static HttpResponse head(GenericUrl url) {
        String urlString = url.build();
        Log.trace("Requesting {}", urlString);
        Preconditions.checkArgument(!urlString.isEmpty(), "URL is empty!");
        HttpRequestFactory factory = Downloader.getRequestFactory("");
        try {
            HttpRequest request = factory.buildHeadRequest(url);
            HttpResponse response = request.execute();
            if (response.getHeaders().containsKey("x-cache-status")) {
                Log.trace("Cache Status: {}", response.getHeaders().get("x-cache-status"));
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getString(GenericUrl url) {
        HttpResponse response = Downloader.get("", url);
        if (response == null)
            throw new NullPointerException("Failed to get HTTP response: " + url.build());
        try {
            return response.parseAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean getFile(GenericUrl url, File saveAs) {
        HttpResponse response = Downloader.get("", url);
        if (response == null)
            throw new NullPointerException("Failed to get HTTP response: " + url.build());

        if (!saveAs.getParentFile().exists() && !saveAs.getParentFile().mkdirs())
            throw new RuntimeException("Failed to create directory " + saveAs.getParent());

        try (OutputStream stream = new FileOutputStream(saveAs)) {
            response.download(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saveAs.exists();
    }

    public static HttpResponse getGson(GenericUrl url) {
        return Downloader.get("gson", url);
    }

    public static HttpResponse getXml(GenericUrl url) {
        return Downloader.get("xml", url);
    }

    public static <T> T getGson(GenericUrl url, Class<T> clazz) {
        return Downloader.get("gson", url, clazz);
    }

    public static <T> T getXml(GenericUrl url, Class<T> clazz) {
        return Downloader.get("xml", url, clazz);
    }

    public static <T> T postGson(GenericUrl url, Object data, Class<T> clazz) {
        HttpContent content;
        if (data instanceof String)
            content = ByteArrayContent.fromString("application/json", (String) data);
        else
            content = new JsonHttpContent(GsonFactory.getDefaultInstance(), data);
        return Downloader.post("gson", url, content, clazz);
    }

    public static File getFileMaven(String url, String id, String dir) {
        File path = new File(dir, MavenUtil.toMavenPath(id));
        return Downloader.getFile(Downloader.buildUrl(MavenUtil.toMavenUrl(url, id)), path) ? path : null;
    }

    public static String combineUrl(String... paths) {
        return Stream.of(paths).reduce((base, extra) -> {
            if (!base.endsWith("/") && !extra.startsWith("/")) // No ending or starting slash
                base += "/" + extra;
            else if (base.endsWith("/") && extra.startsWith("/")) // Both ending and starting slash
                base += extra.substring(1);
            else
                base += extra;
            return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        }).orElseThrow(RuntimeException::new);
    }

    public static GenericUrl buildUrl(String... paths) {
        AtomicReference<String> url = new AtomicReference<>(paths.length == 1 ? paths[0] : Downloader.combineUrl(paths));
        Globals.MIRRORS.forEach((k, v) -> url.set(url.get().replace(k, v)));
        return new GenericUrl(url.get().replace("+", "%2B"));
    }

    private static void addDefaultHeaders(HttpRequest request) {
        request.getHeaders()
                .setUserAgent("MinecraftMDN/v1 (https://github.com/MinecraftMDN)");
    }
}
