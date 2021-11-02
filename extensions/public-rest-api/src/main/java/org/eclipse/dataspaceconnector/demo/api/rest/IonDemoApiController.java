package org.eclipse.dataspaceconnector.demo.api.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.dataspaceconnector.catalog.spi.QueryEngine;
import org.eclipse.dataspaceconnector.catalog.spi.QueryResponse;
import org.eclipse.dataspaceconnector.catalog.spi.model.CacheQuery;
import org.eclipse.dataspaceconnector.common.collection.CollectionUtil;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.eclipse.dataspaceconnector.common.types.Cast.cast;

@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Path("/")
public class IonDemoApiController {
    private final Monitor monitor;
    private final TransferProcessManager transferProcessManager;
    private final TransferProcessStore processStore;
    private final String connectorName;
    private final QueryEngine catalogQueryEngine;
    private final RemoteMessageDispatcherRegistry dispatcherRegistry;


    public IonDemoApiController(String connectorName, Monitor monitor, TransferProcessManager transferProcessManager, TransferProcessStore processStore, QueryEngine catalogQueryEngine, RemoteMessageDispatcherRegistry dispatcherRegistry) {
        this.connectorName = connectorName;
        this.monitor = monitor;
        this.transferProcessManager = transferProcessManager;
        this.processStore = processStore;
        this.catalogQueryEngine = catalogQueryEngine;
        this.dispatcherRegistry = dispatcherRegistry;
    }

    @GET
    @Path("health")
    public Response hello() {
        monitor.info("Controller says hello!");
        HashMap<String, String> m = formatAsJson("up and running");
        return Response.ok(m).build();
    }


    @Deprecated
    @GET
    @Path("catalog")
    public Response getCatalog(@QueryParam("connectorAddress") String connectorAddress) throws ExecutionException, InterruptedException {
        monitor.info("catalog requested");
        var query = QueryRequest.Builder.newInstance()
                .connectorAddress(connectorAddress)
                .connectorId(connectorName)
                .queryLanguage("dataspaceconnector")
                .query("select *")
                .protocol("ids-rest").build();

        CompletableFuture<List<String>> future = cast(dispatcherRegistry.send(List.class, query, () -> null));

        return Response.ok(future.get()).build();
    }

    @GET
    @Path("catalog/cached")
    public Response getCatalogCached() {
        CacheQuery query = CacheQuery.Builder.newInstance()
                .build();

        var queryResponse = catalogQueryEngine.getCatalog(query);
        if (queryResponse.getStatus() == QueryResponse.Status.NO_ADAPTER_FOUND) {
            return Response.status(Response.Status.NOT_IMPLEMENTED).build();
        }
        if (!queryResponse.getErrors().isEmpty()) {
            return Response.status(400, String.join(", ", queryResponse.getErrors())).build();
        }

        return Response.ok(queryResponse.getAssets().collect(Collectors.toList())).build();
    }

    @POST
    @Path("datarequest")
    public Response initiateDataRequest(DataRequest request) {
        if (request == null) {
            return Response.status(400).entity("data request cannot be null").build();
        }
        request = request.copy(UUID.randomUUID().toString()); //assign random ID
        monitor.info("Received new data request, ID = " + request.getId());
        var response = transferProcessManager.initiateConsumerRequest(request);
        monitor.info("Created new transfer process, ID = " + response.getId());

        ResponseStatus status = response.getStatus();
        if (status == ResponseStatus.OK) {
            return Response.ok(formatAsJson(response.getId())).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(response.getStatus().name()).build();
        }
    }

    @GET
    @Path("datarequest/{id}")
    public Response getDatarequest(@PathParam("id") String requestId) {
        monitor.info("getting status of data request " + requestId);

        var process = processStore.find(requestId);
        if (process == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(process).build();
    }

    @DELETE
    @Path("datarequest/{id}")
    public Response deprovisionRequest(@PathParam("id") String requestId) {

        var process = processStore.find(requestId);
        if (process == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            if (CollectionUtil.isAnyOf(process.getState(),
                    TransferProcessStates.DEPROVISIONED.code(),
                    TransferProcessStates.DEPROVISIONING_REQ.code(),
                    TransferProcessStates.DEPROVISIONING.code(),
                    TransferProcessStates.ENDED.code()
            )) {
                monitor.info("Request already deprovisioning or deprovisioned.");
            } else {
                monitor.info("starting to deprovision data request " + requestId);
                process.transitionCompleted();
                process.transitionDeprovisionRequested();
                processStore.update(process);
            }
            return Response.ok(formatAsJson(TransferProcessStates.from(process.getState()).toString())).build();
        } catch (IllegalStateException ex) {
            monitor.severe(ex.getMessage());
            return Response.status(400).entity("The process must be in one of these states: " + String.join(", ", TransferProcessStates.IN_PROGRESS.name(), TransferProcessStates.REQUESTED_ACK.name(), TransferProcessStates.STREAMING.name())).build();
        }

    }

    @GET
    @Path("datarequest/{id}/state")
    public Response getStatus(@PathParam("id") String requestId) {
        monitor.info("getting status of data request " + requestId);

        var process = processStore.find(requestId);
        if (process == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(formatAsJson(TransferProcessStates.from(process.getState()).toString())).build();
    }

    @NotNull
    private HashMap<String, String> formatAsJson(String simpleValue) {
        var m = new HashMap<String, String>();
        m.put("response", simpleValue);
        return m;
    }
}
