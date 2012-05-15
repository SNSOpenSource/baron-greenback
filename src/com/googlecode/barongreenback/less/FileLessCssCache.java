package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;

import java.io.ByteArrayInputStream;
import java.io.File;

public class FileLessCssCache implements LessCssCache {
    private final File cacheLocation;

    public FileLessCssCache(File cacheLocation) {
        this.cacheLocation = cacheLocation;
    }

    @Override
    public boolean containsKey(Uri uri) {
        return new File(cacheLocation, fileNameFor(uri)).exists();
    }

    @Override
    public String get(Uri uri) {
        return Strings.toString(Files.file(cacheLocation, fileNameFor(uri)));
    }

    @Override
    public void put(Uri uri, String result) {
        File file = Files.file(cacheLocation, fileNameFor(uri));
        Files.write(file).apply(new ByteArrayInputStream(result.getBytes()));
    }

    private String fileNameFor(Uri uri) {
        return "cache-" + String.valueOf(uri.hashCode()) + ".css";
    }
}