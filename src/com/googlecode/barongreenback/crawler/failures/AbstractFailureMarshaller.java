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
import com.googlecode.totallylazy.Sequences;

import java.util.List;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.failures.FailureRepository.CRAWLER_ID;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.JOB_TYPE;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.REASON;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.RECORD;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.SOURCE;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.URI;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.lazyrecords.Record.constructors.record;

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
        return HttpDatasource.datasource(
                record.get(URI),
                RecordDefinition.convert(Model.parse(record.get(SOURCE))).definition()
        );
    }

    @Override
    public Record marshal(Failure failure) {
        return record().
                set(JOB_TYPE, FailureMarshallers.forJob(failure.job()).name()).
                set(REASON, failure.reason()).
                set(SOURCE, RecordDefinition.toModel(failure.job().datasource().source()).toString()).
                set(RECORD, toJson(failure.job().record())).
                set(CRAWLER_ID, failure.job().crawlerId()).
                set(URI, failure.job().datasource().uri());
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

    private Record fromJson(String json) {
        Model model = Model.parse(json);
        List<Model> records = model.getValues("record", Model.class);
        return Sequences.sequence(records).fold(record(), toRecord());
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
        return record.fields().fold(model(), toModel()).toString();
    }

    private Function2<Model, Pair<Keyword<?>, Object>, Model> toModel() {
        return new Function2<Model, Pair<Keyword<?>, Object>, Model>() {
            @Override
            public Model call(Model model, Pair<Keyword<?>, Object> field) throws Exception {
                model.add("record", model().
                        add("name", field.first().name()).
                        add("type", field.first().forClass().getName()).
                        add("value", field.second()));
                return model;
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
        return fromJson(record.get(RECORD));
    }
}
