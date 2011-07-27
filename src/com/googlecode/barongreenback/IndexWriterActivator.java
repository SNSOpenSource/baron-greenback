package com.googlecode.barongreenback;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

public class IndexWriterActivator implements Callable<IndexWriter>, Closeable {
    private final Directory directory;
    private final Version version;
    private final Analyzer analyzer;
    private IndexWriter indexWriter;

    public IndexWriterActivator(Directory directory, Version version, Analyzer analyzer) {
        this.directory = directory;
        this.version = version;
        this.analyzer = analyzer;
    }

    public IndexWriter call() throws Exception {
        indexWriter = new IndexWriter(directory, new IndexWriterConfig(version, analyzer));
        return indexWriter;
    }

    public void close() throws IOException {
        indexWriter.close();
    }
}
