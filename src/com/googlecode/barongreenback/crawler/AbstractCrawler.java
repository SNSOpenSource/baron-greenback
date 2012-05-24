package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.DuplicateRemover.ignoreAlias;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.barongreenback.views.Views.find;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Uri.uri;
import static java.util.UUID.randomUUID;

public abstract class AbstractCrawler implements Crawler {
    protected final ModelRepository modelRepository;

    public AbstractCrawler(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    protected static Definition definition(Model crawler, RecordDefinition recordDefinition) {
        return Definition.constructors.definition(update(crawler), keywords(recordDefinition));
    }

    protected static Sequence<Keyword<?>> keywords(RecordDefinition recordDefinition) {
        return RecordDefinition.allFields(recordDefinition).map(ignoreAlias());
    }

    protected static String update(Model crawler) {
        return crawler.get("update", String.class);
    }

    protected RecordDefinition extractRecordDefinition(Model crawler) {
        return convert(crawler.get("record", Model.class));
    }

    protected Model crawlerFor(UUID id) {
        return modelRepository.get(id).get().get("form", Model.class);
    }

    protected void updateView(Model crawler, Sequence<Keyword<?>> keywords) {
        final String update = update(crawler);
        if (find(modelRepository, update).isEmpty()) {
            modelRepository.set(randomUUID(), model().add(Views.ROOT, model().
                    add("name", update).
                    add("records", update).
                    add("query", "").
                    add("visible", true).
                    add("priority", "").
                    add("keywords", keywords.map(Views.asModel()).toList())));
        }
    }

    protected Uri from(Model crawler) {
        return uri(crawler.get("from", String.class));
    }
}
