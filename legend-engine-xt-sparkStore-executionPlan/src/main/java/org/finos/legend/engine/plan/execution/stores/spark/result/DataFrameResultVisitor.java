package org.finos.legend.engine.plan.execution.stores.spark.result;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.finos.legend.engine.plan.execution.result.*;
import org.finos.legend.engine.plan.execution.result.json.JsonStreamingResult;
import org.finos.legend.engine.plan.execution.result.object.StreamingObjectResult;

public class DataFrameResultVisitor implements ResultVisitor<Dataset<Row>> {

    public Dataset<Row> visit(DataFrameResult dataFrameResult) {
        return dataFrameResult.getDataFrame();
    }

    @Override
    public Dataset<Row> visit(ErrorResult errorResult) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public Dataset<Row> visit(StreamingObjectResult streamingObjectResult) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public Dataset<Row> visit(JsonStreamingResult jsonStreamingResult) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public Dataset<Row> visit(ConstantResult constantResult) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public Dataset<Row> visit(MultiResult multiResult) {
        throw new RuntimeException("Not implemented!");
    }
}