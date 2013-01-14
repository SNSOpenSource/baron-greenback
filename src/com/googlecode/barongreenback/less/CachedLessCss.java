package com.googlecode.barongreenback.less;

import java.util.Date;

public class CachedLessCss {
    private String less;
    private Date lastModified;

    public CachedLessCss(String less, Date lastModified) {
        this.less = less;
        this.lastModified = lastModified;
    }

    public String less() {
        return less;
    }

    public Date lastModified() {
        return lastModified;
    }

    public boolean modifiedSince(Date lastModified) {
        return lastModified.after(lastModified());
    }
}
