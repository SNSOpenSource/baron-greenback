package com.googlecode.barongreenback.search.parser;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GrammarTest {
    @Test
    public void quotes() throws Exception{
        assertThat(Grammar.QUOTED_TEXT.parse("\"foo bar\""), is("foo bar"));
    }

    @Test
    public void prefix() throws Exception{
        assertThat(Grammar.PREFIX.parse("+"), is(Prefix.Plus));
        assertThat(Grammar.PREFIX.parse("-"), is(Prefix.Minus));
        assertThat(Grammar.PREFIX.parse(""), is(Prefix.None));

    }

}
