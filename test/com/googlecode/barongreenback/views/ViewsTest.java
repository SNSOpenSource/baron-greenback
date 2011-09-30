package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.lucene.DirectoryActivator;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.yadic.Container;
import org.junit.Before;
import org.junit.Test;

import static com.googlecode.barongreenback.views.View.view;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ViewsTest {
    @Before
    public void deleteIndex() {
        Files.delete(DirectoryActivator.DEFAULT_DIRECTORY);
    }

    @Test
    public void createAndRetrieveAView() throws Exception {
        using(new WebApplication(), new Callable1<WebApplication, Void>() {
            public Void call(WebApplication application) throws Exception {
                application.usingRequestScope(new Callable1<Container, Void>() {
                    public Void call(Container container) throws Exception {
                        Records records = container.get(Records.class);
                        Keyword<Integer> id = keyword("id", Integer.class).metadata(record().set(Keywords.UNIQUE, true).set(Views.VISIBLE, true));
                        View view = view(keyword("users")).withFields(id, keyword("name", String.class));
                        Views views = new Views(records).put(view);

                        Sequence<Record> actual = records.get(Views.RECORDS_NAME).realise();
                        assertThat(actual, hasExactly(
                                record().set(Views.VIEW_NAME, "users").set(Views.FIELD_ORDER, 0).set(Views.FIELD_NAME, "id").set(Views.FIELD_TYPE, Integer.class.getName()).set(Keywords.UNIQUE, true).set(Views.VISIBLE, true),
                                record().set(Views.VIEW_NAME, "users").set(Views.FIELD_ORDER, 1).set(Views.FIELD_NAME, "name").set(Views.FIELD_TYPE, String.class.getName())));

                        View result = views.get().head();
                        assertThat(result, is(view));
                        Keyword idResult = result.fields().find(Predicates.<Keyword>is(id)).get();
                        assertThat(idResult.metadata().get(Keywords.UNIQUE), is(true));

                        return Runnables.VOID;
                    }
                });
                return VOID;
            }
        });
    }
}
