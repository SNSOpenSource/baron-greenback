package com.googlecode.barongreenback.persistence.sql;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.lazyrecords.sql.SqlRecords;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class SqlPersistence implements Persistence{
    private final SqlRecords sqlRecords;

    public SqlPersistence(SqlRecords sqlRecords) {
        this.sqlRecords = sqlRecords;
    }

    @Override
    public void deleteAll() throws Exception {
        sqlRecords.update(textOnly("drop all objects"));
    }

    public static String h2Mem() {
        return "jdbc:h2:mem:baron-greenback";
    }
}
