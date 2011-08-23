package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.totallylazy.records.xml.XmlRecords;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.yadic.Container;
import org.junit.Test;

import static com.googlecode.barongreenback.views.View.view;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.utterlyidle.ApplicationBuilder.application;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ViewsResourceTest {
    @Test
    public void displaysAListOfViews() throws Exception {
        WebApplication application = new WebApplication();
        application.usingRequestScope(new Callable1<Container, Void>() {
            public Void call(Container container) throws Exception {
                View view = view(keyword("users")).withFields(keyword("name", String.class));
                container.get(Views.class).put(view);
                return Runnables.VOID;
            }
        });

        Response response = application(application).handle(get("views/menu"));
        assertThat(response.status(), is(Status.OK));

        XmlRecords xmlRecords = new XmlRecords(Xml.document(new String(response.bytes())));
        Keyword results = keyword("//ul[@class='views']/li");
        Keyword<String> link = keyword("a/@href", String.class);
        xmlRecords.define(results, link);
        assertThat(xmlRecords.get(results).map(link), hasExactly("/users/search/list?query="));

    }
}
