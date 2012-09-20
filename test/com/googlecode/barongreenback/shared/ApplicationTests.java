package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.html.Browser;
import com.googlecode.yadic.Container;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.Properties;

import static com.googlecode.utterlyidle.html.Browser.browser;

public abstract class ApplicationTests {
    protected Application application;
    protected Browser browser;

    @After
    public void closeApplication() throws IOException {
        application.close();
    }

    @Before
    public void deleteIndex() {
        application = new WebApplication(BasePath.basePath("/"), new Properties(){{
        }});
        application.usingRequestScope(new Callable1<Container, Object>() {
            @Override
            public Object call(Container container) throws Exception {
                container.get(Persistence.class).delete();
                return null;
            }
        });
        browser = browser(application);
    }

}
