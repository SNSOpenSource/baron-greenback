package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.AbstractCrawler;
import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.HttpDatasource;
import com.googlecode.barongreenback.crawler.HttpJob;
import com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequences;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.sequence;

abstract public class AbstractFailureMarshaller implements FailureMarshaller {
    private final CrawlerRepository crawlerRepository;
    private final CheckpointHandler checkpointHandler;

    private static final Map<Class, String> typeMap = Maps.map(sequence(
            Pair.<Class, String>pair(MasterPaginatedHttpJob.class, "master"),
            Pair.<Class, String>pair(PaginatedHttpJob.class, "paginated"),
            Pair.<Class, String>pair(HttpJob.class, "http")));

    public AbstractFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler) {
        this.crawlerRepository = crawlerRepository;
        this.checkpointHandler = checkpointHandler;
    }

    public String nameForClass(Class aClass) {
        return typeMap.get(aClass);
    }

    public Definition destination(Record record) {
        UUID crawlerId = record.get(CrawlerFailureRepository.CRAWLER_ID);
        Model crawler = crawlerRepository.crawlerFor(crawlerId);
        return AbstractCrawler.destinationDefinition(crawler);
    }

    public HttpDatasource datasource(Record record) {
        UUID crawlerId = record.get(CrawlerFailureRepository.CRAWLER_ID);
        return HttpDatasource.datasource(
                record.get(CrawlerFailureRepository.URI),
                crawlerId,
                RecordDefinition.convert(Model.parse(record.get(CrawlerFailureRepository.SOURCE))).definition(),
                fromJson(record.get(CrawlerFailureRepository.RECORD)));
    }

    protected Object lastCheckpointFor(Record record) {
        UUID crawlerId = record.get(CrawlerFailureRepository.CRAWLER_ID);
        Model crawler = crawlerRepository.crawlerFor(crawlerId);
        try {
            return checkpointHandler.lastCheckPointFor(crawler);
        } catch (Exception e) {
            throw LazyException.lazyException(e);
        }
    }

    protected String moreUri(Record record) {
        UUID crawlerId = record.get(CrawlerFailureRepository.CRAWLER_ID);
        Model crawler = crawlerRepository.crawlerFor(crawlerId);
        return AbstractCrawler.more(crawler);
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
}
