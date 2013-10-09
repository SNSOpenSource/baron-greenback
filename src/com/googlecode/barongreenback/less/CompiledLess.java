package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Eq;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.annotations.multimethod;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.util.Date;

import static com.googlecode.totallylazy.Sequences.sequence;

public class CompiledLess extends Eq {
    private String less;
    private Date lastModified;

    public CompiledLess(String less, Date lastModified) {
        this.less = less;
        this.lastModified = lastModified;
    }

    public String less() {
        return less;
    }

    public Date lastModified() {
        return lastModified;
    }

    public boolean stale(Date date) {
        return lastModified.before(date);
    }

    @multimethod
    public boolean equals(CompiledLess other) {
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
        public static LogicalPredicate<CompiledLess> stale(final Date date) {
            return new LogicalPredicate<CompiledLess>() {
                @Override
                public boolean matches(CompiledLess other) {
                    return other.stale(date);
                }
            };
        }

        public static Mapper<CompiledLess, String> less = new Mapper<CompiledLess, String>() {
            @Override
            public String call(CompiledLess compiledLess) throws Exception {
                return compiledLess.less();
            }
        };
    }
}
