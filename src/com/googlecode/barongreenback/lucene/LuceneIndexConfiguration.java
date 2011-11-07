package com.googlecode.barongreenback.lucene;

import com.googlecode.totallylazy.Files;

import java.io.File;
import java.util.Properties;

public class LuceneIndexConfiguration {

    public static final String LUCENE_INDEX_TYPE = "baron-greenback.lucene.index.type";
    public static final String LUCENE_INDEX_DIRECTORY = "baron-greenback.lucene.index.directory";
    public static final LuceneIndexType DEFAULT_TYPE = LuceneIndexType.FILESYSTEM;
    public static String DEFAULT_DIRECTORY = new File(Files.TEMP_DIR, "baron-greenback").getAbsolutePath();
    private final LuceneIndexType indexType;
    private final File directory;

    public LuceneIndexConfiguration(Properties properties) {
        this(propertiesToIndexType(properties), properties.getProperty(LUCENE_INDEX_DIRECTORY, DEFAULT_DIRECTORY));
    }

    private static LuceneIndexType propertiesToIndexType(Properties properties) {
        return LuceneIndexType.valueOf(properties.getProperty(LUCENE_INDEX_TYPE, DEFAULT_TYPE.name()));
    }

    public LuceneIndexConfiguration(LuceneIndexType indexType, String directory) {
        this(indexType, new File(directory));
    }

    public LuceneIndexConfiguration(LuceneIndexType indexType, File directory) {
        this.indexType = indexType;
        this.directory = directory;
    }

    public LuceneIndexConfiguration() {
        this(DEFAULT_TYPE, new File(DEFAULT_DIRECTORY));
    }

    public LuceneIndexType getIndexType() {
        return indexType;
    }

    public File getDirectory() {
        return directory;
    }
}
