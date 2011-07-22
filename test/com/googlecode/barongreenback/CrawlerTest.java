package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Binding;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.RestApplication;
import com.googlecode.utterlyidle.ServerActivator;
import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.io.Url;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.googlecode.barongreenback.CrawlerTest.ApplicationBuilder.application;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;
import static com.googlecode.utterlyidle.dsl.DslBindings.bindings;
import static com.googlecode.utterlyidle.dsl.StaticBindingBuilder.in;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CrawlerTest {
    private static final Keyword<Object> ENTRIES = Keyword.keyword("/feed/entry");
    private static final Keyword<String> LINK = Keyword.keyword("link/@href", String.class);
    private static final Keyword<Date> UPDATED = Keyword.keyword("updated", Date.class);

    private static final Keyword<Object> USER = Keyword.keyword("/user");
    private static final Keyword<Integer> USER_ID = Keyword.keyword("summary/userId", Integer.class);
    private static final Keyword<String> FIRST_NAME = Keyword.keyword("summary/firstName", String.class);

    @Test
    public void shouldGetTheContentsOfAUrlAndExtractContent() throws Exception {
        Url base = startServer();
        final Url feed = base.replacePath(base.path().subDirectory("static").file("atom.xml"));
        XmlSource xmlSource = new XmlSource(feed.toURL(), ENTRIES, LINK);
        final Crawler crawler = new Crawler();
        Sequence<Record> records = crawler.crawl(xmlSource).sortBy(descending(UPDATED)).flatMap(crawler.crawl(LINK, USER, USER_ID, FIRST_NAME));
        Record entry = records.head();

        assertThat(entry.get(USER_ID), is(1234));
        assertThat(entry.get(FIRST_NAME), is("Dan"));
    }


    private Url startServer() {
        return application().content(packageUrl(CrawlerTest.class), "static").start(defaultConfiguration().port(10010));
    }

    public static class ApplicationBuilder {
        private final List<Module> modules = new ArrayList<Module>();

        public static ApplicationBuilder application() {
            return new ApplicationBuilder();
        }

        public ApplicationBuilder content(final URL baseUrl, final String path) {
            modules.add(new BindingsModule(bindings(in(baseUrl).path(path))));
            return this;
        }

        public Application build() {
            return new RestApplication(Sequences.sequence(modules).toArray(Module.class));
        }

        public Url start(ServerConfiguration configuration) {
            return Callers.call(new ServerActivator(build(), configuration)).getUrl();
        }

        public Url start() {
            return start(defaultConfiguration());
        }

        public static class BindingsModule implements ResourcesModule {
            private final Binding[] bindings;

            public BindingsModule(Binding[] bindings) {
                this.bindings = bindings;
            }

            public Module addResources(Resources resources) {
                resources.add(bindings);
                return this;
            }
        }
    }
}
