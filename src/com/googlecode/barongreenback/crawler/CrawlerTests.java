package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.waitrest.Waitrest;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.URL;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_XML;
import static com.googlecode.utterlyidle.RequestBuilder.put;

public abstract class CrawlerTests {
    protected Waitrest waitrest;
    public static Uri atomXml = Uri.uri("http://localhost:9001/data");

    @Before
    public void startWaitrest() throws Exception {
        waitrest = setupServerWithDataFeed();
    }

    @After
    public void stopWaitrest() throws IOException {
        waitrest.close();
    }

    public static Waitrest setupServerWithDataFeed() throws Exception {
        Waitrest waitrest = new Waitrest("/", 9001);
        ClientHttpHandler restClient = new ClientHttpHandler();
        final URL waitrestURL = waitrest.getURL();
        restClient.handle(put(uri(waitrestURL, "data")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("atom.xml")).build());
        restClient.handle(put(uri(waitrestURL, "data/prev")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("atom-prev.xml")).build());
        restClient.handle(put(uri(waitrestURL, "entry1.xml")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("entry1.xml")).build());
        restClient.handle(put(uri(waitrestURL, "entry2.xml")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("entry2.xml")).build());
        restClient.handle(put(uri(waitrestURL, "invalid.xml")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("invalid.xml")).build());
        return waitrest;
    }

    private static Uri uri(URL baseUrl, String path) {
       return Uri.uri(String.format("%s%s", baseUrl.toString(), path));
    }

    public static String contentOf(String name) {
        return Strings.toString(CrawlerTests.class.getResourceAsStream(name));
    }
}