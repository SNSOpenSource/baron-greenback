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
    public static final String UPDATE = "//input[@name='form.update']";
    public static final String FROM = "//input[@name='form.from']";
    public static final String MORE = "//input[@name='form.more']";
    public static final String CHECKPOINT_VALUE = "//input[@name='form.checkpoint']";
    public static final String RECORD_NAME = "//input[@id='form.record.name']";
    public static final String KEYWORD_NAME = "//input[@id='form.record.keywords[%s].name']";
    public static final String ALIAS = "//input[@id='form.record.keywords[%s].alias']";
    public static final String GROUP = "//input[@id='form.record.keywords[%s].group']";
    public static final String TYPE = "//select[@id='form.record.keywords[%s].type']";
    public static final String UNIQUE = "//input[@id='form.record.keywords[%s].unique']";
    public static final String VISIBLE = "//input[@id='form.record.keywords[%s].visible']";
    public static final String SUBFEED = "//input[@id='form.record.keywords[%s].subfeed']";
    public static final String CHECKPOINT = "//input[@id='form.record.keywords[%s].checkpoint']";
    private final HttpHandler httpHandler;
    private final Html html;

    public CrawlerPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Crawler"));
    }

    public CrawlerPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get(urlOf(resource(CrawlerResource.class).newForm())).build()));
    }

    public CrawlerListPage save() throws Exception {
        Request request = html.form("//form[contains(@class, 'crawl')]").submit("descendant::input[@type='submit' and @class='save']");
        Response response = httpHandler.handle(request);
        return new CrawlerListPage(httpHandler, response);
    }

    public Input update() {
        return html.input(UPDATE);
    }

    public Input from() {
        return html.input(FROM);
    }

    public Input more() {
        return html.input(MORE);
    }

    public Input checkpoint() {
        return html.input(CHECKPOINT_VALUE);
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

    public Checkbox checkpoint(int index) {
        return html.checkbox(String.format(CHECKPOINT, index));
    }
}
