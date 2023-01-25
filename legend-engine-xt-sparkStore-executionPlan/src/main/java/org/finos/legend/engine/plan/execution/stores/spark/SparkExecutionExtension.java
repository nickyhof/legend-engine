package org.finos.legend.engine.plan.execution.stores.spark;

import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.list.MutableList;
import org.finos.legend.engine.plan.execution.extension.ExecutionExtension;
import org.finos.legend.engine.plan.execution.nodes.state.ExecutionState;
import org.finos.legend.engine.plan.execution.result.Result;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.*;
import org.pac4j.core.profile.CommonProfile;

import java.util.Collections;
import java.util.List;

public class SparkExecutionExtension implements ExecutionExtension {

    @Override
    public List<Function3<ExecutionNode, MutableList<CommonProfile>, ExecutionState, Result>> getExtraNodeExecutors() {
        return Collections.singletonList(((executionNode, profiles, executionState) -> {
            if (executionNode instanceof RelationalTdsInstantiationExecutionNode) {
                return executionNode.accept(new SparkExecutionNodeExecutor(profiles, executionState));
            }

            return null;
        }));
    }
}