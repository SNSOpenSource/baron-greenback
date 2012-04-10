package com.googlecode.barongreenback;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.waitrest.Waitrest;
import org.junit.Ignore;

import java.util.Properties;

import static com.googlecode.barongreenback.persistence.lucene.LucenePersistence.luceneTemporaryDirectory;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

@Ignore
public class WebApplicationTest {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        PersistenceUri.set(properties, luceneTemporaryDirectory(WebApplicationTest.class.getSimpleName()));
        new Waitrest("/", 8899);
        Application application = new WebApplication(BasePath.basePath("/"), properties);
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
