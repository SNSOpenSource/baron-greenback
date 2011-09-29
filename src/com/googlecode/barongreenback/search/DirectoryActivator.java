package com.googlecode.barongreenback.search;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Files.temporaryDirectory;

public class DirectoryActivator implements Callable<Directory>, Closeable{
    private Directory directory;

    public Directory call() throws Exception {
        File path = temporaryDirectory("baron-greenback");
        directory = new NIOFSDirectory(path);
        return directory;
    }

    public void close() throws IOException {
        directory.close();
    }
}
