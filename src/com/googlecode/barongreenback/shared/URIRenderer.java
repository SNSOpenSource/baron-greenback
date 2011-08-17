package com.googlecode.barongreenback.shared;

import com.googlecode.totallylazy.Callable1;

import java.net.URI;

public class URIRenderer implements Callable1<URI, String> {
    public static Callable1<URI, String> toLink() {
        return new URIRenderer();
    }

    public String call(URI uri) throws Exception {
        return String.format("<a href=\"%1$s\">%1$s</a>", uri);
    }
}
