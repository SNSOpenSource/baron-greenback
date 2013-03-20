package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Pair;

import java.util.UUID;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;

public interface ModelRepository extends Repository<UUID, Model>, Finder<Pair<UUID, Model>> {
    Keyword<UUID> ID = keyword("models_id", UUID.class);
    Keyword<String> MODEL_TYPE = keyword("models_type", String.class);
    Keyword<Model> MODEL = keyword("model", Model.class);
    Definition MODELS = Definition.constructors.definition("models", ID, MODEL, MODEL_TYPE);
}
