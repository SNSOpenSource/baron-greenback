package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.regex.Regex;

import java.util.regex.MatchResult;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.Sequences.sequence;

public class ParametersToModel {
    public static Model modelOf(Iterable<Pair<String, String>> parameters) {
        return sequence(parameters).fold(model(), new Callable2<Model, Pair<String, String>, Model>() {
            public Model call(Model model, Pair<String, String> pair) throws Exception {
                String key = pair.first();
                Object value = toValue(pair.second());
                if (key.contains(".")) {
                    Sequence<String> hierarchicalKey = sequence(key.split("\\."));
                    parentOf(model, hierarchicalKey.init()).
                            add(hierarchicalKey.last(), value);
                    return model;
                }
                return model.add(key, value);
            }
        });
    }

    private static Object toValue(final String value) {
        if (value.equals("true")) {
            return true;
        }
        if (value.equals("false")) {
            return false;
        }
        return value;
    }

    private static Regex list = Regex.regex("([^\\[]+)\\[(\\d+)\\]$");

    private static Model parentOf(final Model startingModel, final Sequence<String> parents) {
        return parents.fold(startingModel, new Callable2<Model, String, Model>() {
            public Model call(Model model, String parent) throws Exception {
                if (list.matches(parent)) {
                    MatchResult match = list.match(parent);
                    return getModel(model, match.group(1), Integer.valueOf(match.group(2)) - 1);
                }
                return fixMeModel(model, parent, 0);
            }
        });
    }

    private static Model fixMeModel(Model model, String name, Integer index) {
        int numb = numberOfModelsNeeded(model, name, index + 1);
        for (int i = 0; i < numb; i++) {
            model.add(name, model());
        }
        return model.getValues(name, Model.class).get(index);
    }

    private static Model getModel(Model model, String name, Integer index) {
        int numb = numberOfModelsNeeded(model, name, index + 1);
        model.add(name, repeat(model()).take(numb).toList());
        return model.getValues(name, Model.class).get(index);
    }

    private static int numberOfModelsNeeded(Model model, String name, final int numberNeeded) {
        if (!model.contains(name)) {
            return numberNeeded;
        }
        return numberNeeded - model.getValues(name).size();
    }
}
