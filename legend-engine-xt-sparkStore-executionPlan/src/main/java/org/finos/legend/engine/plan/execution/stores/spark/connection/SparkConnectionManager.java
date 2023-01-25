package org.finos.legend.engine.plan.execution.stores.spark.connection;

import org.eclipse.collections.api.list.MutableList;
import org.finos.legend.engine.plan.execution.stores.StoreExecutionState;
import org.finos.legend.engine.plan.execution.stores.relational.connection.manager.ConnectionManagerSelector;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.DatabaseConnection;
import org.pac4j.core.profile.CommonProfile;

import java.sql.Connection;

public class SparkConnectionManager {

    private final ConnectionManagerSelector connectionManagerSelector;
    private final MutableList<CommonProfile> profiles;
    private final DatabaseConnection databaseConnection;
    private final StoreExecutionState.RuntimeContext runtimeContext;

    public SparkConnectionManager(ConnectionManagerSelector connectionManagerSelector, MutableList<CommonProfile> profiles, DatabaseConnection databaseConnection, StoreExecutionState.RuntimeContext runtimeContext) {
        this.connectionManagerSelector = connectionManagerSelector;
        this.profiles = profiles;
        this.databaseConnection = databaseConnection;
        this.runtimeContext = runtimeContext;
    }

    public Connection getConnection() {
        return connectionManagerSelector.getDatabaseConnection(profiles, databaseConnection, runtimeContext);
    }
}