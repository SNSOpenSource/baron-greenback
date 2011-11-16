package com.googlecode.barongreenback.search.parser;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.time.Dates;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DecoratingParserTest {
    @Test
    public void canInjectCurrentTime() throws Exception{
        PredicateParser parser = new DecoratingParser(new StandardParser(), model().add("now", Dates.date(2001, 2, 3)).toMap());
        Predicate<Record> predicate = parser.parse("Created > $now$", Sequences.<Keyword>empty());
        
        Keyword<Date> created = keyword("Created", Date.class);
        assertThat(predicate.matches(record().set(created, date(2001, 2, 4))), is(true));
        assertThat(predicate.matches(record().set(created, date(2001, 2, 3))), is(false));
        assertThat(predicate.matches(record().set(created, date(2001, 2, 2))), is(false));

    }
}
