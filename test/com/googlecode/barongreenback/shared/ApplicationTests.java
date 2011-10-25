package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.lucene.DirectoryActivator;
import com.googlecode.totallylazy.Files;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.RedirectHttpHandler;
import com.googlecode.utterlyidle.html.RelativeUrlHandler;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

public abstract class ApplicationTests {
    protected Application application;
    protected HttpClient browser;

    @After
    public void closeApplication() throws IOException {
        application.close();
    }

    @Before
    public void deleteIndex() {
        Files.delete(DirectoryActivator.DEFAULT_DIRECTORY);
        application = new WebApplication();
        browser = new RedirectHttpHandler(new RelativeUrlHandler(application));
    }
}
