package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.persistence.lucene.ModelMapping;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.IgnoreLogger;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;

public class PersistenceModule implements RequestScopedModule {
    public Module addPerRequestObjects(final Container container) {
        container.addInstance(StringMappings.class, new StringMappings().add(Model.class, new ModelMapping()));
        container.add(Logger.class, IgnoreLogger.class);
        return this;
    }

}