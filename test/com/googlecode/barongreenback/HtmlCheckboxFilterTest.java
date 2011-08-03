package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static org.hamcrest.MatcherAssert.assertThat;

public class HtmlCheckboxFilterTest {
    @Test
    public void negativeOnly() throws Exception {
        Sequence<String> values = Sequences.sequence("false");
        assertThat(new HtmlCheckboxFilter<String>("false").filter(values), hasExactly("false"));
    }

    @Test
    public void positive() throws Exception {
        Sequence<String> values = Sequences.sequence("true", "false");
        assertThat(new HtmlCheckboxFilter<String>("false").filter(values), hasExactly("true"));
    }
    @Test
    public void correctlyFilters() throws Exception {
        Sequence<String> values = Sequences.sequence("true", "false", "true", "false", "false", "false", "true", "false");
        assertThat(new HtmlCheckboxFilter<String>("false").filter(values), hasExactly("true", "true", "false", "false", "true"));
    }
}
