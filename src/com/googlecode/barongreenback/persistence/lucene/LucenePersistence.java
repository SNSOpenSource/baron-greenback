package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Runnables;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Files.temporaryDirectory;

public class LucenePersistence implements Persistence {
    private final LuceneStorage luceneStorage;
    private final Directory directory;

    public LucenePersistence(LuceneStorage luceneStorage, Directory directory) {
        this.luceneStorage = luceneStorage;
        this.directory = directory;
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
    public void deleteAll() throws IOException {
        luceneStorage.deleteAll();
    }

    @Override
    public void backup(File file) throws Exception {
        Files.delete(file);
        using(new NIOFSDirectory(file), new Callable1<Directory, Void>() {
            @Override
            public Void call(Directory destination) throws Exception {
                for (String segment : directory.listAll()) {
                    directory.copy(destination, segment, segment);
                }
                return Runnables.VOID;
            }
        });
    }
}
