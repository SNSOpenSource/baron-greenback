package com.sky.sns.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Strings;
import com.sky.sns.barongreenback.persistence.BaronGreenbackStringMappings;
import com.sky.sns.barongreenback.shared.ModelRepository;

import java.util.UUID;

import static com.googlecode.funclate.Model.mutable.model;

public class CheckpointHandler {
    private final StringMappings mappings;
    private final ModelRepository modelRepository;

    public CheckpointHandler(BaronGreenbackStringMappings mappings, ModelRepository modelRepository) {
        this.mappings = mappings.value();
        this.modelRepository = modelRepository;
    }

    public Object lastCheckpointFor(Model crawler) throws Exception {
        final String checkpoint = crawler.get("checkpoint", String.class);
        final String checkpointType = crawler.get("checkpointType", String.class);
        return convertFromString(checkpoint, checkpointType);
    }

    public void updateCheckpoint(UUID id, Model crawler, Option<?> checkpoint) {
        if (!checkpoint.isEmpty()) {
            modelRepository.set(id, addCheckpoint(crawler, checkpoint.value()));
        }
    }

    private Model addCheckpoint(Model crawler, Object checkpoint) {
        return model().set("form", crawler.set("checkpoint", convertToString(checkpoint)).set("checkpointType", getCheckpointType(checkpoint)));
    }

    private Object convertFromString(String checkpoint, String checkpointType) throws Exception {
        Class<?> aClass = Strings.isEmpty(checkpointType) ? String.class : Class.forName(checkpointType);
        return mappings.get(aClass).toValue(checkpoint);
    }

    private String convertToString(Object checkpoint) {
        return mapAsString(checkpoint);
    }

    private String getCheckpointType(Object checkpoint) {
        if (checkpoint == null) return String.class.getName();
        return checkpoint.getClass().getName();
    }

    private String mapAsString(Object instance) {
        if (instance == null) return "";
        return mappings.toString(instance.getClass(), instance);
    }
}
