package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.lazyrecords.lucene.LuceneStorage;

import java.io.File;
import java.io.IOException;

import static com.googlecode.totallylazy.Files.temporaryDirectory;

public class LucenePersistence implements Persistence{
    private final LuceneStorage luceneStorage;

    public LucenePersistence(LuceneStorage luceneStorage) {
        this.luceneStorage = luceneStorage;
    }

    public static String luceneDirectory(File file) {
        return String.format("%s:%s://%s", PersistenceModule.LUCENE, DirectoryActivator.FILE, file);
    }

    public static String luceneRam() {
        return String.format("%s:%s", PersistenceModule.LUCENE, DirectoryActivator.MEMORY);
    }

    public static String luceneTemporaryDirectory(String name) {
        return luceneDirectory(temporaryDirectory(name));
    }

    @Override
    public void deleteAll() throws IOException {
        luceneStorage.deleteAll();
    }
}
