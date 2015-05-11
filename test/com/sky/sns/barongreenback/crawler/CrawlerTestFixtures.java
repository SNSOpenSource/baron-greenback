package com.sky.sns.barongreenback.crawler;

import com.sky.sns.barongreenback.shared.RecordDefinition;
import com.sky.sns.barongreenback.views.ViewsRepository;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;

import java.net.URI;

import static com.sky.sns.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;

public class CrawlerTestFixtures {
    public static final Keyword<Integer> USER_ID = keyword("summary/userId", Integer.class).metadata(Keywords.unique, true);
    public static final Keyword<String> FIRST = keyword("first", String.class);
    public static final Keyword<String> STATUS = keyword("status", String.class);
    public static final Keyword<String> FIRST_NAME = keyword("summary/firstName", String.class).as(FIRST);
    public static final Keyword<String> STATUS_CODE = keyword("summary/statusCode", String.class).as(STATUS);
    public static final Definition USER = definition("/user", USER_ID, FIRST_NAME, STATUS_CODE);
    public static final RecordDefinition ENTRY_DEFINITION = new RecordDefinition(USER);
    public static final Keyword<String> ID = keyword("id", String.class).metadata(Keywords.unique, true).metadata(ViewsRepository.VISIBLE, true);
    public static final Keyword<URI> LINK = keyword("link/@href", URI.class).
            metadata(Keywords.unique, true).metadata(RECORD_DEFINITION, ENTRY_DEFINITION);
    public static final Keyword<String> UPDATED = keyword("updated", String.class).metadata(CompositeCrawler.CHECKPOINT, true);
    public static final Keyword<String> TITLE = keyword("title", String.class);
    public static final Definition ENTRIES = definition("/feed/entry", ID, LINK, UPDATED, TITLE);
    public static final RecordDefinition ATOM_DEFINITION = new RecordDefinition(ENTRIES);
}
