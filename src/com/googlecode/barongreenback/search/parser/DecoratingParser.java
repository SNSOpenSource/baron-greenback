package com.googlecode.barongreenback.search.parser;

import com.googlecode.funclate.Funclate;
import com.googlecode.funclate.StringFunclate;
import com.googlecode.lazyparsec.error.ParserException;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;

import java.util.Date;
import java.util.Map;

import static com.googlecode.totallylazy.Predicates.instanceOf;

public class DecoratingParser implements PredicateParser {
    private final PredicateParser parser;
    private final Map<String, Object> data;

    public DecoratingParser(PredicateParser parser, Map<String, Object> data) {
        this.parser = parser;
        this.data = data;
    }

    public Predicate<Record> parse(String query, Sequence<? extends Keyword> implicits) {
        try {
            Funclate funclate = new StringFunclate(query);
            funclate.add(instanceOf(Date.class), formatDate());
            String newQuery = funclate.render(data);
            return parser.parse(newQuery, implicits);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private Callable1<Date, String> formatDate() {
        return new Callable1<Date, String>() {
            public String call(Date date) throws Exception {
                return Grammar.DATE_FORMAT.format(date);
            }
        };
    }
}
