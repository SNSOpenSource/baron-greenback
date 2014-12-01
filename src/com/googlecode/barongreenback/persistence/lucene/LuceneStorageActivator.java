package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.SearcherPool;
import com.googlecode.totallylazy.Function3;
import org.apache.lucene.store.Directory;

public abstract class LuceneStorageActivator extends Function3<String, Directory, SearcherPool, LuceneStorage> {
}
