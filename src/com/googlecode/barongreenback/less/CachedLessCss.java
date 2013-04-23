package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Eq;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.annotations.multimethod;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.util.Date;

import static com.googlecode.totallylazy.Sequences.sequence;

public class CachedLessCss extends Eq {
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

    @multimethod
    public boolean equals(CachedLessCss other) {
        return less.equals(other.less) && lastModified.equals(other.lastModified);
    }

    @Override
    public int hashCode() {
        return less.hashCode() * lastModified.hashCode() * 19;
    }

    @Override
    public String toString() {
        return sequence(lastModified, less).toString("\n");
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
