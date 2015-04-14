package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.lazyrecords.lucene.NameToLuceneDirectoryFunction;

import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.persistence.lucene.LucenePersistence.directoryActivatorFor;

public class NameToLuceneDirectoryFunctionActivator implements Callable<NameToLuceneDirectoryFunction> {

    private final PersistenceUri persistenceUri;

    public NameToLuceneDirectoryFunctionActivator(PersistenceUri persistenceUri) {
        this.persistenceUri = persistenceUri;
    }

    @Override
    public NameToLuceneDirectoryFunction call() throws Exception {
        return new NameToLuceneDirectoryFunction(directoryActivatorFor(persistenceUri.toString()));
    }
}
