package com.googlecode.barongreenback.lucene;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Files.TEMP_DIR;

public class DirectoryActivator implements Callable<Directory>, Closeable{
    public static final File DEFAULT_DIRECTORY = new File(TEMP_DIR, "baron-greenback");
    private Directory directory;

    public Directory call() throws Exception {
        directory = new NIOFSDirectory(DEFAULT_DIRECTORY);
        return directory;
    }

    public void close() throws IOException {
        directory.close();
    }
}
