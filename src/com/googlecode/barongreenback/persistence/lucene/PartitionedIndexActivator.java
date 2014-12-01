package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.lazyrecords.lucene.LucenePartitionedIndex;
import com.googlecode.lazyrecords.lucene.PartitionedIndex;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.persistence.lucene.LucenePersistence.directoryActivatorFor;
import static com.googlecode.lazyrecords.lucene.LucenePartitionedIndex.partitionedIndex;

public class PartitionedIndexActivator implements Callable<PartitionedIndex>, Closeable {

    private final String persistenceUri;
    private LucenePartitionedIndex partitionedIndex;
    private final LuceneStorageActivator luceneStorageActivator;

    public PartitionedIndexActivator(PersistenceUri uri, LuceneStorageActivator luceneStorageActivator) {
        this.luceneStorageActivator = luceneStorageActivator;
        this.persistenceUri = uri.toString();
    }

    public PartitionedIndex call() throws Exception {
        return partitionedIndex = partitionedIndex(directoryActivatorFor(persistenceUri), luceneStorageActivator);
    }

    public void close() throws IOException {
        partitionedIndex.close();
    }
}