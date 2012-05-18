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
    private final Directory directory;
    private final SearcherPool searcherPool;

    public LucenePersistence(LuceneStorage luceneStorage, Directory directory, SearcherPool searcherPool) {
        this.luceneStorage = luceneStorage;
        this.directory = directory;
        this.searcherPool = searcherPool;
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
        File folder = unzippedName(file);
        Files.delete(folder);
        using(directoryFor(folder), copy().apply(directory));
        zip(folder, file);
        Files.delete(folder);
    }

    @Override
    public void restore(File source) throws Exception {
        luceneStorage.deleteAll();
        deleteAllSegments(directory);
        using(directoryFor(unzipIfNeeded(source)), copy().flip().apply(directory));
    }

    private File unzipIfNeeded(File source) throws IOException {
        if (source.isFile()) {
            File unzipped = unzippedName(source);
            unzip(source, unzipped);
            return unzipped;
        }
        return source;
    }

    private void deleteAllSegments(Directory directory) throws IOException {
        for (String segment : directory.listAll()) {
            directory.deleteFile(segment);
        }
    }

    private Directory directoryFor(File file) throws IOException {
        return new NIOFSDirectory(file);
    }

    public static Function2<Directory, Directory, Void> copy() {
        return new Function2<Directory, Directory, Void>() {
            @Override
            public Void call(Directory source, Directory destination) throws Exception {
                copy(source, destination);
                return VOID;
            }
        };
    }

    public static void copy(Directory source, Directory destination) throws IOException {
        for (String segment : source.listAll()) {
            source.copy(destination, segment, segment);
        }
    }

    private File unzippedName(File file) {
        return new File(file.toString() + ".unzipped");
    }

}
