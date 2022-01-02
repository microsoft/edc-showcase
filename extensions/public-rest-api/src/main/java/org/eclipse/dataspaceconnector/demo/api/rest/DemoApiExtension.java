package org.eclipse.dataspaceconnector.demo.api.rest;

import org.eclipse.dataspaceconnector.catalog.spi.QueryEngine;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ContractNegotiationManager;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.protocol.web.WebService;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;

import java.util.Set;


public class DemoApiExtension implements ServiceExtension {

    @Override
    public Set<String> requires() {
        return Set.of("dataspaceconnector:transferprocessstore", "dataspaceconnector:dispatcher", QueryEngine.FEATURE, ContractNegotiationManager.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var webService = context.getService(WebService.class);
        var monitor = context.getMonitor();

        // get all required services
        var transferProcessManager = context.getService(TransferProcessManager.class);
        var processStore = context.getService(TransferProcessStore.class);

        var catalogQueryEngine = context.getService(QueryEngine.class);
        var dispatcherRegistry = context.getService(RemoteMessageDispatcherRegistry.class);

        var consumerNegotiationManager = context.getService(ConsumerContractNegotiationManager.class);

        var controller = new DemoApiController(context.getConnectorId(), monitor, transferProcessManager, processStore, catalogQueryEngine, dispatcherRegistry, consumerNegotiationManager);
        webService.registerController(controller);

        monitor.info("Initialized Demo API Extension");
    }

}
