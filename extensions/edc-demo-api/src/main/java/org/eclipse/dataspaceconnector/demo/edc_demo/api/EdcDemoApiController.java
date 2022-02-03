package org.eclipse.dataspaceconnector.demo.edc_demo.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.dataspaceconnector.azure.blob.core.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.catalog.spi.QueryEngine;
import org.eclipse.dataspaceconnector.catalog.spi.QueryResponse;
import org.eclipse.dataspaceconnector.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.dataspaceconnector.dataloading.AssetEntry;
import org.eclipse.dataspaceconnector.dataloading.DataLoader;
import org.eclipse.dataspaceconnector.dataloading.DataSink;
import org.eclipse.dataspaceconnector.demo.edc_demo.api.dtos.StorageTypeDto;
import org.eclipse.dataspaceconnector.demo.edc_demo.api.dtos.TransferProcessCreationDto;
import org.eclipse.dataspaceconnector.demo.edc_demo.api.dtos.TransferProcessDto;
import org.eclipse.dataspaceconnector.ids.spi.Protocols;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Path("/edc-demo")
public class EdcDemoApiController {
    private static final List<StorageTypeDto> fakeStorageTypeDtos = Arrays.asList(
            new StorageTypeDto("AzureStorage", "Azure Storage"),
            new StorageTypeDto("AmazonS3", "AWS S3"));
    private final Monitor monitor;
    private final TransferProcessManager transferProcessManager;
    private final TransferProcessStore processStore;
    private final String connectorName;
    private final QueryEngine catalogQueryEngine;
    private final RemoteMessageDispatcherRegistry dispatcherRegistry;
    private final ContractDefinitionStore contractDefinitionStore;
    private final String connectorId;
    private final DataSink<AssetEntry> assetSink;

    public EdcDemoApiController(String connectorName, Monitor monitor, TransferProcessManager transferProcessManager, TransferProcessStore processStore, QueryEngine catalogQueryEngine, RemoteMessageDispatcherRegistry dispatcherRegistry, ContractDefinitionStore contractDefinitionStore, String connectorId, DataSink<AssetEntry> assetSink) {
        this.connectorName = connectorName; // is connector id
        this.monitor = monitor;
        this.transferProcessManager = transferProcessManager;
        this.processStore = processStore;
        this.catalogQueryEngine = catalogQueryEngine;
        this.dispatcherRegistry = dispatcherRegistry;
        this.contractDefinitionStore = contractDefinitionStore;
        this.connectorId = connectorId;
        this.assetSink = assetSink;
    }

    @GET
    @Path("health")
    public Response getHealth() {
        monitor.info("GET /edc-demo/health - getHealth()");
        var result = Collections.singletonMap("status", "up and running");
        return Response.ok(result).build();
    }

    @GET
    @Path("assets")
    public Response getAssets() {
        monitor.info("GET /edc-demo/assets - getAssets()");

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

        // todo: replace this, return ContractOffers instead
        var result = queryResponse.getOffers().stream().map(ContractOffer::getAsset).collect(Collectors.toList());

        return Response.ok(result).build();
    }

    @GET
    @Path("contract-definitions")
    public Response getContractDefinitions() {
        monitor.info("GET /edc-demo/contract-definitions - getContractDefinitions()");

        var contractDefinitions = this.contractDefinitionStore.findAll();

        return Response.ok().entity(contractDefinitions).build();
    }

    @POST
    @Path("contract-definitions")
    public Response createContractDefinition(ContractDefinition contractDefinition) {
        monitor.info("POST /edc-demo/contract-definitions - createContractDefinition()");

        if (contractDefinition == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("contract definition cannot be null").build();
        }

        this.contractDefinitionStore.save(contractDefinition);

        return Response.ok().entity(contractDefinition).build();
    }

    @PUT
    @Path("contract-definitions/{id}")
    public Response updateContractDefinition(@PathParam("id") String id, ContractDefinition contractDefinition) {
        monitor.info(String.format("PUT /edc-demo/contract-definitions/%s - updateContractDefinition()", id));

        if (contractDefinition == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("contract definition cannot be null").build();
        }

        this.contractDefinitionStore.update(contractDefinition);

        return Response.ok().entity(contractDefinition).build();
    }

