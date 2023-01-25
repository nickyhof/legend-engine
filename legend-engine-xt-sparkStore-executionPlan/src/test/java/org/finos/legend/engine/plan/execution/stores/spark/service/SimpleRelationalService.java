package org.finos.legend.engine.plan.execution.stores.spark.service;

import org.eclipse.collections.api.factory.Lists;
import org.finos.legend.engine.language.pure.dsl.service.execution.AbstractServicePlanExecutor;
import org.finos.legend.engine.language.pure.dsl.service.execution.ServiceVariable;
import org.finos.legend.engine.plan.execution.result.Result;
import org.finos.legend.engine.plan.execution.stores.StoreExecutorConfiguration;
import org.finos.legend.engine.shared.core.url.StreamProvider;

import java.util.List;

public class SimpleRelationalService extends AbstractServicePlanExecutor {

    public SimpleRelationalService() {
        super("service::RelationalService", "SimpleRelationalService.json", false);
    }

    public SimpleRelationalService(StoreExecutorConfiguration... storeExecutorConfigurations) {
        super("service::RelationalService", "SimpleRelationalService.json", storeExecutorConfigurations);
    }

    public Result execute() {
        return this.execute((StreamProvider)null);
    }

    public Result execute(StreamProvider streamProvider) {
        return this.newExecutionBuilder(0).withStreamProvider(streamProvider).execute();
    }

    public final List<ServiceVariable> getServiceVariables() {
        return Lists.mutable.empty();
    }
}