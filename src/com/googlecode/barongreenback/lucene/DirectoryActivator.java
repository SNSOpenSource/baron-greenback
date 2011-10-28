package com.googlecode.barongreenback.lucene;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Files.TEMP_DIR;

public class DirectoryActivator implements Callable<Directory>, Closeable{
    private Directory directory;
    private final LuceneIndexDirectory luceneIndexDirectory;

    public DirectoryActivator(LuceneIndexDirectory luceneIndexDirectory) {
        this.luceneIndexDirectory = luceneIndexDirectory;
    }

    public Directory call() throws Exception {
        directory = new NIOFSDirectory(luceneIndexDirectory.value());
        return directory;
    }

    public void close() throws IOException {
        directory.close();
    }
}
