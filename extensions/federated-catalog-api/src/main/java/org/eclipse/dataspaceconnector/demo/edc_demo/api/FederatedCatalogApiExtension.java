package org.eclipse.dataspaceconnector.demo.edc_demo.api;

import org.eclipse.dataspaceconnector.catalog.spi.QueryEngine;
import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;


public class FederatedCatalogApiExtension implements ServiceExtension {

    @Inject
    private WebService webService;
    @Inject
    private QueryEngine catalogQueryEngine;


    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        var controller = new FederatedCatalogApiController(monitor, catalogQueryEngine);
        webService.registerResource("data", controller);

    }

    @Override
    public String name() {
        return "Federated Catalog API Controller (EDC Showcase)";
    }
}
