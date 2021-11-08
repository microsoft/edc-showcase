package org.eclipse.dataspaceconnector.transfer;

import org.eclipse.dataspaceconnector.spi.asset.DataAddressResolver;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowManager;

import java.util.Set;

public class CloudTransferExtension implements ServiceExtension {

    @Override
    public Set<String> requires() {
        return Set.of(DataAddressResolver.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var dataFlowMgr = context.getService(DataFlowManager.class);
        var dataAddressResolver = context.getService(DataAddressResolver.class);
        var flowController = new BlobToS3DataFlowController(context.getService(Vault.class), context.getMonitor(), context.getTypeManager(), dataAddressResolver);
        dataFlowMgr.register(flowController);

        context.getMonitor().info("Initialized transfer extension");
    }
}
