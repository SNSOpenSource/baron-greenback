package com.googlecode.barongreenback.search;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.googlecode.barongreenback.search.DrillDowns.drillDowns;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;

public class DrillDownsShortcutPolicyTest {

    @Test
    public void shouldNotShortcutIfThereIsAtLeastOneDrillDownSpecified() throws Exception {
        final DrillDownsShortcutPolicy drillDownsShortcutPolicy = new DrillDownsShortcutPolicy(delegateReturns(true));
        final HashMap<String, List<String>> nonEmptyMap = new HashMap<String, List<String>>() {{
            put("facet", singletonList("value"));
        }};
        assertThat(drillDownsShortcutPolicy.shouldShortcut("", "", drillDowns(nonEmptyMap)), is(false));
    }

    @Test
    public void shouldDelegateIfTheSpecifiedDrillDownIsEmpty() throws Exception {
        assertThat(new DrillDownsShortcutPolicy(delegateReturns(true)).shouldShortcut("", "", DrillDowns.empty()), is(true));
        assertThat(new DrillDownsShortcutPolicy(delegateReturns(false)).shouldShortcut("", "", DrillDowns.empty()), is(false));
    }

    private ShortcutPolicy delegateReturns(final boolean result) {
        return new ShortcutPolicy() {
            @Override
            public boolean shouldShortcut(String view, String query, DrillDowns drillDowns) {
                return result;
            }
        };
    }
}