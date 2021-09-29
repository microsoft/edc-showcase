package org.eclipse.dataspaceconnector.transfer;

import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowManager;

public class CloudTransferExtension implements ServiceExtension {

    @Override
    public void initialize(ServiceExtensionContext context) {
        var dataFlowMgr = context.getService(DataFlowManager.class);
        var flowController = new BlobToS3DataFlowController(context.getService(Vault.class), context.getMonitor(), context.getTypeManager());
        dataFlowMgr.register(flowController);

        context.getMonitor().info("Initialized transfer extension");
    }
}
