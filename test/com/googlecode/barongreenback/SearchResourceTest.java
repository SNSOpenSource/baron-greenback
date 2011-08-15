package com.googlecode.barongreenback;

import com.googlecode.barongreenback.web.WebApplication;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Server;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.utterlyidle.io.Url;
import com.googlecode.yadic.Container;
import org.junit.Test;

import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.utterlyidle.ApplicationBuilder.application;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;
import static com.googlecode.utterlyidle.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SearchResourceTest {
    @Test
    public void canQuery() throws Exception {
        Response response = application(addSomeData(new WebApplication())).handle(get("users/search").withQuery("query", "type:users"));
        System.out.println("response = " + response);
        assertThat(response.status(), is(OK));
    }

    public static Application addSomeData(final WebApplication application) throws Exception {
        Server server = CrawlerTest.startServer();
        Url feed = CrawlerTest.createFeed(server);
        final Sequence<Record> recordSequence = CrawlerTest.crawl(feed).realise();

        application.usingRequestScope(new Callable1<Container, Void>() {
            public Void call(Container container) throws Exception {
                LuceneRecords luceneRecords = container.get(LuceneRecords.class);
                Keyword<Object> users = keyword("users");
                luceneRecords.add(users, recordSequence);
                Views views = container.get(Views.class);
                views.add(View.view(users).withFields(Callables.headers(recordSequence)));
                return VOID;
            }
        });
//        server.close();
        return application;
    }

    public static void main(String[] args) throws Exception {
        new RestServer(addSomeData(new WebApplication()), defaultConfiguration().port(9000));
    }
}
