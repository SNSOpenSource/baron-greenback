package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.lucene.LuceneIndexConfiguration;
import com.googlecode.barongreenback.persistence.lucene.LuceneIndexType;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Files;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.RedirectHttpHandler;
import com.googlecode.utterlyidle.html.RelativeUrlHandler;
import com.googlecode.utterlyidle.modules.StartupModule;
import com.googlecode.yadic.Container;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public abstract class ApplicationTests {
    protected Application application;
    protected HttpClient browser;

    @After
    public void closeApplication() throws IOException {
        application.close();
    }

    @Before
    public void deleteIndex() {
        Properties properties = new Properties();
        application = new WebApplication(BasePath.basePath("/"), properties);
        application.usingRequestScope(new Callable1<Container, Object>() {
            @Override
            public Object call(Container container) throws Exception {
                container.get(Persistence.class).deleteAll();
                return null;
            }
        });
        browser = new RedirectHttpHandler(new RelativeUrlHandler(application));
    }

}
