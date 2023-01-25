package org.finos.legend.engine.plan.execution.stores.spark.result;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.finos.legend.engine.plan.execution.result.ExecutionActivity;
import org.finos.legend.engine.plan.execution.result.Result;
import org.finos.legend.engine.plan.execution.result.ResultVisitor;
import org.finos.legend.engine.plan.execution.stores.relational.activity.RelationalExecutionActivity;
import org.finos.legend.engine.plan.execution.stores.spark.version.SparkVersion;

import java.util.List;

public class DataFrameResult extends Result {

    private final Dataset<Row> dataFrame;

    public DataFrameResult(List<ExecutionActivity> activities, String url) {
        super("success", activities);

        final SparkSession spark = SparkSession.builder().getOrCreate();

        String sql = ((RelationalExecutionActivity) activities.get(activities.size() - 1)).sql;

        // In spark versions before 3.3.0, the connectionProvider option was not available, therefore we need to disable the default basic provider
        if (shouldDisableBasicConnectionProvider(spark)) {
            spark.conf().set("spark.sql.sources.disabledJdbcConnProviderList", "basic");
        }

        dataFrame = spark
                .read()
                .format("jdbc")
                .option("url", url)
                .option("query", sql)
                .option("connectionProvider", "legend")
                .load();
    }

    @Override
    public <T> T accept(ResultVisitor<T> resultVisitor) {
        return (T) ((DataFrameResultVisitor) resultVisitor).visit(this);
    }

    public Dataset<Row> getDataFrame() {
        return dataFrame;
    }

    private boolean shouldDisableBasicConnectionProvider(SparkSession spark) {
        SparkVersion currentVersion = new SparkVersion(spark.version());
        SparkVersion minSupportedVersion = new SparkVersion("3.3.0");

        return currentVersion.compareTo(minSupportedVersion) < 0;
    }
}