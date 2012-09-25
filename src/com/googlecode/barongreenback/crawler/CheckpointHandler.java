package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Option;

import java.util.UUID;

import static com.googlecode.funclate.Model.mutable.model;

public class CheckpointHandler {
    private final StringMappings mappings;
    private final ModelRepository modelRepository;

    public CheckpointHandler(StringMappings mappings, ModelRepository modelRepository) {
        this.mappings = mappings;
        this.modelRepository = modelRepository;
    }

    public Object lastCheckPointFor(Model crawler) throws Exception {
        final String checkpoint = crawler.get("checkpoint", String.class);
        final String checkpointType = crawler.get("checkpointType", String.class);
        return convertFromString(checkpoint, checkpointType);
    }

    public void updateCheckPoint(UUID id, Model crawler, Option<?> checkpoint) {
        if (!checkpoint.isEmpty()) {
            modelRepository.set(id, addCheckpoint(crawler, checkpoint.value()));
        }
    }

    private Model addCheckpoint(Model crawler, Object checkpoint) {
        return model().set("form", crawler.set("checkpoint", convertToString(checkpoint)).set("checkpointType", getCheckPointType(checkpoint)));
    }

    private Object convertFromString(String checkpoint, String checkpointType) throws Exception {
        Class<?> aClass = checkpointType == null ? String.class : Class.forName(checkpointType);
        return mappings.get(aClass).toValue(checkpoint);
    }

    private String convertToString(Object checkpoint) {
        return mapAsString(checkpoint);
    }

    private String getCheckPointType(Object checkpoint) {
        if(checkpoint == null) return String.class.getName();
        return checkpoint.getClass().getName();
    }

    private String mapAsString(Object instance) {
        if(instance == null) return "";
        return mappings.toString(instance.getClass(), instance);
    }
}
