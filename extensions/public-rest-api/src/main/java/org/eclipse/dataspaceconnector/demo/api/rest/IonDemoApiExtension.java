package org.eclipse.dataspaceconnector.demo.api.rest;

import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.protocol.web.WebService;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;

import java.util.Set;


public class IonDemoApiExtension implements ServiceExtension {

    @Override
    public Set<String> requires() {
        return Set.of("dataspaceconnector:transferprocessstore", "dataspaceconnector:dispatcher");
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var webService = context.getService(WebService.class);
        var monitor = context.getMonitor();

        // get all required services
        var transferProcessManager = context.getService(TransferProcessManager.class);
        var processStore = context.getService(TransferProcessStore.class);

        var dispatcherRegistry = context.getService(RemoteMessageDispatcherRegistry.class);
        var controller = new IonDemoApiController(context.getConnectorId(), monitor, transferProcessManager, processStore, dispatcherRegistry);
        webService.registerController(controller);

        monitor.info("Initialized REST API Extension");
    }

}
