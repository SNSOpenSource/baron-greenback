package com.googlecode.barongreenback;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Files.temporaryDirectory;

public class DirectoryActivator implements Callable<Directory>{
    public Directory call() throws Exception {
        return new NIOFSDirectory(temporaryDirectory());
    }
}
