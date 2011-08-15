package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.memory.MemoryRecords;
import org.junit.Test;

import static com.googlecode.barongreenback.View.view;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ViewsTest {
    @Test
    public void createAndRetrieveAView() throws Exception {
        Records records = new MemoryRecords();
        Keyword<Integer> id = keyword("id", Integer.class).metadata(record().set(Keywords.UNIQUE, true));
        View view = view(keyword("users")).withFields(id, keyword("name", String.class));
        Views views = new Views(records).add(view);

        assertThat(records.get(Views.RECORDS_NAME), hasExactly(
                record().set(Views.VIEW_NAME, "users").set(Views.FIELD_NAME, "id").set(Views.FIELD_TYPE, Integer.class.getName()).set(Views.UNIQUE, "true"),
                record().set(Views.VIEW_NAME, "users").set(Views.FIELD_NAME, "name").set(Views.FIELD_TYPE, String.class.getName()).set(Views.UNIQUE, "false")));

        View result = views.get().head();
        assertThat(result, is(view));
        Keyword idResult = result.getFields().find(Predicates.<Keyword>is(id)).get();
        assertThat(idResult.metadata().get(Keywords.UNIQUE), is(true));

    }
}
