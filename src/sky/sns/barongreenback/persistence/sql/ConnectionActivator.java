package com.googlecode.barongreenback.persistence.sql;

import com.googlecode.barongreenback.persistence.PersistencePassword;
import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.barongreenback.persistence.PersistenceUser;
import com.googlecode.totallylazy.Closeables;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.util.concurrent.Callable;

import static java.sql.DriverManager.getConnection;

public class ConnectionActivator implements Callable<Connection>, Closeable {
    private final PersistenceUri uri;
    private final PersistenceUser user;
    private final PersistencePassword password;
    private Connection connection;

    public ConnectionActivator(PersistenceUri uri, PersistenceUser user, PersistencePassword password) {
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection call() throws Exception {
        return connection = getConnection(uri.toString(), user.value(), password.value());
    }

    @Override
    public void close() throws IOException {
        Closeables.close(connection);
    }
}
