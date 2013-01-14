package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Strings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Date;

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
    public CachedLessCss get(String key) {
        File file = Files.file(cacheLocation, fileNameFor(key));
        return new CachedLessCss(Strings.toString(file), new Date(file.lastModified()));
    }

    @Override
    public void put(String key, CachedLessCss result) {
        File file = Files.file(cacheLocation, fileNameFor(key));
        Files.write(file).apply(new ByteArrayInputStream(result.less().getBytes()));
    }

    private String fileNameFor(String key) {
        return "cache-" + key + ".css";
    }
}