package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Uri;

public class Feed {
    private final Uri uri;
    private final RecordDefinition recordDefinition;

    public Feed(Uri uri, RecordDefinition recordDefinition) {
        this.uri = uri;
        this.recordDefinition = recordDefinition;
    }

    public Uri uri() {
        return uri;
    }

    public RecordDefinition recordDefinition() {
        return recordDefinition;
    }
}
