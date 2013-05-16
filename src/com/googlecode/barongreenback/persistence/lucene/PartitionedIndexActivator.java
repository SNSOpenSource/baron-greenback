package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.lazyrecords.lucene.LucenePartitionedIndex;
import com.googlecode.lazyrecords.lucene.PartitionedIndex;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.persistence.lucene.DirectoryType.File;
import static com.googlecode.barongreenback.persistence.lucene.DirectoryType.Memory;
import static com.googlecode.barongreenback.persistence.lucene.DirectoryType.Nio;
import static com.googlecode.barongreenback.persistence.lucene.LuceneModule.fileUrl;
import static com.googlecode.barongreenback.persistence.lucene.LuceneModule.lucene;
import static com.googlecode.lazyrecords.lucene.LucenePartitionedIndex.partitionedIndex;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.nioDirectory;

public class PartitionedIndexActivator implements Callable<PartitionedIndex>, Closeable {

    private final String persistenceUri;
    private LucenePartitionedIndex partitionedIndex;

    public PartitionedIndexActivator(PersistenceUri uri) {
        this.persistenceUri = uri.toString();
    }

    public PartitionedIndex call() throws Exception {
        if (persistenceUri.startsWith(lucene(Memory))) {
            return partitionedIndex = partitionedIndex();
        }
        if (persistenceUri.startsWith(lucene(Nio))) {
            return partitionedIndex = partitionedIndex(nioDirectory(new File(fileUrl(persistenceUri))));
        }
        if (persistenceUri.startsWith(lucene(File))) {
            return partitionedIndex = partitionedIndex(new File(fileUrl(persistenceUri)));
        }
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        partitionedIndex.close();
    }
}