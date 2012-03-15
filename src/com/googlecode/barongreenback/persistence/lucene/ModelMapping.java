package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.lucene.mappings.AbstractStringMapping;
import org.apache.lucene.document.Field;

public class ModelMapping extends AbstractStringMapping<Model> {
    public ModelMapping() {
        super(Field.Index.NOT_ANALYZED);
    }

    @Override
    protected String toString(Model value) throws Exception {
        return value.toString();
    }

    @Override
    protected Model fromString(String value) throws Exception {
        return Model.parse(value);
    }
}
