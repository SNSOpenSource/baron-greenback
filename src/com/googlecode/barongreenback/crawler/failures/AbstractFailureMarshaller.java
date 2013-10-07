package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.AbstractCrawler;
import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.HttpDatasource;
import com.googlecode.barongreenback.crawler.VisitedFactory;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.time.Clock;

import java.util.List;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.HttpDatasource.httpDatasource;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.CRAWLER_ID;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.DURATION;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.JOB_TYPE;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.REASON;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.RECORD;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.REQUEST_TIME;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.SOURCE;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.URI;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.funclate.Model.mutable.parse;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.sequence;

abstract public class AbstractFailureMarshaller implements FailureMarshaller {
    private final CrawlerRepository crawlerRepository;
    private final CheckpointHandler checkpointHandler;
    protected final VisitedFactory visited;
    protected final Clock clock;

    public AbstractFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, VisitedFactory visited, Clock clock) {
        this.crawlerRepository = crawlerRepository;
        this.checkpointHandler = checkpointHandler;
        this.visited = visited;
        this.clock = clock;
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
                set(REQUEST_TIME, failure.job().createdDate()).
                set(DURATION, failure.duration()).
                set(SOURCE, RecordDefinition.toModel(failure.job().datasource().source()).toString()).
                set(RECORD, toJson(failure.job().record())).
                set(CRAWLER_ID, failure.job().crawlerId());
    }

    protected Object lastCheckpointFor(Record record) {
        try {
            return checkpointHandler.lastCheckpointFor(crawlerIdFor(record));
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
                Keyword<Object> keyword = keyword(name, type);
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
}