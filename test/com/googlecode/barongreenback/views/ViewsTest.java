package com.googlecode.barongreenback.views;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.RecordName;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import org.junit.Test;

import static com.googlecode.barongreenback.views.Views.convertToViewModel;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.MapRecord.record;
import static com.googlecode.lazyrecords.RecordName.recordName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ViewsTest {
    @Test
    public void canConvertKeywordWithAlias() throws Exception{
        Model model = convertToViewModel(recordName("news"), Sequences.sequence(keyword("some/xpath").as(keyword("NiceName"))));
        Model field = model.<Model>get("view").<Model>get("keywords");
        assertThat(field.<String>get("name"), is("NiceName"));
    }

    @Test
    public void canConvertKeyword() throws Exception{
        Record metadata = record().set(Views.GROUP, "someGroup").
                set(Views.VISIBLE, false).
                set(Keywords.UNIQUE, true);
        Model model = convertToViewModel(recordName("news"), Sequences.sequence(keyword("aField", String.class).metadata(metadata)));

        Model field = model.<Model>get("view").get("keywords");
        assertThat(field.<String>get("name"), is("aField"));
        assertThat(field.<String>get("group"), is("someGroup"));
        assertThat(field.<String>get("type"), is(String.class.getName()));
        assertThat(field.<Boolean>get("unique"), is(true));
        assertThat(field.<Boolean>get("visible"), is(false));
    }
}
