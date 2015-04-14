package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.totallylazy.Value;

public enum DirectoryType implements Value<String> {

    File("file"), Memory("mem"), Nio("nio");

    private final String schema;

    DirectoryType(String schema){
        this.schema = schema;
    }

    public String value() {
        return schema;
    }
}
