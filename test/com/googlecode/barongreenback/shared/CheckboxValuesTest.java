package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.shared.CheckboxValues;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import static com.googlecode.totallylazy.Sequences.join;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static org.hamcrest.MatcherAssert.assertThat;

public class CheckboxValuesTest {
    @Test
    public void checkboxNotSelected() throws Exception {
        assertThat(new CheckboxValues(notSelected()), hasExactly(false));
        assertThat(new CheckboxValues(notSelected().join(notSelected())), hasExactly(false, false));
    }

    @Test
    public void checkbocSelected() throws Exception {
        assertThat(new CheckboxValues(selected()), hasExactly(true));
    }
    
    @Test
    public void supportsCombinations() throws Exception {
        assertThat(new CheckboxValues(join(selected(), selected(), notSelected(), notSelected(), selected(), notSelected(), notSelected())),
                hasExactly(true, true, false, false, true, false, false));
    }

    private Sequence<String> selected() {
        return Sequences.sequence("true", "false");
    }

    private Sequence<String> notSelected() {
        return Sequences.sequence("false");
    }
}
