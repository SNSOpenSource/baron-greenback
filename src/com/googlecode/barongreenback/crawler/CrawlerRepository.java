package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.Forms;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;

import java.util.UUID;

import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static java.util.UUID.randomUUID;

public class CrawlerRepository {
    private final ModelRepository modelRepository;

    public CrawlerRepository(final ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public Sequence<Pair<UUID, Model>> allCrawlerModels() {
        return modelRepository.find(where(MODEL_TYPE, is("form")));
    }

    public Option<Model> modelFor(UUID id) {
        return modelRepository.get(id);
    }

    public Option<Model> copy(UUID id) {
        Option<Model> crawlerOption = modelFor(id).map(copyOfCrawler());
        for (Model crawler : crawlerOption) modelRepository.set(UUID.randomUUID(), crawler);
        return crawlerOption;
    }

    private Callable1<Model, Model> copyOfCrawler() {
        return new Callable1<Model, Model>() {
            @Override
            public Model call(Model crawler) throws Exception {
                Model root = crawler.get("form", Model.class);
                root.set("enabled", false);
                root.set("update", "copy of " + root.get("update", String.class));
                return crawler;
            }
        };
    }

    public void importCrawler(Option<UUID> id, String model) {
        modelRepository.set(id.getOrElse(randomUUID()), Model.parse(model));
    }

    public void remove(UUID id) {
        modelRepository.remove(id);
    }

    public void reset(UUID id) {
        Model model = modelFor(id).get();
        Model form = model.get("form", Model.class);
        form.remove("checkpoint", String.class);
        form.add("checkpoint", "");
        form.remove("checkpointType", String.class);
        form.add("checkpointType", String.class.getName());
        modelRepository.set(id, model);
    }

    public void edit(UUID id, Model root) {
        Model form = root.get("form", Model.class);
        String from = form.get("from", String.class);
        String update = form.get("update", String.class);
        String more = form.get("more", String.class);
        String checkpoint = form.get("checkpoint", String.class);
        String checkpointType = form.get("checkpointType", String.class);
        Boolean enabled = form.get("enabled", Boolean.class);
        Model record = form.get("record", Model.class);
        RecordDefinition recordDefinition = convert(record);
        modelRepository.set(id, Forms.crawler(update, from, more, checkpoint, checkpointType, enabled, recordDefinition.toModel()));
    }

    public Boolean enabled(Model model) {
        return model.get("form", Model.class).get("enabled", Boolean.class);
    }

}
