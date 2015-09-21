package com.sky.sns.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.*;
import com.sky.sns.barongreenback.shared.Forms;
import com.sky.sns.barongreenback.shared.ModelRepository;
import com.sky.sns.barongreenback.shared.RecordDefinition;

import java.util.UUID;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.funclate.Model.mutable.parse;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.sky.sns.barongreenback.crawler.Crawler.methods.name;
import static com.sky.sns.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.sky.sns.barongreenback.shared.RecordDefinition.convert;
import static java.util.UUID.randomUUID;

public class CrawlerRepository {
    private static final String NAME = "name";
    private static final String UPDATE = "update";
    private static final String COPY_PREFIX = "copy of ";
    private final ModelRepository modelRepository;

    public CrawlerRepository(final ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public Model crawlerFor(UUID id) {
        final Option<Model> modelOption = modelFor(id);
        if(modelOption.isEmpty()){
            return model();
        }
        return modelOption.get().get("form", Model.class);
    }

    public Sequence<Pair<UUID, Model>> allCrawlerModels() {
        return modelRepository.find(where(MODEL_TYPE, is("form"))).map(Callables.<UUID, Model, Model>second(addName()));
    }

    public Option<Model> modelFor(UUID id) {
        return modelRepository.get(id).map(addName());
    }

    private Callable1<? super Model, ? extends Model> addName() {
        return new Callable1<Model, Model>() {
            @Override
            public Model call(Model model) throws Exception {
                Model form = model.get("form", Model.class);
                if (!form.contains(NAME) || Strings.isEmpty(form.get(NAME, String.class))) {
                    form.set(NAME, form.get(UPDATE, String.class));
                }
                return model;
            }
        };
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
                root.set("disabled", true);
                root.set(NAME, COPY_PREFIX + root.get(NAME, String.class));
                return crawler;
            }
        };
    }

    public void importCrawler(Option<UUID> id, String model) {
        modelRepository.set(id.getOrElse(randomUUID()), parse(model));
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
        String update = form.get(UPDATE, String.class);
        String name = form.get(NAME, String.class);
        String more = form.get("more", String.class);
        String checkpoint = form.get("checkpoint", String.class);
        String checkpointType = form.get("checkpointType", String.class);
        Boolean disabled = !predicates.enabled(root);
        Model record = form.get("record", Model.class);
        RecordDefinition recordDefinition = convert(record);
        modelRepository.set(id, Forms.crawler(name, update, from, more, checkpoint, checkpointType, disabled, recordDefinition.toModel()));
    }

    public static class predicates {
        public static Predicate<Model> enabled() {
            return new Predicate<Model>() {
                @Override
                public boolean matches(Model model) {
                    return enabled(model);
                }
            };
        }

        public static Boolean enabled(Model model) {
            return !option(model.get("form", Model.class).get("disabled", Boolean.class)).getOrElse(false);
        }

        public static Predicate<Model> isCopy() {
            return new Predicate<Model>() {
                @Override
                public boolean matches(Model model) {
                    return isCopy(model);
                }
            };
        }

        public static Boolean isCopy(Model model) {
            return name(model.get("form", Model.class)).startsWith(COPY_PREFIX);
        }
    }

}
