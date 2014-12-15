package com.googlecode.barongreenback.search;

public interface ShortcutPolicy {
    boolean shouldShortcut(String view, String query, DrillDowns drillDowns);
}
