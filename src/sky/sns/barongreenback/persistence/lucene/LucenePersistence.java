package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.totallylazy.Function1;
import org.apache.lucene.store.Directory;

import java.io.File;

import static com.googlecode.barongreenback.persistence.lucene.DirectoryType.Memory;
import static com.googlecode.barongreenback.persistence.lucene.DirectoryType.Nio;
import static com.googlecode.barongreenback.persistence.lucene.LuceneModule.fileUrl;
import static com.googlecode.barongreenback.persistence.lucene.LuceneModule.lucene;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.mmapDirectory;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.nioDirectory;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.ramDirectory;
import static com.googlecode.totallylazy.Files.temporaryDirectory;

public class LucenePersistence {
    public static String luceneDirectory(File file) {
        return String.format("%s:%s//%s", PersistenceModule.LUCENE, DirectoryType.File.value(), file);
    }

    public static String luceneRam() {
        return String.format("%s:%s", PersistenceModule.LUCENE, DirectoryType.Memory.value());
    }

    public static String luceneTemporaryDirectory(String name) {
        return luceneDirectory(temporaryDirectory(name));
    }

    public static Function1<String, Directory> directoryActivatorFor(String uri) {
        if (uri.startsWith(lucene(Memory))) {
            return ramDirectory();
        }
        if (uri.startsWith(lucene(Nio))) {
            return nioDirectory(new File(fileUrl(uri)));
        }
        if (uri.startsWith(lucene(DirectoryType.File))) {
            return mmapDirectory(new File(fileUrl(uri)));
        }
        throw new UnsupportedOperationException();
    }
}
