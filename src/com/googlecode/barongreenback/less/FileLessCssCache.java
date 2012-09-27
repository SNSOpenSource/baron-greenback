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
    public boolean containsKey(String key) {
        return new File(cacheLocation, fileNameFor(key)).exists();
    }

    @Override
    public String get(String key) {
        return Strings.toString(Files.file(cacheLocation, fileNameFor(key)));
    }

    @Override
    public void put(String key, String result) {
        File file = Files.file(cacheLocation, fileNameFor(key));
        Files.write(file).apply(new ByteArrayInputStream(result.getBytes()));
    }

    private String fileNameFor(String key) {
        return "cache-" + key + ".css";
    }
}