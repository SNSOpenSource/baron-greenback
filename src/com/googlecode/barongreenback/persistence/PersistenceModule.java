package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.persistence.lucene.LuceneModule;
import com.googlecode.barongreenback.persistence.lucene.ModelMapping;
import com.googlecode.barongreenback.persistence.sql.SqlModule;
import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.IgnoreLogger;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.PrefixProperties;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceModule implements ApplicationScopedModule, RequestScopedModule {
    public Module addPerRequestObjects(final Container container) throws Exception {
        container.addInstance(StringMappings.class, new StringMappings().add(Model.class, new ModelMapping()));
        container.add(Logger.class, IgnoreLogger.class);
        container.add(PersistenceRequestScope.class);
        container.addActivator(BaronGreenbackRecords.class, BaronGreenbackRecordsActivator.class);
        return this;
    }

    @Override
    public Module addPerApplicationObjects(Container container) throws Exception {
        container.add(PersistenceProperties.class);
        container.add(PersistenceUri.class);
        container.add(PersistenceUser.class);
        container.add(PersistencePassword.class);
        container.add(PersistenceApplicationScope.class);
        container.addActivator(Persistence.class, PersistenceActivator.class);
        return this;
    }

    public static final Map<String, Module> modules = new ConcurrentHashMap<String, Module>(){{
        put("lucene", new LuceneModule());
        put("jdbc", new SqlModule());
    }};

    public static Application configure(Application application) {
        application.add(new PersistenceModule());
        PersistenceUri persistenceUri = application.applicationScope().get(PersistenceUri.class);
        application.add(modules.get(persistenceUri.scheme()));
        return application;
    }
}
