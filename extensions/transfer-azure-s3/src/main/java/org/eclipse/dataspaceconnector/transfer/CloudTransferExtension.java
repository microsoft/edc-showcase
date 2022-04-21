package org.eclipse.dataspaceconnector.transfer;

import net.jodah.failsafe.RetryPolicy;
import org.eclipse.dataspaceconnector.aws.s3.core.S3ClientProvider;
import org.eclipse.dataspaceconnector.aws.s3.operator.S3BucketReader;
import org.eclipse.dataspaceconnector.aws.s3.operator.S3BucketWriter;
import org.eclipse.dataspaceconnector.azure.blob.core.api.BlobStoreApi;
import org.eclipse.dataspaceconnector.azure.blob.operator.BlobStoreReader;
import org.eclipse.dataspaceconnector.azure.blob.operator.BlobStoreWriter;
import org.eclipse.dataspaceconnector.spi.asset.DataAddressResolver;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowManager;
import org.eclipse.dataspaceconnector.spi.transfer.inline.DataOperatorRegistry;
import org.eclipse.dataspaceconnector.transfer.core.inline.InlineDataFlowController;

public class CloudTransferExtension implements ServiceExtension {


    @Inject
    private DataFlowManager dataFlowMgr;
    @Inject
    private DataOperatorRegistry registry;
    @Inject
    private RetryPolicy<Object> retryPolicy;
    @Inject
    private BlobStoreApi blobStoreApi;
    @Inject
    private S3ClientProvider clientProvider;

    @Override
    public void initialize(ServiceExtensionContext context) {
        Vault vault = context.getService(Vault.class);

        registry.registerWriter(new S3BucketWriter(context.getMonitor(), context.getTypeManager(), retryPolicy, clientProvider));
        registry.registerWriter(new BlobStoreWriter(context.getMonitor(), context.getTypeManager()));
        registry.registerReader(new BlobStoreReader(blobStoreApi));
        registry.registerReader(new S3BucketReader(context.getMonitor(), vault, clientProvider ));
        var flowController = new InlineDataFlowController(vault, context.getMonitor(), registry);
        dataFlowMgr.register(flowController);

        context.getMonitor().info("Initialized transfer extension");
    }
}
