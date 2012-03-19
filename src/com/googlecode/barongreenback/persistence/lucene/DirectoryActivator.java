package com.googlecode.barongreenback.persistence.lucene;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

public class DirectoryActivator implements Callable<Directory>, Closeable{
    private Directory directory;
    private final LuceneIndexConfiguration luceneIndexConfiguration;

    public DirectoryActivator(LuceneIndexConfiguration luceneIndexConfiguration) {
        this.luceneIndexConfiguration = luceneIndexConfiguration;
    }

    public Directory call() throws Exception {
        if (luceneIndexConfiguration.getIndexType() == LuceneIndexType.FILESYSTEM) {
            return directory = new NIOFSDirectory(luceneIndexConfiguration.getDirectory());
        } else {
            return directory = new RAMDirectory();
        }
    }

    public void close() throws IOException {
        directory.close();
    }
}
