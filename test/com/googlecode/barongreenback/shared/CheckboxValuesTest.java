package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.shared.CheckboxValues;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import static com.googlecode.totallylazy.Sequences.join;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static org.hamcrest.MatcherAssert.assertThat;

public class CheckboxValuesTest {
    @Test
    public void checkboxNotSelected() throws Exception {
        assertThat(new CheckboxValues(notSelected()), hasExactly(false));
    }

    @Test
    public void checkbocSelected() throws Exception {
        assertThat(new CheckboxValues(selected()), hasExactly(true));
    }
    @Test
    public void supportsCombinations() throws Exception {
        assertThat(new CheckboxValues(join(selected(), selected(), notSelected(), notSelected(), selected())), hasExactly(true, true, false, false, true));
    }

    private Sequence<String> selected() {
        return Sequences.sequence("true", "false");
    }

    private Sequence<String> notSelected() {
        return Sequences.sequence("false");
    }
}
