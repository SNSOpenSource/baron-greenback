package com.googlecode.barongreenback.lucene;

import com.googlecode.totallylazy.Files;

import java.io.File;
import java.util.Properties;

public class LuceneIndexDirectory {

    public static final String LUCENE_INDEX_DIRECTORY = "baron-greenback.lucene.index.directory";
    public static String DEFAULT = Files.temporaryDirectory("baron-greenback").getAbsolutePath();
    private File value;

    public LuceneIndexDirectory(Properties properties) {
        this(properties.getProperty(LUCENE_INDEX_DIRECTORY, DEFAULT));
    }

    public LuceneIndexDirectory(String directory) {
        this(new File(directory));
    }

    public LuceneIndexDirectory(File directory) {
        this.value = directory;
    }

    public LuceneIndexDirectory() {
        this(new File(DEFAULT));
    }

    public File value() {
        return value;
    }
}
