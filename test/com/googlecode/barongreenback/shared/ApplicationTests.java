package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.lucene.LuceneIndexDirectory;
import com.googlecode.totallylazy.Files;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.RedirectHttpHandler;
import com.googlecode.utterlyidle.html.RelativeUrlHandler;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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
        Files.delete(new File(LuceneIndexDirectory.DEFAULT));
        Properties properties = new Properties();
        properties.put(LuceneIndexDirectory.LUCENE_INDEX_DIRECTORY, LuceneIndexDirectory.DEFAULT);
        application = new WebApplication(properties);
        browser = new RedirectHttpHandler(new RelativeUrlHandler(application));
    }

}
