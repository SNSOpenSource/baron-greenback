package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.crawler.CrawlerConnectTimeout;
import com.googlecode.barongreenback.crawler.CrawlerHttpClient;
import com.googlecode.barongreenback.crawler.CrawlerImportPage;
import com.googlecode.barongreenback.crawler.CrawlerListPage;
import com.googlecode.barongreenback.crawler.CrawlerReadTimeout;
import com.googlecode.barongreenback.schedules.ScheduleListPage;
import com.googlecode.lazyrecords.lucene.Persistence;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.html.Browser;
import com.googlecode.utterlyidle.jobs.Completer;
import com.googlecode.utterlyidle.jobs.CountDownCompleter;
import com.googlecode.utterlyidle.schedules.CountDownScheduler;
import com.googlecode.utterlyidle.schedules.FixedScheduler;
import com.googlecode.utterlyidle.schedules.HttpScheduler;
import com.googlecode.utterlyidle.schedules.Scheduler;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.waitrest.Waitrest;
import com.googlecode.yadic.Container;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static com.googlecode.barongreenback.crawler.CrawlerTests.serverWithDataFeed;
import static com.googlecode.utterlyidle.handlers.RoutingClient.allTrafficTo;
import static com.googlecode.utterlyidle.html.Browser.browser;

public abstract class ApplicationTests {
    protected Application application;
    protected Browser browser;
    protected Waitrest waitrest;

    @After
    public void closeApplication() throws IOException {
        application.close();
        waitrest.close();
    }

    @Before
    public void deleteIndex() throws Exception {
        waitrest = serverWithDataFeed();
        application = new WebApplication(BasePath.basePath("/"), getProperties());
        application.add(new RequestScopedModule() {
            @Override
            public Container addPerRequestObjects(Container container) throws Exception {
                container.removeOption(CrawlerHttpClient.class);
                BaronGreenbackProperties properties = container.get(BaronGreenbackProperties.class);
                return container.addInstance(CrawlerHttpClient.class, new CrawlerHttpClient(allTrafficTo(new CrawlerHttpClient(new CrawlerConnectTimeout(properties), new CrawlerReadTimeout(properties)), feed().authority())));
            }
        });
        application.usingRequestScope(new Callable1<Container, Object>() {
            @Override
            public Object call(Container container) throws Exception {
                container.get(Persistence.class).deleteAll();
                return null;
            }
        });
        application.start();
        browser = browser(application);
    }

    protected Properties getProperties() {
        return new Properties();
    }

    public CrawlerListPage importCrawler(InputStream stream) throws Exception {
        CrawlerImportPage crawlerImportPage = new CrawlerImportPage(browser);
        return crawlerImportPage.importCrawler(Strings.toString(stream), Option.<UUID>none());
    }

    public ScheduleListPage crawlSampleData(CrawlerListPage listPage, String name) throws Exception {

        final CountDownLatch latch = new CountDownLatch(2);
        application.applicationScope().addInstance(CountDownLatch.class, latch).
                decorate(Scheduler.class, CountDownScheduler.class).
                decorate(Completer.class, CountDownCompleter.class);

        ScheduleListPage jobs = listPage.crawlAndCreateView(name);
        latch.await();
        return jobs;
    }

    protected Uri feed() {
        return Uri.uri(waitrest.getURL().toString()).mergePath("data");
    }

    protected HttpClient feedClient() {
        return allTrafficTo(new ClientHttpHandler(), feed().authority());
    }
}