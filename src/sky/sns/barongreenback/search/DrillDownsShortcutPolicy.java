package com.googlecode.barongreenback.search;

public class DrillDownsShortcutPolicy implements ShortcutPolicy {

    private final ShortcutPolicy decoratedShortcutPolicy;

    public DrillDownsShortcutPolicy(ShortcutPolicy decoratedShortcutPolicy) {
        this.decoratedShortcutPolicy = decoratedShortcutPolicy;
    }

    @Override
    public boolean shouldShortcut(String view, String query, DrillDowns drillDowns) {
        return drillDowns.equals(DrillDowns.empty()) && decoratedShortcutPolicy.shouldShortcut(view, query, drillDowns);
    }
}
