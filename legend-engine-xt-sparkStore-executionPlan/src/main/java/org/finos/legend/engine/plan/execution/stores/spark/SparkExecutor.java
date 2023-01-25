package org.finos.legend.engine.plan.execution.stores.spark;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.plan.execution.nodes.helpers.freemarker.FreeMarkerExecutor;
import org.finos.legend.engine.plan.execution.nodes.state.ExecutionState;
import org.finos.legend.engine.plan.execution.result.ConstantResult;
import org.finos.legend.engine.plan.execution.result.Result;
import org.finos.legend.engine.plan.execution.result.StreamingResult;
import org.finos.legend.engine.plan.execution.stores.StoreType;
import org.finos.legend.engine.plan.execution.stores.relational.RelationalDatabaseCommandsVisitorBuilder;
import org.finos.legend.engine.plan.execution.stores.relational.activity.RelationalExecutionActivity;
import org.finos.legend.engine.plan.execution.stores.relational.config.RelationalExecutionConfiguration;
import org.finos.legend.engine.plan.execution.stores.relational.connection.driver.DatabaseManager;
import org.finos.legend.engine.plan.execution.stores.relational.connection.manager.ConnectionManagerSelector;
import org.finos.legend.engine.plan.execution.stores.relational.plugin.RelationalStoreExecutionState;
import org.finos.legend.engine.plan.execution.stores.relational.result.PreparedTempTableResult;
import org.finos.legend.engine.plan.execution.stores.relational.result.RealizedRelationalResult;
import org.finos.legend.engine.plan.execution.stores.relational.result.RelationalResult;
import org.finos.legend.engine.plan.execution.stores.spark.connection.SparkConnectionManager;
import org.finos.legend.engine.plan.execution.stores.spark.connection.SparkConnectionProvider;
import org.finos.legend.engine.plan.execution.stores.spark.result.DataFrameResult;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.ExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.RelationalExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.SQLExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.RelationalDatabaseConnection;
import org.pac4j.core.profile.CommonProfile;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SparkExecutor {

    private static final String DEFAULT_DB_TIME_ZONE = "GMT";

    public Result execute(SQLExecutionNode node, MutableList<CommonProfile> profiles, ExecutionState executionState) {
        String databaseTimeZone = node.getDatabaseTimeZone() == null ? DEFAULT_DB_TIME_ZONE : node.getDatabaseTimeZone();
        String databaseType = node.getDatabaseTypeName();
        List<String> tempTableList = FastList.newList();

        RelationalDatabaseConnection databaseConnection = (RelationalDatabaseConnection) node.connection;

        RelationalStoreExecutionState storeExecutionState = (RelationalStoreExecutionState) executionState.getStoreExecutionState(StoreType.Relational);

        ConnectionManagerSelector connectionManagerSelector = storeExecutionState.getRelationalExecutor().getConnectionManager();
        RelationalExecutionConfiguration relationalExecutionConfiguration = storeExecutionState.getRelationalExecutor().getRelationalExecutionConfiguration();

        Connection connectionManagerConnection = connectionManagerSelector.getDatabaseConnection(profiles, databaseConnection, storeExecutionState.getRuntimeContext());

        try {
            String url = connectionManagerConnection.getMetaData().getURL();
            this.prepareForSQLExecution(node, relationalExecutionConfiguration, connectionManagerConnection, databaseTimeZone, databaseType, tempTableList, profiles, executionState);

            SparkConnectionProvider.registerConnectionManager(url, new SparkConnectionManager(connectionManagerSelector, profiles, databaseConnection, storeExecutionState.getRuntimeContext()));

            return new DataFrameResult(executionState.activities, url);
        } catch (SQLException e) {
            throw new RuntimeException("Error using connection", e);
        }
    }

    //TODO: REMOVE THIS as it was copied from org.finos.legend.engine.plan.execution.stores.relational.RelationalExecutor
    private void prepareForSQLExecution(ExecutionNode node, RelationalExecutionConfiguration relationalExecutionConfiguration, Connection connection, String databaseTimeZone, String databaseTypeName, List<String> tempTableList, MutableList<CommonProfile> profiles, ExecutionState executionState)
    {
        String sqlQuery;

        sqlQuery = node instanceof RelationalExecutionNode ? ((RelationalExecutionNode) node).sqlQuery() : ((SQLExecutionNode) node).sqlQuery();

        DatabaseManager databaseManager = DatabaseManager.fromString(databaseTypeName);
        for (Map.Entry<String, Result> var : executionState.getResults().entrySet())
        {
            if (var.getValue() instanceof StreamingResult && sqlQuery.contains("(${" + var.getKey() + "})"))
            {
                String tableName = databaseManager.relationalDatabaseSupport().processTempTableName(var.getKey());
                this.prepareTempTable(connection, relationalExecutionConfiguration, (StreamingResult) var.getValue(), tableName, databaseTypeName, databaseTimeZone, tempTableList);
                tempTableList.add(tableName);
                sqlQuery = sqlQuery.replace("(${" + var.getKey() + "})", tableName);
            }
            else if (var.getValue() instanceof PreparedTempTableResult && sqlQuery.contains("(${" + var.getKey() + "})"))
            {
                sqlQuery = sqlQuery.replace("(${" + var.getKey() + "})", ((PreparedTempTableResult) var.getValue()).getTempTableName());
            }
            else if (var.getValue() instanceof RelationalResult && (sqlQuery.contains("inFilterClause_" + var.getKey() + "})") || sqlQuery.contains("${" + var.getKey() + "}")))
            {
                if (((RelationalResult) var.getValue()).columnCount == 1)
                {
                    RealizedRelationalResult realizedRelationalResult = (RealizedRelationalResult) var.getValue().realizeInMemory();
                    List<Map<String, Object>> rowValueMaps = realizedRelationalResult.getRowValueMaps(false);
                    executionState.addResult(var.getKey(), new ConstantResult(rowValueMaps.stream().flatMap(map -> map.values().stream()).collect(Collectors.toList())));
                }
            }
        }

        if (sqlQuery == null)
        {
            throw new RuntimeException("Relational execution not supported on external server");
        }

        try
        {
            sqlQuery = FreeMarkerExecutor.process(sqlQuery, executionState, databaseTypeName, databaseTimeZone);
            Span span = GlobalTracer.get().activeSpan();
            if (span != null)
            {
                span.setTag("generatedSQL", sqlQuery);
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Reprocessing sql failed with vars " + executionState.getResults().keySet(), e);
        }

        executionState.activities.add(new RelationalExecutionActivity(sqlQuery));
    }

    //TODO: REMOVE THIS as it was copied from org.finos.legend.engine.plan.execution.stores.relational.RelationalExecutor
    private void prepareTempTable(Connection connectionManagerConnection, RelationalExecutionConfiguration relationalExecutionConfiguration, StreamingResult res, String tempTableName, String databaseTypeName, String databaseTimeZone, List<String> tempTableList)
    {
        DatabaseManager databaseManager = DatabaseManager.fromString(databaseTypeName);
        try (Scope ignored = GlobalTracer.get().buildSpan("create temp table").withTag("tempTableName", tempTableName).withTag("databaseType", databaseTypeName).startActive(true))
        {
            databaseManager.relationalDatabaseSupport().accept(RelationalDatabaseCommandsVisitorBuilder.getStreamResultToTempTableVisitor(relationalExecutionConfiguration, connectionManagerConnection, res, tempTableName, databaseTimeZone));
        }
        catch (Exception e)
        {
            try
            {
                if (!tempTableList.isEmpty())
                {
                    try (Statement statement = connectionManagerConnection.createStatement())
                    {
                        tempTableList.forEach((Consumer<? super String>) table ->
                        {
                            try
                            {
                                statement.execute(databaseManager.relationalDatabaseSupport().dropTempTable(table));
                            }
                            catch (Exception ignored)
                            {
                            }
                        });
                    }
                }
                connectionManagerConnection.close();
                throw new RuntimeException(e);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(e);
            }
        }
    }
}