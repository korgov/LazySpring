package ru.korgov.util;

import ru.korgov.util.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 12/18/11
 */
public class UrlUtils {

    public static String sendPostRequest(final String url, final String query) throws IOException {
        final URLConnection urlConnection = createPostConnection(url);
        write(urlConnection, query);
        return read(urlConnection);
    }

    public static URLConnection createPostConnection(final String url) throws IOException {
        final URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.setDoOutput(true);
        return urlConnection;
    }

    public static void write(final URLConnection urlConnection, final String data) throws IOException {
        IOUtils.write(urlConnection.getOutputStream(), data);
    }

    public static String read(final URLConnection urlConnection) throws IOException {
        return IOUtils.read(urlConnection.getInputStream());
    }

}
