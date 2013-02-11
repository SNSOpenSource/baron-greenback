package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;

import java.net.URI;
import java.util.UUID;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CrawlerTestFixtures {
    public static final Keyword<Integer> USER_ID = keyword("summary/userId", Integer.class).metadata(Keywords.unique, true);
    public static final Keyword<String> FIRST = Keywords.keyword("first", String.class);
    public static final Keyword<String> FIRST_NAME = keyword("summary/firstName", String.class).as(FIRST);
    public static final Definition USER = definition("/user", USER_ID, FIRST_NAME);
    public static final RecordDefinition ENTRY_DEFINITION = new RecordDefinition(USER);
    public static final Keyword<String> ID = keyword("id", String.class).metadata(Keywords.unique, true).metadata(ViewsRepository.VISIBLE, true);
    public static final Keyword<URI> LINK = keyword("link/@href", URI.class).
            metadata(Keywords.unique, true).metadata(RECORD_DEFINITION, ENTRY_DEFINITION);
    public static final Keyword<String> UPDATED = keyword("updated", String.class).metadata(CompositeCrawler.CHECKPOINT, true);
    public static final Keyword<String> TITLE = keyword("title", String.class);
    public static final Definition ENTRIES = definition("/feed/entry", ID, LINK, UPDATED, TITLE);
    public static final RecordDefinition ATOM_DEFINITION = new RecordDefinition(ENTRIES);
}
