package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.barongreenback.persistence.PersistenceUri;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Files.temporaryDirectory;
import static com.googlecode.totallylazy.URLs.uri;

public class DirectoryActivator implements Callable<Directory>, Closeable {
    public static final String FILE = "file";
    public static final String MEMORY = "mem";
    private final String persistenceUri;
    private Directory directory;

    public DirectoryActivator(PersistenceUri uri) {
        this.persistenceUri = uri.toString();
    }

    public Directory call() throws Exception {
        if (persistenceUri.startsWith(prefix(MEMORY))) {
            return directory = new RAMDirectory();
        }
        if (persistenceUri.startsWith(prefix(FILE))) {
            URI fileUri = extractFileUri();
            return directory = new NIOFSDirectory(new File(fileUri));
        }
        throw new UnsupportedOperationException();
    }

    private URI extractFileUri() {
        return uri(persistenceUri.substring(PersistenceModule.LUCENE.length() + 1));
    }

    private String prefix(String name) {
        return String.format("%s:%s", PersistenceModule.LUCENE, name);
    }

    public void close() throws IOException {
        directory.close();
    }

}
