package com.googlecode.barongreenback;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.PrintStreamLogger;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.waitrest.Waitrest;
import com.googlecode.yadic.Container;
import org.junit.Ignore;

import java.io.PrintStream;
import java.net.URL;

import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

@Ignore
public class WebApplicationTest {
    public static void main(String[] args) throws Exception {
        System.setProperty(BaronGreenbackProperties.PREFIX + ".lucene.index.directory", Files.TEMP_DIR + "/" + WebApplicationTest.class.getSimpleName());
        new Waitrest("/", 8899);
        Application application = new WebApplication(BasePath.basePath("/"), System.getProperties());
        new RestServer(
                application,
                defaultConfiguration().port(9000));

        String definition = Strings.toString(WebApplication.class.getResourceAsStream("BBC.json"));
        Response response = application.handle(post("crawler/import").form("model", definition).form("action", "Import").build());
        if (response.status().code() >= 400) {
            throw new RuntimeException(String.format("Problem importing BBC.json definition \n%s", response));
        }

    }
}
