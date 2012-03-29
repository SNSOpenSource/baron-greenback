package com.googlecode.barongreenback.persistence.sql;

import java.sql.Connection;
import java.util.concurrent.Callable;

import static java.sql.DriverManager.getConnection;

public class ConnectionActivator implements Callable<Connection> {
    @Override
    public Connection call() throws Exception {
        return getConnection("jdbc:h2:mem:baron-greenback", "SA", "");
    }
}
