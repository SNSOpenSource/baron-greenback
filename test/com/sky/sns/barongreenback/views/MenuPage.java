package sky.sns.barongreenback.views;

import sky.sns.barongreenback.search.SearchPage;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.html.Link;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class MenuPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public MenuPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Menu"));
    }

    public MenuPage(HttpHandler httpHandler, String drilldowns) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(ViewsResource.class).menu("", "", SearchPage.parseDrillDowns(drilldowns))))).build()));
    }

    public Link link(String value) {
        return html.link("//a[contains(@class, 'tab') and contains(text(), '" + value + "')]");
    }

    public Number numberOfItems() {
        return html.count("//a[contains(@class, 'tab')]");
    }

    public int count(String view) {
        return Integer.parseInt(html.selectContent("//li[contains(@class, '" + view +"')]//span").replaceAll("[\\(\\)]",""));
    }
}