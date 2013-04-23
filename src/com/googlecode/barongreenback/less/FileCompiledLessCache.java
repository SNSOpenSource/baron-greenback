package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Strings;

import java.io.File;
import java.util.Date;

import static com.googlecode.totallylazy.Files.fileOption;

public class FileCompiledLessCache implements CompiledLessCache {
    private final File cacheLocation;

    public FileCompiledLessCache(File cacheLocation) {
        this.cacheLocation = cacheLocation;
    }

    @Override
    public Option<CompiledLess> get(String key) {
        return fileOption(cacheLocation, fileNameFor(key)).map(toCached());
    }

    private Mapper<File, CompiledLess> toCached() {
        return new Mapper<File, CompiledLess>() {
            @Override
            public CompiledLess call(File file) throws Exception {
                return new CompiledLess(Strings.toString(file), new Date(file.lastModified()));
            }
        };
    }

    @Override
    public boolean put(String key, CompiledLess result) {
        File file = Files.file(cacheLocation, fileNameFor(key));
        Files.write(Strings.bytes(result.less()), file);
        return file.exists();
    }

    private String fileNameFor(String key) {
        return key + ".css";
    }
}