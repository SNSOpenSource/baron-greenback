package com.googlecode.barongreenback.search.parser;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.lucene.Lucene;
import com.googlecode.totallylazy.records.lucene.mappings.Mappings;
import com.googlecode.totallylazy.records.sql.expressions.WhereClause;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StandardParserTest {
    @Test
    public void supportsImplicitKeywords() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Keyword<String> name = keyword("name", String.class);
        Predicate<Record> predicate = predicateParser.parse("bob", sequence(name));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "dan")), is(false));

        assertLuceneSyntax(predicate, "name:[bob TO bob]");
        assertSqlSyntax(predicate, "name = 'bob'");
    }

    @Test
    public void supportsExplicitKeywords() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:bob", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "dan")), is(false));

        assertLuceneSyntax(predicate, "name:[bob TO bob]");
        assertSqlSyntax(predicate, "name = 'bob'");
    }

    @Test
    public void supportsMultipleConditions() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:bob age:12", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> age = keyword("age", String.class);
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "13")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(false));

        assertLuceneSyntax(predicate, "+name:[bob TO bob] +age:[12 TO 12]");
        assertSqlSyntax(predicate, "(name = 'bob' and age = '12')");
    }

    @Test
    public void supportsMultipleConditionsSeparatedByManySpaces() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:bob    age:12", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> age = keyword("age", String.class);
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "13")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(false));

        assertLuceneSyntax(predicate, "+name:[bob TO bob] +age:[12 TO 12]");
        assertSqlSyntax(predicate, "(name = 'bob' and age = '12')");
    }

    @Test
    public void supportsNegationWithImplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse(" NOT bob age:12", sequence(keyword("name", String.class)));

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> age = keyword("age", String.class);
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "13")), is(false));

        assertLuceneSyntax(predicate, "-name:[bob TO bob] +age:[12 TO 12]");
        assertSqlSyntax(predicate, "(not name = 'bob' and age = '12')");
    }

    @Test
    public void supportsNegationWithExplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("-name:bob age:12", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> age = keyword("age", String.class);
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "13")), is(false));

        assertLuceneSyntax(predicate, "-name:[bob TO bob] +age:[12 TO 12]");
        assertSqlSyntax(predicate, "(not name = 'bob' and age = '12')");
    }

    @Test
    public void supportsOrWithImplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("dan,bob", sequence(keyword("name", String.class)));

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceneSyntax(predicate, "name:[dan TO dan] name:[bob TO bob]");
        assertSqlSyntax(predicate, "(name = 'dan' or name = 'bob')");

    }

    @Test
    public void supportsAndWithExplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:bodart AND title:baron", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> title = keyword("title", String.class);
        assertThat(predicate.matches(record().set(title, "baron").set(name, "bodart")), is(true));
        assertThat(predicate.matches(record().set(title, "duke").set(name, "bodart")), is(false));
        assertThat(predicate.matches(record().set(title, "baron").set(name, "greenback")), is(false));

        assertLuceneSyntax(predicate, "+name:[bodart TO bodart] +title:[baron TO baron]");
        assertSqlSyntax(predicate, "(name = 'bodart' and title = 'baron')");
    }

    @Test
    public void supportsOrWithExplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:dan OR name:bob", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceneSyntax(predicate, "name:[dan TO dan] name:[bob TO bob]");
        assertSqlSyntax(predicate, "(name = 'dan' or name = 'bob')");
    }

    @Test
    public void ignoreWhitespaces() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("  name  :  dan  ,   bob  ", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceneSyntax(predicate, "name:[dan TO dan] name:[bob TO bob]");
        assertSqlSyntax(predicate, "(name = 'dan' or name = 'bob')");
    }

    @Test
    public void supportsQuotedValue() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:\"Dan Bod\"", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bod")), is(false));

        assertLuceneSyntax(predicate, "name:[Dan Bod TO Dan Bod]");
        assertSqlSyntax(predicate, "name = 'Dan Bod'");
    }

    @Test
    public void supportsStartsWith() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:Dan*", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Bod")), is(false));

        assertLuceneSyntax(predicate, "name:Dan*");
        assertSqlSyntax(predicate, "name like 'Dan%'");
    }

    @Test
    public void supportsEndsWith() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:*Bod", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bod")), is(true));

        assertLuceneSyntax(predicate, "name:*Bod");
        assertSqlSyntax(predicate, "name like '%Bod'");
    }

    @Test
    public void supportsContains() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:*ell*", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Hello")), is(true));
        assertThat(predicate.matches(record().set(name, "Helo")), is(false));
        assertThat(predicate.matches(record().set(name, "ell")), is(true));

        assertLuceneSyntax(predicate, "name:*ell*");
        assertSqlSyntax(predicate, "name like '%ell%'");
    }

    @Test
    public void supportsQuotesContainingNonAlphaNumericCharacters() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("id:\"urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1\"", Sequences.<Keyword>empty());

        Keyword<String> id = keyword("id", String.class);
        assertThat(predicate.matches(record().set(id, "urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1")), is(true));
        assertThat(predicate.matches(record().set(id, "fail")), is(false));

        assertLuceneSyntax(predicate, "id:[urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1 TO urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1]");
        assertSqlSyntax(predicate, "id = 'urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1'");
    }

    @Test
    public void supportsQuotedName() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("\"First Name\":Dan", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("First Name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));
    }

    @Test
    public void supportsEmptyQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("First Name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));
    }

    @Test
    public void supportsExplicitDateBasedQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("dob:2001/1/10", Sequences.<Keyword>empty());

        Keyword<Date> dob = keyword("dob", Date.class);
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 10, 1))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10, 3, 15, 59, 123))), is(true));

        assertLuceneSyntax(predicate, "dob:[20010110000000000 TO 20010110235959000]");
        assertSqlSyntax(predicate, "dob between 'Wed Jan 10 00:00:00 GMT 2001' and 'Wed Jan 10 23:59:59 GMT 2001'");

    }

    @Test
    public void supportsImplicitDateBasedQueries() throws Exception {
        Keyword<Date> dob = keyword("dob", Date.class);
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("2001/1/10", sequence(dob));

        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 10, 1))), is(false));

        String luceneQuery = new Lucene(new Mappings()).query(predicate).toString();
        assertThat(luceneQuery, is("dob:[20010110000000000 TO 20010110235959000]"));
    }

    @Test
    public void supportsGreaterThanQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("dob > 2001/1/10", Sequences.<Keyword>empty());

        Keyword<Date> dob = keyword("dob", Date.class);
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 11))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 9))), is(false));

        String luceneQuery = new Lucene(new Mappings()).query(predicate).toString();
        assertThat(luceneQuery, is("dob:{20010110000000000 TO *]"));
    }

    @Test
    public void supportsLowerThanDateQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("dob < 2001/1/10", Sequences.<Keyword>empty());

        Keyword<Date> dob = keyword("dob", Date.class);
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 9))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 11))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(false));

        String luceneQuery = new Lucene(new Mappings()).query(predicate).toString();
        assertThat(luceneQuery, is("dob:[* TO 20010110000000000}"));
    }

    @Test
    public void supportsLowerThanStringQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name < Dan", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Bob")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));

        String luceneQuery = new Lucene(new Mappings()).query(predicate).toString();
        assertThat(luceneQuery, is("name:[* TO Dan}"));
    }

    @Test
    public void supportsGreaterThanStringQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name > Dan", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bob")), is(false));

        String luceneQuery = new Lucene(new Mappings()).query(predicate).toString();
        assertThat(luceneQuery, is("name:{Dan TO *]"));
    }

    @Test
    public void supportsGreaterThanOrEqualStringQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name >= Dan", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Bob")), is(false));

        String luceneQuery = new Lucene(new Mappings()).query(predicate).toString();
        assertThat(luceneQuery, is("name:[Dan TO *]"));
    }

    @Test
    public void supportsLessThanOrEqualStringQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name <= Dan", Sequences.<Keyword>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Bob")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));

        String luceneQuery = new Lucene(new Mappings()).query(predicate).toString();
        assertThat(luceneQuery, is("name:[* TO Dan]"));
    }

    private void assertSqlSyntax(Predicate<Record> predicate, String expression) {
        assertThat(WhereClause.toSql(predicate).toString(), is(expression));
    }

    private void assertLuceneSyntax(Predicate<Record> predicate, String expression) {
        String luceneQuery = new Lucene(new Mappings()).query(predicate).toString();
        assertThat(luceneQuery, is(expression));
    }

}
