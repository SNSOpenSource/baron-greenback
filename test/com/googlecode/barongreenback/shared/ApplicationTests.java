package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.lucene.LuceneIndexConfiguration;
import com.googlecode.totallylazy.Files;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.RedirectHttpHandler;
import com.googlecode.utterlyidle.html.RelativeUrlHandler;
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
        Files.delete(new File(LuceneIndexConfiguration.DEFAULT_DIRECTORY));
        Properties properties = new Properties();
        properties.put(LuceneIndexConfiguration.LUCENE_INDEX_TYPE, LuceneIndexConfiguration.DEFAULT_TYPE.name());
        properties.put(LuceneIndexConfiguration.LUCENE_INDEX_DIRECTORY, LuceneIndexConfiguration.DEFAULT_DIRECTORY);
        application = new WebApplication(BasePath.basePath("/"), properties);
        browser = new RedirectHttpHandler(new RelativeUrlHandler(application));
    }

}
