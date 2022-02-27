package org.eclipse.dataspaceconnector.demo.api.rest;

import org.eclipse.dataspaceconnector.catalog.spi.QueryEngine;
import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;


public class DemoApiExtension implements ServiceExtension {

    @Inject
    private TransferProcessManager transferProcessManager;
    @Inject
    private WebService webService;
    @Inject
    private ConsumerContractNegotiationManager consumerNegotiationManager;
    @Inject
    private RemoteMessageDispatcherRegistry dispatcherRegistry;
    @Inject
    private QueryEngine catalogQueryEngine;
    @Inject
    private TransferProcessStore processStore;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        consumerNegotiationManager = context.getService(ConsumerContractNegotiationManager.class);
        var controller = new DemoApiController(context.getConnectorId(), monitor, transferProcessManager, processStore, catalogQueryEngine, dispatcherRegistry, consumerNegotiationManager);
        webService.registerResource(controller);

        monitor.info("Initialized Demo API Extension");
    }

}
