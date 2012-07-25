package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.SearcherPool;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function2;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Files.temporaryDirectory;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.Zip.unzip;
import static com.googlecode.totallylazy.Zip.zip;

public class LucenePersistence implements Persistence {
    private final LuceneStorage luceneStorage;

    public LucenePersistence(LuceneStorage luceneStorage) {
        this.luceneStorage = luceneStorage;
    }

    public static String luceneDirectory(File file) {
        return String.format("%s:%s", PersistenceModule.LUCENE, file.toURI());
    }

    public static String luceneRam() {
        return String.format("%s:%s", PersistenceModule.LUCENE, DirectoryActivator.MEMORY);
    }

    public static String luceneTemporaryDirectory(String name) {
        return luceneDirectory(temporaryDirectory(name));
    }

    @Override
    public void delete() throws IOException {
        luceneStorage.deleteAll();
    }

    @Override
    public void backup(File file) throws Exception {
        luceneStorage.backup(file);
    }

    @Override
    public void restore(File source) throws Exception {
        luceneStorage.restore(source);
    }
}
