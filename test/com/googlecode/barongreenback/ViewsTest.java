package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.memory.MemoryRecords;
import org.junit.Test;

import static com.googlecode.barongreenback.View.view;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static org.hamcrest.MatcherAssert.assertThat;

public class ViewsTest {
    @Test
    public void createAndRetrieveAView() throws Exception {
        Records records = new MemoryRecords();
        View view = view(keyword("users")).withFields(keyword("id", Integer.class), keyword("name", String.class));
        Views views = new Views(records).add(view);

        assertThat(records.get(Views.RECORDS_NAME), hasExactly(
                record().set(Views.VIEW_NAME, "users").set(Views.FIELD_NAME, "id").set(Views.FIELD_TYPE, Integer.class.getName()),
                record().set(Views.VIEW_NAME, "users").set(Views.FIELD_NAME, "name").set(Views.FIELD_TYPE, String.class.getName())));

        assertThat(views.get(), hasExactly(view));
    }
}
