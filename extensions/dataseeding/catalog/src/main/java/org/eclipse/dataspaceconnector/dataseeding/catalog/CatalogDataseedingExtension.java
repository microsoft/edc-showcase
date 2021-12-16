package org.eclipse.dataspaceconnector.dataseeding.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.catalog.cache.query.IdsMultipartNodeQueryAdapter;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.catalog.spi.NodeQueryAdapter;
import org.eclipse.dataspaceconnector.catalog.spi.NodeQueryAdapterRegistry;
import org.eclipse.dataspaceconnector.dataloading.AssetLoader;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
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
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.eclipse.dataspaceconnector.policy.model.Operator.IN;

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

        savePolicies(context);
        var assets = saveAssets(context.getConnectorId());
        offerAssets(assets);
        saveNodeEntries(context);
        createProtocolAdapter(context);

        monitor.info("Catalog Data seeding done");
    }

    public void offerAssets(List<Asset> assets) {
        Policy publicPolicy = Policy.Builder.newInstance()
                .permission(Permission.Builder.newInstance()
                        .target("1")
                        .action(Action.Builder.newInstance()
                                .type("USE")
                                .build())
                        .build())
                .build();

        assets.stream().map(a -> ContractDefinition.Builder.newInstance()
                        .id(a.getId())
                        .accessPolicy(publicPolicy)
                        .contractPolicy(publicPolicy)
                        .selectorExpression(AssetSelectorExpression.Builder.newInstance().whenEquals(Asset.PROPERTY_ID, getId(a)).build())
                        .build())
                .forEach(contractDefinitionStore::save);


    }

    private String getId(Asset a) {
        return "'" + a.getId() + "'";
    }

    private void createProtocolAdapter(ServiceExtensionContext context) {
        var dispatcherRegistry = context.getService(RemoteMessageDispatcherRegistry.class);
        var protocolAdapterRegistry = context.getService(NodeQueryAdapterRegistry.class);
        NodeQueryAdapter idsQueryAdapter = new IdsMultipartNodeQueryAdapter(context.getConnectorId(), dispatcherRegistry, context.getTypeManager());

        protocolAdapterRegistry.register("ids-multipart", idsQueryAdapter);

    }

    private void saveNodeEntries(ServiceExtensionContext context) {
        var nodeDirectory = context.getService(FederatedCacheNodeDirectory.class);

        var nodes = readNodesFromJson("nodes-local.json");
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

    private List<Asset> saveAssets(String connectorId) {

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
            return List.of(asset1, asset2, asset3, asset4, asset5);
        } catch (EdcException ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
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


