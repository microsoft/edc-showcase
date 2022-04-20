package org.eclipse.dataspaceconnector.demo.edc_demo.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.dataspaceconnector.catalog.spi.QueryEngine;
import org.eclipse.dataspaceconnector.catalog.spi.QueryResponse;
import org.eclipse.dataspaceconnector.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.util.Collections;

@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Path("/catalog")
public class FederatedCatalogApiController {
    private final Monitor monitor;
    private final QueryEngine catalogQueryEngine;

    public FederatedCatalogApiController(Monitor monitor, QueryEngine catalogQueryEngine) {
        this.monitor = monitor;
        this.catalogQueryEngine = catalogQueryEngine;
    }

    @GET
    @Path("health")
    public Response getHealth() {
        monitor.info("GET /edc-demo/health - getHealth()");
        var result = Collections.singletonMap("status", "up and running");
        return Response.ok(result).build();
    }

    @GET
    @Path("contract-offers")
    public Response getContractOffers() {
        monitor.info("GET /edc-demo/contract-offers - getContractOffers()");

        FederatedCatalogCacheQuery query = FederatedCatalogCacheQuery
                .Builder
                .newInstance()
                .build();

        var queryResponse = catalogQueryEngine.getCatalog(query);
        if (queryResponse.getStatus() == QueryResponse.Status.NO_ADAPTER_FOUND) {
            return Response.status(Response.Status.NOT_IMPLEMENTED).build();
        }

        if (!queryResponse.getErrors().isEmpty()) {
            return Response.status(400, String.join(", ", queryResponse.getErrors())).build();
        }

        var result = queryResponse.getOffers();

        return Response.ok(result).build();
    }
}

