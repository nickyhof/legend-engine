package org.finos.legend.engine.plan.execution.stores.spark.connection;

import org.apache.spark.sql.jdbc.JdbcConnectionProvider;

import java.sql.Connection;
import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;

public class SparkConnectionProvider extends JdbcConnectionProvider {

    private static final Map<String, SparkConnectionManager> connectionManagers = new HashMap<>();

    @Override
    public String name() {
        return "legend";
    }

    public static void registerConnectionManager(String url, SparkConnectionManager sparkConnectionManager) {
        connectionManagers.put(url, sparkConnectionManager);
    }

    @Override
    public boolean canHandle(Driver driver, scala.collection.immutable.Map<String, String> options) {
        return connectionManagers.containsKey(options.get("url").get());
    }

    @Override
    public Connection getConnection(Driver driver, scala.collection.immutable.Map<String, String> options) {
        return connectionManagers.get(options.get("url").get()).getConnection();
    }
}