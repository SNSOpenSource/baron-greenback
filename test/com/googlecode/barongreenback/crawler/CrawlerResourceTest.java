package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.html.RedirectFollowingHandler;
import com.googlecode.barongreenback.html.RelativeUrlHandler;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CrawlerResourceTest {
    @Test
    public void displayCrawlingForm() throws Exception {
        CrawlerPage page = new CrawlerPage(new RedirectFollowingHandler(new RelativeUrlHandler(new WebApplication())));
        CrawlerListPage list = page.update("news").
                from("http://feeds.bbci.co.uk/news/rss.xml").
                save();

        assertThat(list.contains("news"), is(true));
    }
}
