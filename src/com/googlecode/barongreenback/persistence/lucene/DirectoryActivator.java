package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.totallylazy.Files;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

public class DirectoryActivator implements Callable<Directory>, Closeable{
    private Directory directory;
    private final PersistenceUri uri;

    public DirectoryActivator(PersistenceUri uri) {
        this.uri = uri;
    }

    public Directory call() throws Exception {
        if(uri.toString().startsWith("lucene:mem")){
            return directory = new RAMDirectory();
        }
        if(uri.toString().startsWith("lucene:tmp")){
            return directory = new NIOFSDirectory(Files.temporaryDirectory());
        }
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        directory.close();
    }
}
