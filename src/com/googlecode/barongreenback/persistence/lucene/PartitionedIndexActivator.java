package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.lazyrecords.lucene.LucenePartitionedIndex;
import com.googlecode.lazyrecords.lucene.PartitionedIndex;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;

import static com.googlecode.lazyrecords.lucene.LucenePartitionedIndex.partitionedIndex;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.nioDirectory;
import static com.googlecode.totallylazy.URLs.uri;

public class PartitionedIndexActivator implements Callable<PartitionedIndex>, Closeable {

    private final String persistenceUri;
    private LucenePartitionedIndex partitionedIndex;

    public PartitionedIndexActivator(PersistenceUri uri) {
        this.persistenceUri = uri.toString();
    }

    public PartitionedIndex call() throws Exception {
        if (persistenceUri.startsWith(prefix(DirectoryType.Memory.value()))) {
            return partitionedIndex = partitionedIndex();
        }
        if (persistenceUri.startsWith(prefix(DirectoryType.Nio.value()))) {
            URI fileUri = extractFileUri();
            return partitionedIndex = partitionedIndex(nioDirectory(new File(fileUri)));
        }
        if (persistenceUri.startsWith(prefix(DirectoryType.File.value()))) {
            URI fileUri = extractFileUri();
            return partitionedIndex = partitionedIndex(new File(fileUri));
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
        partitionedIndex.close();
    }

}
