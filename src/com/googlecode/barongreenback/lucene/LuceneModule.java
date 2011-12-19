package com.googlecode.barongreenback.lucene;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.totallylazy.records.lucene.LuceneStorage;
import com.googlecode.totallylazy.records.lucene.OptimisedStorage;
import com.googlecode.totallylazy.records.lucene.mappings.Mappings;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;
import org.apache.lucene.store.Directory;

import java.io.PrintStream;

public class LuceneModule implements ApplicationScopedModule, RequestScopedModule{
    public Module addPerApplicationObjects(Container container) {
        container.add(LuceneIndexConfiguration.class);
        container.addActivator(Directory.class, DirectoryActivator.class);
        container.add(LuceneStorage.class, OptimisedStorage.class);
        return this;
    }

    public Module addPerRequestObjects(Container container) {
        container.add(LuceneRecords.class);
        container.addActivator(Records.class, container.getActivator(LuceneRecords.class));
        container.addInstance(Mappings.class, new Mappings().add(Model.class, new ModelMapping()));
        container.addInstance(PrintStream.class, new StringPrintStream());
        return this;
    }
}