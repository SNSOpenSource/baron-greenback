package com.googlecode.barongreenback.persistence;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.mappings.StringMapping;

public class ModelMapping implements StringMapping<Model> {
    @Override
    public String toString(Model value) throws Exception {
        return value.toString();
    }

    @Override
    public Model toValue(String value) throws Exception {
        return Model.parse(value);
    }
}
