package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Strings;

import java.io.File;
import java.util.Date;

import static com.googlecode.totallylazy.Files.fileOption;

public class FileLessCssCache implements LessCssCache {
    private final File cacheLocation;

    public FileLessCssCache(File cacheLocation) {
        this.cacheLocation = cacheLocation;
    }

    @Override
    public Option<CachedLessCss> get(String key) {
        return fileOption(cacheLocation, fileNameFor(key)).map(toCached());
    }

    private Mapper<File, CachedLessCss> toCached() {
        return new Mapper<File, CachedLessCss>() {
            @Override
            public CachedLessCss call(File file) throws Exception {
                return new CachedLessCss(Strings.toString(file), new Date(file.lastModified()));
            }
        };
    }

    @Override
    public boolean put(String key, CachedLessCss result) {
        File file = Files.file(cacheLocation, fileNameFor(key));
        Files.write(Strings.bytes(result.less()), file);
        return file.exists();
    }

    private String fileNameFor(String key) {
        return key + ".css";
    }
}