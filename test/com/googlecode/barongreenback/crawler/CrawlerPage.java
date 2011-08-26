package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.html.Checkbox;
import com.googlecode.barongreenback.html.Html;
import com.googlecode.barongreenback.html.Input;
import com.googlecode.barongreenback.html.Select;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.proxy.Resource.resource;
import static com.googlecode.utterlyidle.proxy.Resource.urlOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CrawlerPage {
    public static final String UPDATE = "//input[@name='update']";
    public static final String FROM = "//input[@name='from']";
    public static final String RECORD_NAME = "//input[@name='recordName']";
    public static final String KEYWORD_NAME = "//input[@id='keywordName%s']";
    public static final String ALIAS = "//input[@id='alias%s']";
    public static final String GROUP = "//input[@id='group%s']";
    public static final String TYPE = "//select[@id='type%s']";
    public static final String UNIQUE = "//input[@id='unique%s']";
    public static final String VISIBLE = "//input[@id='visible%s']";
    public static final String SUBFEED = "//input[@id='subfeed%s']";
    private final HttpHandler httpHandler;
    private final Html html;

    public CrawlerPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Crawler"));
    }

    public CrawlerPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get(urlOf(resource(CrawlerResource.class).get(3))).build()));
    }

    public CrawlerListPage save() throws Exception {
        Request request = html.form("//form[contains(@class, 'crawl')]").submit("//input[@type='submit' and @class='save']");
        Response response = httpHandler.handle(request);
        return new CrawlerListPage(httpHandler, response);
    }

    public Input update() {
        return html.input(UPDATE);
    }

    public Input from() {
        return html.input(FROM);
    }

    public Input recordName() {
        return html.input(RECORD_NAME);
    }

    public Input keyword(int index) {
        return html.input(String.format(KEYWORD_NAME, index));
    }

    public Input alias(int index) {
        return html.input(String.format(ALIAS, index));
    }

    public Input group(int index) {
        return html.input(String.format(GROUP, index));
    }

    public Select type(int index) {
        return html.select(String.format(TYPE, index));
    }

    public Checkbox unique(int index) {
        return html.checkbox(String.format(UNIQUE, index));
    }

    public Checkbox visible(int index) {
        return html.checkbox(String.format(VISIBLE, index));
    }

    public Checkbox subfeed(int index) {
        return html.checkbox(String.format(SUBFEED, index));
    }
}
