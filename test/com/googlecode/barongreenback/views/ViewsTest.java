package com.googlecode.barongreenback.views;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import org.junit.Test;

import static com.googlecode.barongreenback.views.Views.convertToViewModel;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ViewsTest {
    @Test
    public void canConvertKeywordWithAlias() throws Exception{
        Model model = convertToViewModel(keyword("news"), Sequences.<Keyword>sequence(keyword("some/xpath").as(keyword("NiceName"))));
        Model field = model.<Model>get("view").<Model>get("keywords");
        assertThat(field.<String>get("name"), is("NiceName"));
    }

    @Test
    public void canConvertKeyword() throws Exception{
        Record metadata = record().set(Views.GROUP, "someGroup").
                set(Views.VISIBLE, false).
                set(Keywords.UNIQUE, true);
        Model model = convertToViewModel(keyword("news"), Sequences.<Keyword>sequence(keyword("aField", String.class).metadata(metadata)));

        Model field = model.<Model>get("view").get("keywords");
        assertThat(field.<String>get("name"), is("aField"));
        assertThat(field.<String>get("group"), is("someGroup"));
        assertThat(field.<String>get("type"), is(String.class.getName()));
        assertThat(field.<Boolean>get("unique"), is(true));
        assertThat(field.<Boolean>get("visible"), is(false));
    }
}
