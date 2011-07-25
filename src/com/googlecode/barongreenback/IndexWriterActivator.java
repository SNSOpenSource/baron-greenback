package com.googlecode.barongreenback;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.util.concurrent.Callable;

public class IndexWriterActivator implements Callable<IndexWriter> {
    private final Directory directory;
    private final Version version;

    public IndexWriterActivator(Directory directory, Version version) {
        this.directory = directory;
        this.version = version;
    }

    public IndexWriter call() throws Exception {
        return new IndexWriter(directory, new IndexWriterConfig(version, new StandardAnalyzer(version)));
    }
}
