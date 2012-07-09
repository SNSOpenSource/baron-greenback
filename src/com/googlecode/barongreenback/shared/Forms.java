package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequences;

import java.net.URI;
import java.util.Date;
import java.util.List;

import static com.googlecode.funclate.Model.model;

public class Forms {
    public static final Integer NUMBER_OF_FIELDS = 3;

    public static Model crawler(String update, String from, String more, String checkpoint, String checkpointType, Model definition) {
        return model().
                add("form", model().
                        add("update", update).
                        add("from", from).
                        add("more", more).
                        add("checkpoint", checkpoint).
                        add("checkpointType", checkpointType).
                        add("record", definition));
    }

    public static Model emptyForm(Integer numberOfFields) {
        return addTemplates(crawler("", "", "", "", "", emptyDefinition(numberOfFields(numberOfFields))));
    }

    public static Model emptyKeyword() {
        return model().add("visible", true);
    }

    public static int numberOfFields(Integer numberOfFields) {
        return Math.min(Math.max(numberOfFields, 1), 100);
    }

    public static Model emptyDefinition(int number) {
        return RecordDefinition.recordDefinition("", Sequences.repeat(emptyKeyword()).take(number).toArray(Model.class));
    }

    public static List<Model> types(Class... classes) {
        return Sequences.sequence(classes).map(new Callable1<Class, Model>() {
            public Model call(Class aClass) throws Exception {
                return model().
                        add("name", aClass.getSimpleName()).
                        add("value", aClass.getName()).
                        add(aClass.getName(), true); // enable selected
            }
        }).toList();
    }

    public static Model addTemplates(Model model) {
        return model.add("emptyKeyword", emptyKeyword()).
                add("types", types(String.class, Date.class, URI.class));
    }

    public static class functions {
        public static Callable1<Model, Model> addTemplates() {
            return new Callable1<Model, Model>() {
                @Override
                public Model call(Model model) throws Exception {
                    return Forms.addTemplates(model);
                }
            };
        }
    }
}
