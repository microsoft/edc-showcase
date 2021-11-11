package org.eclipse.dataspaceconnector.dataseeding.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.catalog.spi.CatalogQueryAdapter;
import org.eclipse.dataspaceconnector.catalog.spi.CatalogQueryAdapterRegistry;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.catalog.spi.model.UpdateResponse;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.asset.AssetIndex;
import org.eclipse.dataspaceconnector.spi.asset.AssetIndexLoader;
import org.eclipse.dataspaceconnector.spi.asset.DataAddressResolver;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.policy.PolicyRegistry;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.GenericDataCatalogEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.eclipse.dataspaceconnector.common.types.Cast.cast;
import static org.eclipse.dataspaceconnector.policy.model.Operator.IN;

public class CatalogDataseedingExtension implements ServiceExtension {
    public static final String USE_EU_POLICY = "use-eu";
    public static final String USE_US_POLICY = "use-us";
    private AssetIndexLoader assetIndexLoader;

    @Override
    public Set<String> requires() {
        return Set.of(PolicyRegistry.FEATURE,
                FederatedCacheNodeDirectory.FEATURE,
                DataAddressResolver.FEATURE,
                AssetIndex.FEATURE,
                CatalogQueryAdapterRegistry.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        assetIndexLoader = context.getService(AssetIndexLoader.class);

        savePolicies(context);
        saveAssets(context.getConnectorId());
        saveNodeEntries(context);
        createProtocolAdapter(context);

        monitor.info("Catalog Data seeding done");
    }

    private void createProtocolAdapter(ServiceExtensionContext context) {
        var dispatcherRegistry = context.getService(RemoteMessageDispatcherRegistry.class);
        var protocolAdapterRegistry = context.getService(CatalogQueryAdapterRegistry.class);
        CatalogQueryAdapter idsQueryAdapter = updateRequest -> {
            var query = QueryRequest.Builder.newInstance()
                    .connectorAddress(updateRequest.getNodeUrl())
                    .connectorId(context.getConnectorId())
                    .queryLanguage("edc")
                    .query("select *")
                    .protocol("ids-rest").build();

            CompletableFuture<List<Map<String, Object>>> future = cast(dispatcherRegistry.send(List.class, query, () -> null));

            return future.thenApply(assetNames -> {
                // This is a dirty hack which is necessary because "assetNames" will get deserialized to
                // a list of LinkedHashMaps, instead of Assets.
                // so we need to serialize and manually deserialize again...
                var tm = context.getTypeManager();
                var assets = assetNames.stream().map(asset -> tm.readValue(tm.writeValueAsString(asset), Asset.class))
                        .collect(Collectors.toList());

                return new UpdateResponse(updateRequest.getNodeUrl(), assets);
            });
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

    private void saveAssets(String connectorId) {
        GenericDataCatalogEntry file1 = GenericDataCatalogEntry.Builder.newInstance()
                .property("type", "File")
                .property("path", "/home/paul/Documents/")
                .property("filename", "test-document.txt")
                .property("targetUrl", "http://whatever.com")
                .build();

        GenericDataCatalogEntry file2 = GenericDataCatalogEntry.Builder.newInstance()
                .property("type", "AzureStorage")
                .property("account", "iondemogpstorage")
                .property("container", "src-container")
                .property("blobname", "test-document.txt")
                .property("targetUrl", "http://whatever.com")
                .build();

        GenericDataCatalogEntry file3 = GenericDataCatalogEntry.Builder.newInstance()
                .property("type", "AzureStorage")
                .property("account", "iondemogpstorage")
                .property("container", "src-container")
                .property("blobname", "complex_schematic_drawing.png")
                .property("targetUrl", "http://whatever.com")
                .build();

        GenericDataCatalogEntry file4 = GenericDataCatalogEntry.Builder.newInstance()
                .property("type", "http")
                .property("targetUrl", "https://jsonplaceholder.typicode.com/todos/1")
                .build();

        GenericDataCatalogEntry file5 = GenericDataCatalogEntry.Builder.newInstance()
                .property("type", "http")
                .property("targetUrl", "https://jsonplaceholder.typicode.com/todos/2")
                .build();


        DataEntry entry1 = DataEntry.Builder.newInstance().id("test-document1").policyId(USE_US_POLICY).catalogEntry(file1).build();
        DataEntry entry2 = DataEntry.Builder.newInstance().id("test-document2").policyId(USE_EU_POLICY).catalogEntry(file2).build();
        DataEntry entry3 = DataEntry.Builder.newInstance().id("schematic-drawing").policyId(USE_EU_POLICY).catalogEntry(file3).build();
        DataEntry entry4 = DataEntry.Builder.newInstance().id("demo-todo").policyId(USE_EU_POLICY).catalogEntry(file4).build();
        DataEntry entry5 = DataEntry.Builder.newInstance().id("trains").policyId(USE_EU_POLICY).catalogEntry(file5).build();

        assetIndexLoader.insert(toAsset(entry1, connectorId), toDataAddress(entry1));
        assetIndexLoader.insert(toAsset(entry2, connectorId), toDataAddress(entry2));
        assetIndexLoader.insert(toAsset(entry3, connectorId), toDataAddress(entry3));
        assetIndexLoader.insert(toAsset(entry4, connectorId), toDataAddress(entry4));
        assetIndexLoader.insert(toAsset(entry5, connectorId), toDataAddress(entry5));

    }


    private DataAddress toDataAddress(DataEntry entry) {
        return entry.getCatalogEntry().getAddress();
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

    private Asset toAsset(DataEntry entry, String idPostfix) {
        var builder = Asset.Builder.newInstance()
                .id(entry.getId() + "_" + idPostfix)
                .property("policyId", entry.getPolicyId());
        ((GenericDataCatalogEntry) entry.getCatalogEntry()).getProperties().forEach(builder::property);
        return builder.build();
    }
}


