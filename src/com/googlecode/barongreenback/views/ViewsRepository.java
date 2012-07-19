package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.crawler.AbstractCrawler;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static java.util.UUID.randomUUID;

public class ViewsRepository {

    private final ModelRepository modelRepository;

    public ViewsRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public static Model viewModel(Sequence<Keyword<?>> keywords, String name, String records, String query, boolean visible, String priority) {
        return model().add(Views.ROOT, model().
                add("name", name).
                add("records", records).
                add("query", query).
                add("visible", visible).
                add("priority", priority).
                add("keywords", keywords.map(Views.asModel()).toList()));
    }

    public static void ensureViewForCrawlerExists(ViewsRepository viewRepository, Model crawler, Sequence<Keyword<?>> keywords) {
        final String name = AbstractCrawler.name(crawler);
        if (viewRepository.viewForName(name).isEmpty()) {
            viewRepository.set(randomUUID(), viewModel(keywords, name, AbstractCrawler.update(crawler), "", true, ""));
        }
    }

    public Option<Model> viewForName(String name) {
        return Views.find(modelRepository, name);
    }

    public void set(UUID uuid, Model model) {
        modelRepository.set(uuid, model);
    }
}