    @GET
    @Path("policies")
    public Response getPolicies() {
        monitor.info("GET /policies - getPolicies()");

        var policies = new HashMap<String, Policy>();

        var contractDefinitions = this.contractDefinitionStore.findAll();

        for (var cd : contractDefinitions) {
            var accessPolicy = cd.getAccessPolicy();
            policies.put(accessPolicy.getUid(), accessPolicy);

            var contractPolicy = cd.getContractPolicy();
            policies.put(contractPolicy.getUid(), contractPolicy);
        }

        return Response.ok().entity(policies.values()).build();
    }

    @GET
    @Path("transfer-processes")
    public Response getTransferProcesses() {
        monitor.info("GET /edc-demo/transfer-processes - getTransferProcesses()");

        var transferProcesses = new ArrayList<TransferProcess>();

        Stream.of(TransferProcessStates.values()).forEach(state -> transferProcesses.addAll(processStore.nextForState(state.code(), 10)));

        var transferProcessDtos = transferProcesses
                .stream()
                .map(this::mapToTransferProcessDto)
                .sorted(Comparator.comparing(TransferProcessDto::getStateTimestamp).reversed())
                .collect(Collectors.toList());

        return Response.ok().entity(transferProcessDtos).build();
    }

    @GET
    @Path("transfer-processes/{id}")
    public Response getTransferProcessById(@PathParam("id") String id) {
        monitor.info(String.format("GET /edc-demo/transfer-processes/%s - getTransferProcessById()", id));

        var process = processStore.find(id);
        if (process == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapToTransferProcessDto(process)).build();
    }

    @POST
    @Path("transfer-processes")
    public Response createTransferProcess(TransferProcessCreationDto transferProcessCreation) {
        monitor.info("POST /edc-demo/transfer-processes - createTransferProcess()");

        if (transferProcessCreation == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("data request cannot be null").build();
        }

        var destinationDataAddress = DataAddress.Builder.newInstance().type(transferProcessCreation.getDataDestinationType())
                .property(AzureBlobStoreSchema.ACCOUNT_NAME, "edcshowcasegpstorage")
                .property(AzureBlobStoreSchema.CONTAINER_NAME, "dst-container")
                .property(AzureBlobStoreSchema.BLOB_NAME, UUID.randomUUID().toString())
                .build();

        var dataRequest = DataRequest.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .connectorId(this.connectorId)
                .protocol(Protocols.IDS_MULTIPART)
                .connectorAddress(transferProcessCreation.getConnectorAddress())
                .assetId(transferProcessCreation.getAssetId())
                .contractId(transferProcessCreation.getContractId())
                .dataDestination(destinationDataAddress)
                .managedResources(true)
                .build();

        var transferResponse = transferProcessManager.initiateConsumerRequest(dataRequest);

        if (transferResponse.failed()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(transferResponse.getFailureMessages()).build();

        }
        var tpId = Objects.requireNonNull(transferResponse.getData()).toString();
        var transferProcess = processStore.find(tpId);

        return Response.ok().entity(mapToTransferProcessDto(transferProcess)).build();

    }

    @GET
    @Path("storage-types")
    public Response getStorageTypes() {
        monitor.info("GET /edc-demo/storage-types - getStorageTypes()");
        return Response.ok().entity(fakeStorageTypeDtos).build();
    }

    @POST
    @Path("asset-entries")
    public Response createAssetEntry(AssetEntry assetEntry) {
        monitor.info("POST /edc-demo/asset-entries - createAssetEntry()");

        if (assetEntry == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("assetEntry must not be null").build();
        }

        DataLoader.Builder<AssetEntry> dataLoaderBuilder = DataLoader.Builder.newInstance();
        var dataLoader = dataLoaderBuilder.sink(assetSink).build();

        dataLoader.insert(assetEntry);

        return Response.ok().entity(assetEntry).build();
    }

    private TransferProcessDto mapToTransferProcessDto(TransferProcess transferProcess) {
        return new TransferProcessDto(
                transferProcess.getId(),
                transferProcess.getType().toString(),
                transferProcess.getState(),
                new Timestamp(transferProcess.getStateTimestamp()),
                transferProcess.getErrorDetail(),
                transferProcess.getDataRequest().getConnectorAddress(),
                transferProcess.getDataRequest().getProtocol(),
                transferProcess.getDataRequest().getConnectorId(),
                transferProcess.getDataRequest().getAssetId(),
                transferProcess.getDataRequest().getContractId(),
                transferProcess.getDataRequest().getDestinationType());
    }
}
