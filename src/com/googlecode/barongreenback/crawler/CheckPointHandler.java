package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.mappings.StringMappings;

import static com.googlecode.funclate.Model.model;

public class CheckPointHandler {
    private final StringMappings mappings;

    public CheckPointHandler(StringMappings mappings) {
        this.mappings = mappings;
    }

    public Object lastCheckPointFor(Model crawler) throws Exception {
        final String checkpoint = crawler.get("checkpoint", String.class);
        final String checkpointType = crawler.get("checkpointType", String.class);
        return convertFromString(checkpoint, checkpointType);
    }

    public Model addCheckpoint(Model crawler, Object checkpoint) {
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
