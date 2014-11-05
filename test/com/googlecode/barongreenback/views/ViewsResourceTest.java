package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.crawler.CompositeCrawlerTest;
import com.googlecode.barongreenback.crawler.CrawlerTests;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.search.DrillDowns;
import com.googlecode.barongreenback.search.SearchPage;
import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import com.googlecode.waitrest.Waitrest;
import com.googlecode.yadic.Container;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Keyword.methods.keywords;
import static com.googlecode.totallylazy.Arrays.list;
import static com.googlecode.totallylazy.numbers.Numbers.subtract;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

public class ViewsResourceTest extends ApplicationTests {
    private static final UUID VIEW_ID = randomUUID();

    @Before
    public void createView() throws Exception {
        application.usingRequestScope(new Block<Container>() {
            public void execute(Container container) throws Exception {
                ModelRepository repository = container.get(ModelRepository.class);
                repository.set(VIEW_ID, model().
                        add("view", model().
                                add("name", "users").
                                add("records", "users").
                                add("query", "").
                                add("visible", true).
                                add("parent", "someParent").
                                add("keywords", list(model().
                                        add("name", "first").
                                        add("alias", "").
                                        add("group", "").
                                        add("type", "java.lang.String").
                                        add("visible", true).
                                        add("unique", false)))));
                repository.set(UUID.randomUUID(), model().
                        add("view", model().
                                add("name", "news").
                                add("records", "news").
                                add("query", "").
                                add("visible", true).
                                add("keywords", list(model().
                                        add("name", "title").
                                        add("alias", "").
                                        add("group", "").
                                        add("type", "java.lang.String").
                                        add("visible", true).
                                        add("unique", false),
                                        model().
                                                add("name", "field").
                                                add("alias", "fieldalias").
                                                add("group", "").
                                                add("type", "java.lang.String").
                                                add("visible", true).
                                                add("unique", false)))));
                repository.set(UUID.randomUUID(), model().
                        add("view", model().
                                add("name", "hidden view").
                                add("records", "news").
                                add("query", "").
                                add("visible", false).
                                add("keywords", list(model().
                                        add("name", "title").
                                        add("alias", "").
                                        add("group", "").
                                        add("type", "java.lang.String").
                                        add("visible", true).
                                        add("unique", false)))));
            }
        });
        final Waitrest waitrest = CrawlerTests.serverWithDataFeed();
        final Sequence<Record> recordSequence = CompositeCrawlerTest.crawlOnePageOnly(feed(), feedClient()).realise();

        application.usingRequestScope(new Block<Container>() {
            public void execute(Container container) throws Exception {
                Records records = container.get(BaronGreenbackRecords.class).value();
                final Definition usersView = definition("users", keywords(recordSequence).append(keyword("updated", Date.class)));
                records.add(usersView, recordSequence);
            }
        });
        waitrest.close();

    }

    @Test
    public void menuOnlyDisplaysVisibleViews() throws Exception {
        MenuPage menu = new MenuPage(browser,  "{}");
        assertThat(menu.numberOfItems(), NumberMatcher.is(2));
        assertThat(menu.link("users").value(), is("/" + relativeUriOf(method(on(SearchResource.class).list("users", "", Either.<String, DrillDowns>right(DrillDowns.empty()))))));
        assertThat(menu.link("news").value(), is("/" + relativeUriOf(method(on(SearchResource.class).list("news", "", Either.<String, DrillDowns>right(DrillDowns.empty()))))));
    }

    @Test
    public void menuRespectsDrilldowns() throws Exception {
        final String drillDowns = "{\"first\": [ \"Dan\" ]}";
        MenuPage menu = new MenuPage(browser, drillDowns);
        assertThat(menu.numberOfItems(), NumberMatcher.is(2));
        assertThat(menu.link("users").value(), is("/" + relativeUriOf(method(on(SearchResource.class).list("users", "", SearchPage.parseDrillDowns(drillDowns))))));
        assertThat(menu.link("news").value(), is("/" + relativeUriOf(method(on(SearchResource.class).list("news", "", SearchPage.parseDrillDowns(drillDowns))))));
        assertThat(menu.count("users"), is(1));
        assertThat(menu.count("news"), is(0));
    }

    @Test
    public void menuShouldReturnCount0IfDrillDownNotPresentInView() throws Exception {
        final String drillDowns = "{\"title\": [ \"Test\" ]}";
        MenuPage menu = new MenuPage(browser, drillDowns);
        assertThat(menu.numberOfItems(), NumberMatcher.is(2));
        assertThat(menu.link("users").value(), is("/" + relativeUriOf(method(on(SearchResource.class).list("users", "", SearchPage.parseDrillDowns(drillDowns))))));
        assertThat(menu.link("news").value(), is("/" + relativeUriOf(method(on(SearchResource.class).list("news", "", SearchPage.parseDrillDowns(drillDowns))))));
        assertThat(menu.count("users"), is(0));
        assertThat(menu.count("news"), is(0));
    }

    @Test
    public void canEdit() throws Exception {
        ViewListPage views = new ViewListPage(browser);
        assertThat(relativeUriOf(method(on(ViewsResource.class).edit(VIEW_ID))).toString(), endsWith(views.link("users").value()));
        ViewEditPage edit = views.edit(VIEW_ID);
        assertThat(edit.name().value(), is("users"));
        assertThat(edit.records().value(), is("users"));
        assertThat(edit.query().value(), is(""));
        assertThat(edit.parent().value(), is("someParent"));
        assertThat(edit.fieldName(1).value(), is("firstname"));
        edit.name().value("people");
        edit.records().value("people");
        edit.query().value("firstname:dan");
        edit.parent().value("parent");

        ViewListPage modifiedViews = edit.save();

        assertThat(relativeUriOf(method(on(ViewsResource.class).edit(VIEW_ID))).toString(), endsWith(modifiedViews.link("people").value()));
        ViewEditPage modifiedView = views.edit(VIEW_ID);
        assertThat(modifiedView.records().value(), is("people"));
        assertThat(modifiedView.query().value(), is("firstname:dan"));
        assertThat(modifiedView.parent().value(), is("parent"));
        assertThat(modifiedView.fieldName(1).value(), is("firstname"));
    }

    @Test
    public void deleteView() throws Exception {
        ViewListPage views = new ViewListPage(browser);
        Number numberOfViews = views.count();
        ViewListPage newPage = views.delete(VIEW_ID);
        assertThat(newPage.count(), is(subtract(numberOfViews, 1)));
    }
}
