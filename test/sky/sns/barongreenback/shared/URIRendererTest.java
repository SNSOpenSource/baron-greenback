package com.googlecode.barongreenback.shared;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class URIRendererTest {
    @Test
    public void escapesQueryParamaters() throws Exception {
        String html = new URIRenderer().render(URI.create("http://server/path?a=1&b=2"));
        assertThat(html, is("<a href=\"http://server/path?a=1&amp;b=2\">http://server/path?a=1&amp;b=2</a>"));
    }
}
