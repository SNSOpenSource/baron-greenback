package com.googlecode.barongreenback.persistence.sql;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.barongreenback.persistence.lucene.DirectoryActivator;
import com.googlecode.lazyrecords.sql.SqlRecords;
import com.googlecode.totallylazy.Files;

import java.io.File;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.totallylazy.Files.temporaryDirectory;

public class SqlPersistence implements Persistence{
    private final SqlRecords sqlRecords;

    public SqlPersistence(SqlRecords sqlRecords) {
        this.sqlRecords = sqlRecords;
    }

    @Override
    public void delete() throws Exception {
        sqlRecords.update(textOnly("drop all objects"));
    }

    @Override
    public void backup(File destination) throws Exception {
        Files.delete(destination);
        sqlRecords.update(expression("backup to ?", destination));
    }

    @Override
    public void restore(File file) throws Exception {
        throw new UnsupportedOperationException();
    }

    public static String h2Mem() {
        return "jdbc:h2:mem:baron-greenback";
    }

    public static String h2TemporaryDirectory(String name) {
        return h2Directory(temporaryDirectory(name));
    }

    public static String h2Directory(File file) {
        return String.format("jdbc:h2://%s", file);
    }
}
