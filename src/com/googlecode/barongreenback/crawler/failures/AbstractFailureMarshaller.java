package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.AbstractCrawler;
import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.HttpDatasource;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.HttpDatasource.httpDatasource;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.CRAWLER_ID;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.JOB_TYPE;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.REASON;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.RECORD;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.SOURCE;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.URI;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.VISITED;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.funclate.Model.mutable.parse;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Uri.uri;

abstract public class AbstractFailureMarshaller implements FailureMarshaller {
    private final CrawlerRepository crawlerRepository;
    private final CheckpointHandler checkpointHandler;

    public AbstractFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler) {
        this.crawlerRepository = crawlerRepository;
        this.checkpointHandler = checkpointHandler;
    }

    public Definition destination(Record record) {
        return AbstractCrawler.destinationDefinition(crawlerIdFor(record));
    }

    public HttpDatasource datasource(Record record) {
        return httpDatasource(record.get(URI), convert(parse(record.get(SOURCE))).definition());
    }

    @Override
    public Record marshal(Failure failure) {
        return record().
                set(JOB_TYPE, FailureMarshallers.forJob(failure.job()).name()).
                set(REASON, failure.reason()).
                set(URI, failure.job().datasource().uri()).
                set(SOURCE, RecordDefinition.toModel(failure.job().datasource().source()).toString()).
                set(RECORD, toJson(failure.job().record())).
                set(CRAWLER_ID, failure.job().crawlerId()).
                set(VISITED, toJson(failure.job().visited()));
    }

    private String toJson(Set<HttpDatasource> visited) {
        return sequence(visited).fold(model(), toModel()).toString();
    }

    private Function2<Model, HttpDatasource, Model> toModel() {
        return new Function2<Model, HttpDatasource, Model>() {
            @Override
            public Model call(Model model, HttpDatasource httpDatasource) throws Exception {
                return model.add("visited", model().add("source", RecordDefinition.toModel(httpDatasource.source())).add("uri", httpDatasource.uri()));
            }
        };
    }

    private Set<HttpDatasource> toVisited(String json) {
        return sequence(parse(json).getValues("visited", Model.class)).fold(new HashSet<HttpDatasource>(), addDatasource());
    }

    private Function2<Set<HttpDatasource>, Model, Set<HttpDatasource>> addDatasource() {
        return new Function2<Set<HttpDatasource>, Model, Set<HttpDatasource>>() {
            @Override
            public Set<HttpDatasource> call(Set<HttpDatasource> visiteds, Model model) throws Exception {
                visiteds.add(toDatasource(model));
                return visiteds;
            }
        };
    }

    private HttpDatasource toDatasource(Model model) {
        return httpDatasource(uri(model.get("uri", String.class)), convert(model.get("source", Model.class)).definition());
    }

    protected Object lastCheckpointFor(Record record) {
        try {
            return checkpointHandler.lastCheckPointFor(crawlerIdFor(record));
        } catch (Exception e) {
            throw LazyException.lazyException(e);
        }
    }

    protected String moreUri(Record record) {
        return AbstractCrawler.more(crawlerIdFor(record));
    }

    private Record toRecord(String json) {
        Model model = parse(json);
        List<Model> records = model.getValues("record", Model.class);
        return sequence(records).fold(record(), toRecord());
    }

    private Function2<Record, Model, Record> toRecord() {
        return new Function2<Record, Model, Record>() {
            @Override
            public Record call(Record record, Model model) throws Exception {
                String name = model.get("name", String.class);
                Class<?> type = Class.forName(model.get("type", String.class));
                Keyword<Object> keyword = Keywords.keyword(name, type);
                return record.set(keyword, model.get("value"));
            }
        };
    }

    protected String toJson(Record record) {
        return record.fields().fold(model(), recordToModel()).toString();
    }

    private Function2<Model, Pair<Keyword<?>, Object>, Model> recordToModel() {
        return new Function2<Model, Pair<Keyword<?>, Object>, Model>() {
            @Override
            public Model call(Model model, Pair<Keyword<?>, Object> field) throws Exception {
                return model.add("record", model().
                        add("name", field.first().name()).
                        add("type", field.first().forClass().getName()).
                        add("value", field.second()));
            }
        };
    }

    private Model crawlerIdFor(Record record) {
        UUID crawlerId = record.get(CRAWLER_ID);
        return crawlerRepository.crawlerFor(crawlerId);
    }

    protected UUID crawlerId(Record record) {
        return record.get(CRAWLER_ID);
    }

    protected Record crawledRecord(Record record) {
        return toRecord(record.get(RECORD));
    }

    protected Set<HttpDatasource> visited(Record record) {
        return toVisited(record.get(VISITED));
    }
}
