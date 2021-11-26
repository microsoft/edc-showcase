package org.eclipse.dataspaceconnector.dataseeding.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.catalog.spi.NodeQueryAdapter;
import org.eclipse.dataspaceconnector.catalog.spi.NodeQueryAdapterRegistry;
import org.eclipse.dataspaceconnector.catalog.spi.model.UpdateResponse;
import org.eclipse.dataspaceconnector.dataloading.AssetLoader;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.asset.AssetIndex;
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression;
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.policy.PolicyRegistry;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.eclipse.dataspaceconnector.common.types.Cast.cast;

public class CatalogDataseedingExtension implements ServiceExtension {
    public static final String USE_EU_POLICY = "use-eu";
    public static final String USE_US_POLICY = "use-us";
    private AssetLoader assetIndexLoader;
    private ContractDefinitionStore contractDefinitionStore;

    @Override
    public Set<String> requires() {
        return Set.of(PolicyRegistry.FEATURE,
                FederatedCacheNodeDirectory.FEATURE,
                AssetIndex.FEATURE,
                ContractDefinitionStore.FEATURE,
                NodeQueryAdapterRegistry.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        assetIndexLoader = context.getService(AssetLoader.class);
        contractDefinitionStore = context.getService(ContractDefinitionStore.class);

//        savePolicies(context);
        setupContractOffers();
        saveAssets(context.getConnectorId());
        saveNodeEntries(context);
        createProtocolAdapter(context);

        monitor.info("Catalog Data seeding done");
    }

    public void setupContractOffers() {
        Policy publicPolicy = Policy.Builder.newInstance()
                .permission(Permission.Builder.newInstance()
                        .target("1")
                        .action(Action.Builder.newInstance()
                                .type("USE")
                                .build())
                        .build())
                .build();

        Policy publicPolicy2 = Policy.Builder.newInstance()
                .permission(Permission.Builder.newInstance()
                        .target("2")
                        .action(Action.Builder.newInstance()
                                .type("USE")
                                .build())
                        .build())
                .build();

        ContractDefinition contractDefinition1 = ContractDefinition.Builder.newInstance()
                .id("1")
                .accessPolicy(publicPolicy)
                .contractPolicy(publicPolicy)
                .selectorExpression(AssetSelectorExpression.Builder.newInstance().whenEquals("id", "1").build())
                .build();

        ContractDefinition contractDefinition2 = ContractDefinition.Builder.newInstance()
                .id("2")
                .accessPolicy(publicPolicy2)
                .contractPolicy(publicPolicy2)
                .selectorExpression(AssetSelectorExpression.Builder.newInstance().whenEquals("id", "2").build())
                .build();

        contractDefinitionStore.save(contractDefinition1);
        contractDefinitionStore.save(contractDefinition2);
    }

    private void createProtocolAdapter(ServiceExtensionContext context) {
        var dispatcherRegistry = context.getService(RemoteMessageDispatcherRegistry.class);
        var protocolAdapterRegistry = context.getService(NodeQueryAdapterRegistry.class);
        NodeQueryAdapter idsQueryAdapter = updateRequest -> {
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

        var asset1 = Asset.Builder.newInstance()
                .property("type", "file")
                .name("test-document")
                .id("test-document_" + connectorId)
                .contentType("text/plain")
                .property(Asset.PROPERTY_POLICY_ID, USE_US_POLICY)
                .version("1.0")
                .build();

        var dataAddress1 = DataAddress.Builder.newInstance()
                .type("file")
                .property("path", "/home/paul/Documents/")
                .property("filename", "test-document.txt")
                .build();


        var asset2 = Asset.Builder.newInstance()
                .property("type", "AzureStorage")
                .name("test-document-az")
                .id("test-document-az_" + connectorId)
                .contentType("text/plain")
                .property(Asset.PROPERTY_POLICY_ID, USE_EU_POLICY)
                .version("1.0")
                .build();

        var dataAddress2 = DataAddress.Builder.newInstance()
                .type("AzureStorage")
                .property("account", "edcshowcasegpstorage")
                .property("container", "src-container")
                .property("blobname", "test-document.txt")
                .build();

        var asset3 = Asset.Builder.newInstance()
                .property("type", "AzureStorage")
                .name("schematic_drawing-az")
                .id("schematic-drawing-az_" + connectorId)
                .contentType("image/png")
                .property(Asset.PROPERTY_POLICY_ID, USE_EU_POLICY)
                .version("1.0")
                .build();

        var dataAddress3 = DataAddress.Builder.newInstance()
                .type("AzureStorage")
                .property("account", "edcshowcasegpstorage")
                .property("container", "src-container")
                .property("blobname", "complex_schematic_drawing")
                .build();

        var asset4 = Asset.Builder.newInstance()
                .property("type", "http")
                .name("demo-todos")
                .id("demo-todos_" + connectorId)
                .property(Asset.PROPERTY_POLICY_ID, USE_EU_POLICY)
                .version("1.0")
                .build();

        var dataAddress4 = DataAddress.Builder.newInstance()
                .type("http")
                .property("targetUrl", "https://jsonplaceholder.typicode.com/todos/1")
                .build();

        var asset5 = Asset.Builder.newInstance()
                .property("type", "http")
                .name("demo-train-data")
                .id("demo-train-data_" + connectorId)
                .property(Asset.PROPERTY_POLICY_ID, USE_EU_POLICY)
                .version("1.0")
                .build();

        var dataAddress5 = DataAddress.Builder.newInstance()
                .type("http")
                .property("targetUrl", "https://jsonplaceholder.typicode.com/todos/2")
                .build();

        try {
            assetIndexLoader.accept(asset1, dataAddress1);
            assetIndexLoader.accept(asset2, dataAddress2);
            assetIndexLoader.accept(asset3, dataAddress3);
            assetIndexLoader.accept(asset4, dataAddress4);
            assetIndexLoader.accept(asset5, dataAddress5);
        } catch (EdcException ex) {
            ex.printStackTrace();
        }
    }
}


