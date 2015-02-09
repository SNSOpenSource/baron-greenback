package com.googlecode.barongreenback;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.barongreenback.persistence.lucene.NameBasedIndexFacetingPolicy;
import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.lazyrecords.lucene.FieldBasedFacetingPolicy;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.yadic.Container;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

import static com.googlecode.barongreenback.persistence.lucene.LucenePersistence.luceneDirectory;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

public class ShowAndTell {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        PersistenceUri.set(properties, luceneDirectory(new File("/tmp/bg/test-server")));
        Application application = new WebApplication(BasePath.basePath("/"), properties);
        application.add(new ApplicationScopedModule() {
            @Override
            public Container addPerApplicationObjects(Container container) throws Exception {
                final Container bgbApplicationScope = container.get(BaronGreenbackApplicationScope.class).value();
                bgbApplicationScope.remove(NameBasedIndexFacetingPolicy.class);
                bgbApplicationScope.addInstance(NameBasedIndexFacetingPolicy.class, new NameBasedIndexFacetingPolicy(in("news", "flooze")));
                bgbApplicationScope.remove(FieldBasedFacetingPolicy.class);
                bgbApplicationScope.addInstance(FieldBasedFacetingPolicy.class, new FieldBasedFacetingPolicy(in("title", "description", "link", "pubDate")));
                return container;
            }
        });
        new RestServer(
                application,
                defaultConfiguration().port(9000));

        String definition = bbcDefinition();
        Response response = application.handle(post("crawler/import").form("model", definition).form("id", UUID.fromString("77916239-0dfe-4217-9e2a-ceaa9e5bed42")).form("action", "Import").build());
        if (response.status().code() >= 400) {
            throw new RuntimeException(String.format("Problem importing BBC.json definition \n%s", response));
        }
    }

    public static String bbcDefinition() {
        return Strings.toString(WebApplication.class.getResourceAsStream("BBC.json"));
    }
}