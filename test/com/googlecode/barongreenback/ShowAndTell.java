package com.googlecode.barongreenback;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.barongreenback.persistence.lucene.SearcherPoolActivator;
import com.googlecode.lazyrecords.lucene.LucenePool;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.httpserver.RestServer;

import java.io.File;
import java.util.Properties;

import static com.googlecode.barongreenback.persistence.lucene.LucenePersistence.luceneDirectory;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

public class ShowAndTell {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        PersistenceUri.set(properties, luceneDirectory(new File("/dev/shm/bgb")));
        SearcherPoolActivator.setSearchPool(properties, LucenePool.class);
//        Waitrest waitrest = serverWithDataFeed();
        Application application = new WebApplication(BasePath.basePath("/"), properties);
        new RestServer(
                application,
                defaultConfiguration().port(9000));

//        String definition = Strings.toString(WebApplication.class.getResourceAsStream("BBC.json"));
//        Response response = application.handle(post("crawler/import").form("model", definition).form("action", "Import").build());
//        if (response.status().code() >= 400) {
//            throw new RuntimeException(String.format("Problem importing BBC.json definition \n%s", response));
//        }
    }
}