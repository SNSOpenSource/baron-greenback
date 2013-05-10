package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceModule;

import java.io.File;

import static com.googlecode.totallylazy.Files.temporaryDirectory;

public class LucenePersistence {
    public static String luceneDirectory(File file) {
        return String.format("%s:%s", PersistenceModule.LUCENE, file.toURI());
    }

    public static String luceneRam() {
        return String.format("%s:%s", PersistenceModule.LUCENE, DirectoryType.Memory);
    }

    public static String luceneTemporaryDirectory(String name) {
        return luceneDirectory(temporaryDirectory(name));
    }
}
