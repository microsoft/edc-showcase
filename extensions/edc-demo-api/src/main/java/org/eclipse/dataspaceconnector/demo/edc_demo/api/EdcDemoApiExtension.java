package org.eclipse.dataspaceconnector.demo.edc_demo.api;

import org.eclipse.dataspaceconnector.catalog.spi.QueryEngine;
import org.eclipse.dataspaceconnector.dataloading.AssetLoader;
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.protocol.web.WebService;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;


public class EdcDemoApiExtension implements ServiceExtension {

    @Inject
    private WebService webService;
    @Inject
    private TransferProcessManager transferProcessManager;
    @Inject
    private TransferProcessStore processStore;
    @Inject
    private QueryEngine catalogQueryEngine;
    @Inject
    private RemoteMessageDispatcherRegistry dispatcherRegistry;
    @Inject
    private ContractDefinitionStore contractDefinitionStore;
    @Inject
    private AssetLoader assetSink;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        // get all required services
        var connectorId = context.getConnectorId();
//        var policyRegistry = context.getService(PolicyRegistry.class);

        var controller = new EdcDemoApiController(context.getConnectorId(), monitor, transferProcessManager, processStore, catalogQueryEngine, dispatcherRegistry, contractDefinitionStore, connectorId, assetSink); //, policyRegistry);
        webService.registerController(controller);

    }

    @Override
    public String name() {
        return "EDC Showcase REST API extension";
    }
}
