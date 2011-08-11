package com.googlecode.barongreenback;

import org.junit.Test;

import static com.googlecode.totallylazy.records.Keywords.keyword;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ViewRendererTest {

    @Test
    public void renderALinkToTheSearchResource() throws Exception {
        String link = new ViewRenderer().call(View.view(keyword("user", String.class)));
        System.out.println(link);
        assertThat(link, is("<a href=\"/search?query=%2Btype%3Auser\">user</a>"));
    }
}
