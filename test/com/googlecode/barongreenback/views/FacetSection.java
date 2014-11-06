package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.search.DrillDowns;
import com.googlecode.barongreenback.search.DrillDownsActivator;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.FIRST;
import static com.googlecode.totallylazy.Either.left;
import static com.googlecode.totallylazy.Either.right;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.util.Arrays.asList;

public class FacetSection {
    public static final String SHOW_MORE = "facet-show-more";
    public static final String SHOW_FEWER = "facet-show-fewer";

    private HttpHandler httpHandler;
    private Html html;

    public FacetSection(HttpHandler httpHandler, Html html) {
        this.httpHandler = httpHandler;
        this.html = html;
    }

    public FacetSection(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
    }

    public static FacetSection singleFacet(HttpHandler httpHandler, String view, String query, String drillDowns) throws Exception {
        return singleFacet(httpHandler, view, FIRST.name(), query, drillDowns);
    }

    public static FacetSection singleFacet(HttpHandler httpHandler, String view, String facetName, String query, String drillDowns) throws Exception {
        return new FacetSection(httpHandler, httpHandler.handle(singleFacetUrl(view, query, facetName, Option.none(Integer.class), drillDowns).build()));
    }

    public static FacetSection singleFacet(HttpHandler httpHandler, String view, String query, Integer entryCount, String drillDowns) throws Exception {
        return new FacetSection(httpHandler, httpHandler.handle(singleFacetUrl(view, query, FIRST.name(), Option.some(entryCount), drillDowns).build()));
    }

    public static FacetSection facets(HttpHandler httpHandler, String view, String query, String drillDowns) throws Exception {
        return new FacetSection(httpHandler, httpHandler.handle(listFacetsUrl(view, query, drillDowns).build()));
    }

    public Html getHtml() {
        return html;
    }

    private static RequestBuilder singleFacetUrl(String view, String query, String facetName, Option<Integer> entryCount, String drillDownsDocument) throws Exception {
        Either<String, DrillDowns> drillDowns = parseDrillDowns(drillDownsDocument);
        return get("/" + relativeUriOf(method(on(FacetsResource.class).facet(view, query, facetName, entryCount, drillDowns))));
    }

    private static RequestBuilder listFacetsUrl(String view, String query, String drillDownsDocument) throws Exception {
        Either<String, DrillDowns> drillDowns = parseDrillDowns(drillDownsDocument);
        return get("/" + relativeUriOf(method(on(FacetsResource.class).list(view, query, drillDowns))));
    }


    private static Either<String, DrillDowns> parseDrillDowns(String drillDownsDocument) {
        Either<String, DrillDowns> drillDowns;
        try {
            drillDowns = right(new DrillDownsActivator(drillDownsDocument).call());
        } catch (Exception e) {
            drillDowns = left(drillDownsDocument);
        }
        return drillDowns;
    }

    public boolean hasLink(String link) {
        return html.count(String.format("//li[@class='%s']/a/@href", link)).intValue() > 0;
    }

    public Response clicking(String link) throws Exception {
        final String url = link(link);
        return httpHandler.handle(get(url).build());
    }

    public int selectedFacetEntriesCount() {
        return html.count("//input[contains(@class, 'facet-entry-checkbox') and @checked='checked']").intValue();

    }

    public String drillDownsException() {
        return html.selectContent("//meta[@name='drillDownsException']/@content");
    }

    public String errorMessage() {
        return html.selectContent("//div[contains(@class, 'error')]/text()");
    }

    public int facetEntryCount() {
        return html.count("//ul[contains(@class, 'facet')]/li[contains(@class, 'facet-entry')]").intValue();
    }

    public String link(String link) {
        return html.selectContent(String.format("//li[@class='%s']/a/@href", link));
    }

    public Collection<Entry> entries() {
        return html.selectValues("//span[contains(@class, 'facet-entry-name')]").map(entry());
    }

    public Collection<Entry> selectedEntries() {
        return html.selectValues("//ul[contains(@class, 'facet')]/li[contains(@class, 'facet-entry')]/label/input[contains(@checked, 'checked')]/@value").map(entry());
    }

    public static Entry facetEntry(String name) {
        return new Entry(name);
    }

    public static Facet facet(String name) {
        return new Facet(name);
    }

    public Collection<Facet> displayedFacets() throws Exception {
        final Sequence<String> facetNames = html.selectValues("//h5[contains(@class, 'facet-name')]");
        return facetNames.map(new Callable1<String, Facet>() {
            @Override
            public Facet call(String facetName) throws Exception {
                final Sequence<String> entries = html.selectValues(String.format("//h5[text()='%s']/following-sibling::ul//span[contains(@class, 'facet-entry-name')]", facetName));
                return new Facet(facetName, entries.toList());
            }
        });
    }

    private Callable1<String, Entry> entry() {

        return new Callable1<String, Entry>() {
            @Override
            public Entry call(String value) throws Exception {
                return new Entry(value);
            }
        };
    }

    public static class Entry {
        private final String name;

        Entry(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            return name.equals(entry.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static class Facet {
        private final String name;
        private final List<String> entries;

        public Facet(String name) {
            this.name = name;
            this.entries = new ArrayList<String>();
        }

        public Facet(String name, List<String> entries) {
            this.name = name;
            this.entries = entries;
        }

        public Facet withEntries(String... entries) {
            return new Facet(name, asList(entries));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Facet facet = (Facet) o;

            return entries.equals(facet.entries) && name.equals(facet.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + entries.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Facet{" +
                    "name='" + name + '\'' +
                    ", entries=" + entries +
                    '}';
        }
    }
}
