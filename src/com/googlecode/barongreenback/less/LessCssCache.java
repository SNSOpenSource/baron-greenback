package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Uri;

public interface LessCssCache {

    boolean containsKey(Uri uri);

    String get(Uri uri);

    void put(Uri uri, String result);
}
