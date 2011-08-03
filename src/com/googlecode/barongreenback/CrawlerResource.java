package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.lucene.Lucene;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import org.apache.lucene.queryParser.ParseException;

import java.net.URL;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

@Path("admin")
@Produces(MediaType.TEXT_HTML)
public class CrawlerResource {
    private final Records records;
    private final Crawler crawler;

    public CrawlerResource(Records records, Crawler crawler) {
        this.records = records;
        this.crawler = crawler;
    }

    @POST
    @Path("crawl")
    public Response crawl(@FormParam("url") URL url, @FormParam("recordName") String recordName, @FormParam("elementXPath") String elementXPath, @FormParam("fields") Iterable<String> fields, @FormParam("aliases") Iterable<String> aliases, @FormParam("types") Iterable<String> types) throws Exception {
        XmlSource webSource = new XmlSource(url, keyword(elementXPath), toKeyWords(fields, aliases, types));
        Sequence<Record> extractedValues = crawler.crawl(webSource);
        return add(keyword(recordName), extractedValues);
    }

    private Keyword[] toKeyWords(Iterable<String> fields, Iterable<String> types, Iterable<String> aliases) {
        return sequence(fields).zip(sequence(aliases).zip(sequence(types))).filter(where(first(String.class), not(empty()))).map(asKeyword()).toArray(Keyword.class);
    }

    private Callable1<? super Pair<String, Pair<String, String>>, Keyword> asKeyword() {
        return new Callable1<Pair<String, Pair<String, String>>, Keyword>() {
            public Keyword call(Pair<String, Pair<String, String>> pair) throws Exception {
                Class aClass = Class.forName(pair.second().first());
                ImmutableKeyword source = keyword(pair.first(), aClass);
                String alias = pair.second().second();
                if(!alias.isEmpty()){
                    return source.as(keyword(alias, aClass));
                }
                return source;
            }
        };
    }

    private Response add(final Keyword<Object> recordName, final Sequence<Record> recordsToAdd) throws ParseException {
        records.add(recordName, recordsToAdd);
        return redirect(resource(SearchResource.class).find(String.format("%s:%s", Lucene.RECORD_KEY, recordName)));
    }


}
