package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.OptimisedStorage;
import com.googlecode.lazyrecords.lucene.mappings.Mappings;
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

    public Module addPerRequestObjects(final Container container) {
        container.addActivator(BaronGreenbackRecords.class, LuceneBaronGreenbackRecordsActivator.class);
        container.addInstance(Mappings.class, new Mappings().add(Model.class, new ModelMapping()));
        container.addInstance(PrintStream.class, new StringPrintStream());
        return this;
    }

}