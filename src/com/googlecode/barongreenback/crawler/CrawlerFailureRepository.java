package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.shared.Repository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Uri;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CrawlerFailureRepository implements Repository<UUID, Failure>  {
    private static final Keyword<UUID> failureId = keyword("failureId", UUID.class);
    private static final Keyword<String> type = keyword("type", String.class);
    private static final Keyword<String> reason = keyword("reason", String.class);
    private static final Keyword<Uri> uri = keyword("uri", Uri.class);
    private static final Keyword<String> source = keyword("source", String.class);
    private static final Keyword<UUID> crawlerId = keyword("crawlerId", UUID.class);
    private static final Keyword<String> record= keyword("record", String.class);

    private static final Definition failures = Definition.constructors.definition("failures", failureId, type, reason, uri, crawlerId, source, record);

    private final Records records;
    private final CrawlerRepository crawlerRepository;

    public CrawlerFailureRepository(BaronGreenbackRecords records, CrawlerRepository crawlerRepository) {
        this.crawlerRepository = crawlerRepository;
        this.records = records.value();
    }

    private static final Map<Class, String> typeMap = Maps.map(sequence(
            Pair.<Class, String>pair(MasterPaginatedHttpJob.class, "master"),
            Pair.<Class, String>pair(PaginatedHttpJob.class, "paginated"),
            Pair.<Class, String>pair(HttpJob.class, "http")));

    private String nameForClass(Class aClass) {
        return typeMap.get(aClass);
    }

    @Override
    public void set(UUID id, Failure failure) {
        records.put(failures, update(using(failureId),
                record().
                        set(failureId, id).
                        set(type, nameForClass(failure.job().getClass())).
                        set(reason, failure.reason()).
                        set(source, RecordDefinition.toModel(failure.job().datasource().source()).toString()).
                        set(record, toJson(failure.job().datasource().record())).
                        set(crawlerId, failure.job().datasource().crawlerId()).
                        set(uri, failure.job().datasource().uri())));
    }

    @Override
    public Option<Failure> get(UUID id) {
        Record record = records.get(failures).find(where(failureId, is(id))).get();
        UUID crawlerId = record.get(CrawlerFailureRepository.crawlerId);
        HttpDatasource datasource = HttpDatasource.datasource(
                record.get(uri),
                crawlerId,
                RecordDefinition.convert(Model.parse(record.get(source))).definition(),
                fromJson(record.get(CrawlerFailureRepository.record)));
        Definition destination = AbstractCrawler.destinationDefinition(crawlerRepository.crawlerFor(crawlerId));
        HttpJob job = HttpJob.httpJob(datasource, destination);
        return some(Failure.failure(job, record.get(reason)));
    }

    private String toJson(Record record) {
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

    @Override
    public void remove(UUID key) {
        throw new UnsupportedOperationException("not done yet");
    }
}
