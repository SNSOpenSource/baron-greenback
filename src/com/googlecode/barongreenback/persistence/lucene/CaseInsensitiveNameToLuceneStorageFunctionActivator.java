package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.lazyrecords.lucene.CaseInsensitive;
import com.googlecode.lazyrecords.lucene.ClosingNameToLuceneStorageFunction;
import com.googlecode.lazyrecords.lucene.NameToLuceneDirectoryFunction;
import com.googlecode.lazyrecords.lucene.NameToLuceneStorageFunction;

import java.util.concurrent.Callable;

public class CaseInsensitiveNameToLuceneStorageFunctionActivator implements Callable<NameToLuceneStorageFunction> {

    private final NameToLuceneDirectoryFunction directoryActivator;

    public CaseInsensitiveNameToLuceneStorageFunctionActivator(NameToLuceneDirectoryFunction directoryActivator) {
        this.directoryActivator = directoryActivator;
    }

    @Override
    public NameToLuceneStorageFunction call() throws Exception {
        return new ClosingNameToLuceneStorageFunction(directoryActivator, CaseInsensitive.queryAnalyzer());
    }
}
