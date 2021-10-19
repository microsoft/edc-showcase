package org.eclipse.dataspaceconnector.dataseeding.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.catalog.spi.ProtocolAdapter;
import org.eclipse.dataspaceconnector.catalog.spi.ProtocolAdapterRegistry;
import org.eclipse.dataspaceconnector.catalog.spi.model.UpdateRequest;
import org.eclipse.dataspaceconnector.catalog.spi.model.UpdateResponse;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.metadata.MetadataStore;
import org.eclipse.dataspaceconnector.spi.policy.PolicyRegistry;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.GenericDataCatalogEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.eclipse.dataspaceconnector.common.types.Cast.cast;
import static org.eclipse.dataspaceconnector.policy.model.Operator.IN;

public class CatalogDataseedingExtension implements ServiceExtension {
    public static final String USE_EU_POLICY = "use-eu";
    public static final String USE_US_POLICY = "use-us";

    @Override
    public Set<String> requires() {
        return Set.of(MetadataStore.FEATURE, PolicyRegistry.FEATURE, FederatedCacheNodeDirectory.FEATURE, ProtocolAdapterRegistry.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        savePolicies(context);
        saveAssets(context);
        saveNodeEntries(context);
        createProtocolAdapter(context);

        monitor.info("Catalog Data seeding done");
    }

    private void createProtocolAdapter(ServiceExtensionContext context) {
        var dispatcherRegistry = context.getService(RemoteMessageDispatcherRegistry.class);
        var protocolAdapterRegistry = context.getService(ProtocolAdapterRegistry.class);
        var idsQueryAdapter = new ProtocolAdapter() {
            @Override
            public CompletableFuture<UpdateResponse> sendRequest(UpdateRequest updateRequest) {
                var query = QueryRequest.Builder.newInstance()
                        .connectorAddress(updateRequest.getNodeUrl())
                        .connectorId(context.getConnectorId())
                        .queryLanguage("edc")
                        .query("select *")
                        .protocol("ids-rest").build();

                CompletableFuture<List<String>> future = cast(dispatcherRegistry.send(List.class, query, () -> null));

                return future.thenApply(assetNames -> new UpdateResponse(updateRequest.getNodeUrl(), assetNames));
            }
        };

        protocolAdapterRegistry.register("ids-rest", idsQueryAdapter);

    }

    private void saveNodeEntries(ServiceExtensionContext context) {
        var nodeDirectory = context.getService(FederatedCacheNodeDirectory.class);

        var nodes = readNodesFromJson("nodes.json");
        nodes.forEach(nodeDirectory::insert);
    }

    private List<FederatedCacheNode> readNodesFromJson(String resourceName) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            var tr = new TypeReference<List<FederatedCacheNode>>() {
            };
            return mapper.readValue(in, tr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveAssets(ServiceExtensionContext context) {
        //todo this will change once the AssetIndex has replaced the MetadataStore
        var metadataStore = context.getService(MetadataStore.class);


        GenericDataCatalogEntry file1 = GenericDataCatalogEntry.Builder.newInstance()
                .property("type", "File")
                .property("path", "/home/paul/Documents/")
                .property("filename", "test-document.txt")
                .build();

        GenericDataCatalogEntry file2 = GenericDataCatalogEntry.Builder.newInstance()
                .property("type", "AzureStorage")
                .property("account", "iondemogpstorage")
                .property("container", "src-container")
                .property("blobname", "test-document.txt")
                .build();

        GenericDataCatalogEntry file3 = GenericDataCatalogEntry.Builder.newInstance()
                .property("type", "AzureStorage")
                .property("account", "iondemogpstorage")
                .property("container", "src-container")
                .property("blobname", "complex_schematic_drawing.png")
                .build();

        DataEntry entry1 = DataEntry.Builder.newInstance().id("test-document1").policyId(USE_US_POLICY).catalogEntry(file1).build();
        DataEntry entry2 = DataEntry.Builder.newInstance().id("test-document2").policyId(USE_EU_POLICY).catalogEntry(file2).build();
        DataEntry entry3 = DataEntry.Builder.newInstance().id("schematic-drawing").policyId(USE_EU_POLICY).catalogEntry(file3).build();

        metadataStore.save(entry1);
        metadataStore.save(entry2);
        metadataStore.save(entry3);
    }

    private void savePolicies(ServiceExtensionContext context) {
        PolicyRegistry policyRegistry = context.getService(PolicyRegistry.class);

        LiteralExpression spatialExpression = new LiteralExpression("ids:absoluteSpatialPosition");
        var euConstraint = AtomicConstraint.Builder.newInstance().leftExpression(spatialExpression).operator(IN).rightExpression(new LiteralExpression("eu")).build();
        var euUsePermission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("idsc:USE").build()).constraint(euConstraint).build();
        var euPolicy = Policy.Builder.newInstance().id(USE_EU_POLICY).permission(euUsePermission).build();

        var usConstraint = AtomicConstraint.Builder.newInstance().leftExpression(spatialExpression).operator(IN).rightExpression(new LiteralExpression("us")).build();
        var usUsePermission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("idsc:USE").build()).constraint(usConstraint).build();
        var usPolicy = Policy.Builder.newInstance().id(USE_US_POLICY).permission(usUsePermission).build();

        policyRegistry.registerPolicy(usPolicy);
        policyRegistry.registerPolicy(euPolicy);
    }
}


