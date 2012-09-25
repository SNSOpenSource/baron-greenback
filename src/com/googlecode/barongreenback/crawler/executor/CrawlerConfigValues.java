package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;

import static com.googlecode.totallylazy.Predicates.is;

enum CrawlerConfigValues {
    INPUT_HANDLER_THREADS("inputHandlerThreads"),
    INPUT_HANDLER_CAPACITY("inputHandlerCapacity"),
    PROCESS_HANDLER_THREADS("processHandlerThreads"),
    PROCESS_HANDLER_CAPACITY("processHandlerCapacity"),
    OUTPUT_HANDLER_THREADS("outputHandlerThreads"),
    OUTPUT_HANDLER_SECONDS("outputHandlerSeconds");

    private String displayName;

    CrawlerConfigValues(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static CrawlerConfigValues fromDisplayName(String displayName) {
        return Sequences.sequence(values()).find(Predicates.where(functions.displayName(), is(displayName))).get();
    }

    private static class functions {
        private static Callable1<CrawlerConfigValues, String> displayName() {
            return new Callable1<CrawlerConfigValues, String>() {
                @Override
                public String call(CrawlerConfigValues values) throws Exception {
                    return values.displayName();
                }
            };
        }
    }
}
