package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.html.RedirectFollowingHandler;
import com.googlecode.barongreenback.html.RelativeUrlHandler;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CrawlerResourceTest {
    @Test
    public void canSaveAndLoadACrawler() throws Exception {
        CrawlerPage newPage = new CrawlerPage(new RedirectFollowingHandler(new RelativeUrlHandler(new WebApplication())));
        newPage.update().value("news");
        newPage.from().value("http://feeds.bbci.co.uk/news/rss.xml");
        newPage.recordName().value("/rss/channel/item");
        newPage.keyword(1).value("title");
        newPage.alias(1).value("foo");
        newPage.group(1).value("foo");
        newPage.type(1).value(String.class.getName());
        CrawlerListPage list =  newPage.save();

        CrawlerPage edit = list.edit("news");
        assertThat(edit.update().value(), is("news"));
        assertThat(edit.from().value(), is("http://feeds.bbci.co.uk/news/rss.xml"));
        assertThat(edit.recordName().value(), is("/rss/channel/item"));
        assertThat(edit.keyword(1).value(), is("title"));
        assertThat(edit.alias(1).value(), is("foo"));
        assertThat(edit.group(1).value(), is("foo"));
        assertThat(edit.type(1).value(), is(String.class.getName()));
    }
}
