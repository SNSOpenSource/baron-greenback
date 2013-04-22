package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

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

    public static class functions {
        public static LogicalPredicate<CachedLessCss> modifiedSince(final Date lastModified) {
            return new LogicalPredicate<CachedLessCss>() {
                @Override
                public boolean matches(CachedLessCss other) {
                    return other.modifiedSince(lastModified);
                }
            };
        }

        public static Mapper<CachedLessCss, String> less = new Mapper<CachedLessCss, String>() {
            @Override
            public String call(CachedLessCss cachedLessCss) throws Exception {
                return cachedLessCss.less();
            }
        };
    }
}
