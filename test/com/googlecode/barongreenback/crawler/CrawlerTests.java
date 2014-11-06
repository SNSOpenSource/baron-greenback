package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.waitrest.Waitrest;

import java.net.URL;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_XML;
import static com.googlecode.utterlyidle.RequestBuilder.put;

public abstract class CrawlerTests extends ApplicationTests {
    public static Waitrest serverWithDataFeed() throws Exception {
        Waitrest waitrest = new Waitrest();
        ClientHttpHandler restClient = new ClientHttpHandler();
        final URL waitrestURL = waitrest.getURL();
        restClient.handle(put(uri(waitrestURL, "data")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("atom.xml")).build());
        restClient.handle(put(uri(waitrestURL, "data/prev")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("atom-prev.xml")).build());
        restClient.handle(put(uri(waitrestURL, "entry1.xml")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("entry1.xml")).build());
        restClient.handle(put(uri(waitrestURL, "entry1pony.xml")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("entry1pony.xml")).build());
        restClient.handle(put(uri(waitrestURL, "entry2.xml")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("entry2.xml")).build());
        restClient.handle(put(uri(waitrestURL, "entry3.xml")).header(CONTENT_TYPE, TEXT_XML).entity(contentOf("entry3.xml")).build());
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