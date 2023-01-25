package org.finos.legend.engine.plan.execution.stores.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.eclipse.collections.api.factory.Lists;
import org.finos.legend.engine.plan.execution.stores.spark.result.DataFrameResult;
import org.finos.legend.engine.plan.execution.stores.spark.service.SimpleRelationalService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SparkExecutorTest {

    private static SparkSession sparkSession;

    @BeforeClass
    public static void beforeClass() {
        sparkSession = SparkSession
                .builder()
                .master("local")
                .appName("Local Spark Testing")
                .getOrCreate();
    }

    @AfterClass
    public static void afterClass() {
        if (sparkSession != null) {
            sparkSession.stop();
        }
    }

    @Test
    public void testSimpleRelationalService() {
        SimpleRelationalService service = new SimpleRelationalService();

        DataFrameResult result = (DataFrameResult) service.execute();
        Dataset<Row> dataFrame = result.getDataFrame();

        Dataset<Row> expectedDataFrame = sparkSession.createDataFrame(
                Lists.mutable.of(RowFactory.create(1, "f1", "l1")),
                StructType.fromDDL("age INT, first_name STRING, last_name STRING")
        );

        Assert.assertEquals(dataFrame.collectAsList(), expectedDataFrame.collectAsList());
    }
}