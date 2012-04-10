package com.googlecode.barongreenback.persistence;

import com.googlecode.totallylazy.Uri;

public class PersistenceUri extends Uri {
    public PersistenceUri(CharSequence value) {
        super(value);
    }

    public PersistenceUri(PersistenceProperties properties) {
        super(properties.getProperty("uri", "lucene:mem"));
    }
}
