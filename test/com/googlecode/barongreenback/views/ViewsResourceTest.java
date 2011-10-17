package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.ApplicationTest;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.yadic.Container;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Arrays.list;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

public class ViewsResourceTest extends ApplicationTest {
    private static final UUID VIEW_ID = randomUUID();

    @Before
    public void createView() {
        application.usingRequestScope(new Callable1<Container, Void>() {
            public Void call(Container container) throws Exception {
                container.get(ModelRepository.class).set(VIEW_ID, model().
                        add("view", model().
                                add("name", "users").
                                add("keywords", list(model().
                                        add("name", "firstname").
                                        add("alias", "").
                                        add("group", "").
                                        add("type", "java.lang.String").
                                        add("visible", true).
                                        add("unique", false)))));
                return Runnables.VOID;
            }
        });
    }

    @Test
    public void displayMenuTabs() throws Exception {
        MenuPage menu = new MenuPage(browser);
        assertThat(menu.link("users").value(), is("/" + relativeUriOf(method(on(SearchResource.class).list("users", "")))));
    }

    @Test
    public void canEdit() throws Exception {
        ViewListPage views = new ViewListPage(browser);
        assertThat(relativeUriOf(method(on(ViewsResource.class).edit(VIEW_ID))).toString(), endsWith(views.link("users").value()));
        ViewEditPage edit = views.edit(VIEW_ID);
        assertThat(edit.fieldName(1).value(), is("firstname"));
        edit.name().value("people");
        ViewListPage modifiedViews = edit.save();
        assertThat(relativeUriOf(method(on(ViewsResource.class).edit(VIEW_ID))).toString(), endsWith(modifiedViews.link("people").value()));
    }

    @Test
    public void deleteView() throws Exception {
        ViewListPage views = new ViewListPage(browser);
        int numberOfViews = views.count();
        ViewListPage newPage = views.delete(VIEW_ID);
        assertThat(newPage.count(), is(numberOfViews - 1));
    }
}
