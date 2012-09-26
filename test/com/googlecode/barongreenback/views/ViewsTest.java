package com.googlecode.barongreenback.views;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ViewsTest {
    @Test
    public void canConvertKeywordWithAlias() throws Exception{
        Model model = model().add(ViewsRepository.ROOT, model().
                add("name", "news").
                add("records", "news").
                add("query", "").
                add("visible", true).
                add("priority", "").
                add("keywords", Sequences.sequence(keyword("some/xpath").as(keyword("NiceName"))).map(ViewsRepository.asModel()).toList()));
        Model field = model.<Model>get("view").<Model>get("keywords");
        assertThat(field.<String>get("name"), is("NiceName"));
    }

    @Test
    public void canConvertKeyword() throws Exception{
        Record metadata = record().
                set(ViewsRepository.GROUP, "someGroup").
                set(ViewsRepository.VISIBLE, false).
                set(Keywords.UNIQUE, true);
        Model model = model().add(ViewsRepository.ROOT, model().
                add("name", "news").
                add("records", "news").
                add("query", "").
                add("visible", true).
                add("priority", "").
                add("keywords", Sequences.sequence(keyword("aField", String.class).metadata(metadata)).map(ViewsRepository.asModel()).toList()));

        Model field = model.<Model>get("view").get("keywords");
        assertThat(field.<String>get("name"), is("aField"));
        assertThat(field.<String>get("group"), is("someGroup"));
        assertThat(field.<String>get("type"), is(String.class.getName()));
        assertThat(field.<Boolean>get("unique"), is(true));
        assertThat(field.<Boolean>get("visible"), is(false));
    }
}
