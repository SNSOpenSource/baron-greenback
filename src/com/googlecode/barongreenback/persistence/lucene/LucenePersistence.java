package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.lazyrecords.lucene.LuceneStorage;

import java.io.IOException;

public class LucenePersistence implements Persistence{
    private final LuceneStorage luceneStorage;

    public LucenePersistence(LuceneStorage luceneStorage) {
        this.luceneStorage = luceneStorage;
    }

    @Override
    public void deleteAll() throws IOException {
        luceneStorage.deleteAll();
    }
}
