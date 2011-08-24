package com.googlecode.barongreenback.shared;

import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.utterlyidle.FormParameters;
import org.junit.Test;

import java.net.URI;

import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.ALIASES;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.KEYWORD_NAME;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.SUBFEED_PREFIX;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.RECORD_NAME;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.SUBFEED;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.TYPES;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.UNIQUE;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.VISIBLE;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RecordDefinitionExtractorTest {
    @Test
    public void correctlyExtractsFormParamters() throws Exception {
        FormParameters form = FormParameters.formParameters(pair(RECORD_NAME, "/feed/entry"),
                pair(KEYWORD_NAME, "name"),
                pair(ALIASES, ""),
                pair(TYPES, "java.lang.String"),
                pair(UNIQUE, "false"),
                pair(VISIBLE, "false"),
                pair(SUBFEED, "false"),
                pair(SUBFEED_PREFIX, "field1"),

                pair(KEYWORD_NAME, "id"),
                pair(ALIASES, ""),
                pair(TYPES, "java.lang.Integer"),
                pair(UNIQUE, "true"), pair(UNIQUE, "false"),
                pair(VISIBLE, "false"),
                pair(SUBFEED, "false"),
                pair(SUBFEED_PREFIX, "field2")

        );

        RecordDefinitionExtractor extractor = new RecordDefinitionExtractor(form);

        RecordDefinition definition = extractor.extract();
        Keyword<Object> keyword = keyword("/feed/entry");
        assertThat(definition.recordName(), is(keyword));
        assertThat(definition.fields(), hasExactly(keyword("name", String.class), keyword("id", Integer.class)));
        assertThat(uniqueFields(definition), hasExactly(keyword("id", Integer.class)));
    }

    @Test
    public void supportsSubFeed() throws Exception {
        String prefix = "field1.";

        FormParameters form = FormParameters.formParameters(
                pair(RECORD_NAME, "/feed/entry"),
                pair(KEYWORD_NAME, "link"),
                pair(ALIASES, ""),
                pair(TYPES, "java.net.URI"),
                pair(UNIQUE, "false"),
                pair(VISIBLE, "false"),
                pair(SUBFEED, "true"), pair(SUBFEED, "false"),
                pair(SUBFEED_PREFIX, prefix),

                pair(prefix + RECORD_NAME, "/user/summary"),
                pair(prefix + KEYWORD_NAME, "ID"),
                pair(prefix + ALIASES, ""),
                pair(prefix + TYPES, "java.lang.Integer"),
                pair(prefix + UNIQUE, "true"), pair(prefix + UNIQUE, "false"),
                pair(prefix + VISIBLE, "false"),
                pair(prefix + SUBFEED, "false"),
                pair(prefix + SUBFEED_PREFIX, prefix + "field1.")

        );

        RecordDefinitionExtractor extractor = new RecordDefinitionExtractor(form);

        RecordDefinition definition = extractor.extract();
        Keyword<Object> keyword = keyword("/feed/entry");
        assertThat(definition.recordName(), is(keyword));
        assertThat(definition.fields(), hasExactly(keyword("link", URI.class)));

        Keyword<Object> subRoot = keyword("/user/summary");
        RecordDefinition subDefinition = definition.fields().head().metadata().get(RecordDefinition.RECORD_DEFINITION);
        assertThat(subDefinition.recordName(), is(subRoot));
        assertThat(subDefinition.fields(), hasExactly(keyword("id", Integer.class)));
        assertThat(uniqueFields(subDefinition), hasExactly(keyword("id", Integer.class)));

    }
}
