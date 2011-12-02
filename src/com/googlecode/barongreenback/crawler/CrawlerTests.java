package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.waitrest.Restaurant;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_XML;
import static com.googlecode.utterlyidle.RequestBuilder.put;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

public abstract class CrawlerTests {
    protected RestServer restServer;
    public static Uri atomXml = Uri.uri("http://localhost:9001/data");

    @Before
    public void startWaitrest() throws Exception {
        restServer = setupServerWithDataFeed();
    }

    @After
    public void stopWaitrest() throws IOException {
        restServer.close();
    }

    public static RestServer setupServerWithDataFeed() throws Exception {
        RestServer dataSourceServer = new RestServer(new Restaurant(BasePath.basePath("/")), defaultConfiguration().port(9001));
        ClientHttpHandler restClient = new ClientHttpHandler();
        restClient.handle(put(dataSourceServer.uri().mergePath("data")).header(CONTENT_TYPE, TEXT_XML).input(contentOf("atom.xml").getBytes()).build());
        restClient.handle(put(dataSourceServer.uri().mergePath("data/prev")).header(CONTENT_TYPE, TEXT_XML).input(contentOf("atom-prev.xml").getBytes()).build());
        restClient.handle(put(dataSourceServer.uri().mergePath("entry1.xml")).header(CONTENT_TYPE, TEXT_XML).input(contentOf("entry1.xml").getBytes()).build());
        restClient.handle(put(dataSourceServer.uri().mergePath("entry2.xml")).header(CONTENT_TYPE, TEXT_XML).input(contentOf("entry2.xml").getBytes()).build());
        return dataSourceServer;
    }

    public static String contentOf(String name) {
        return Strings.toString(CrawlerTests.class.getResourceAsStream(name));
    }





}
