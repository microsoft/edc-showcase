package org.eclipse.dataspaceconnector.transfer.httproxy;

import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.spi.asset.DataAddressResolver;
import org.eclipse.dataspaceconnector.spi.protocol.web.WebService;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.synchronous.DataProxyManager;
import org.eclipse.dataspaceconnector.spi.transfer.synchronous.ProxyEntryHandlerRegistry;

import java.util.concurrent.CopyOnWriteArrayList;

public class TransferProxyHttpExtension implements ServiceExtension {

    @Inject
    private WebService webService;
    @Inject
    private DataAddressResolver dataAddressResolver;
    @Inject
    private DataProxyManager manager;
    @Inject
    private ProxyEntryHandlerRegistry registry;

    @Override
    public void initialize(ServiceExtensionContext context) {

        var issuedTokens = new CopyOnWriteArrayList<String>();

        var controller = new ForwardingController(context.getMonitor(), dataAddressResolver, context.getService(Vault.class), context.getService(OkHttpClient.class), issuedTokens);
        webService.registerController(controller);
        manager.addProxy(RestDataProxy.DESTINATION_TYPE_HTTP, new RestDataProxy(controller.getRootPath(), context.getConnectorId(), issuedTokens));
        registry.put(RestDataProxy.DESTINATION_TYPE_HTTP, new RestProxyEntryHandler(context.getMonitor(), context.getService(OkHttpClient.class)));

    }
}
