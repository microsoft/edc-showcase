package org.eclipse.dataspaceconnector.dataseeding.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.dataloading.AssetEntry;
import org.eclipse.dataspaceconnector.dataloading.AssetLoader;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.EdcSetting;
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression;
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.store.PolicyStore;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CatalogDataseedingExtension implements ServiceExtension {
    @EdcSetting
    private static final String NODES_FILE_SETTING = "edc.showcase.fcc.nodes.file";
    @Inject
    private AssetLoader assetIndexLoader;
    @Inject
    private ContractDefinitionStore contractDefinitionStore;
    @Inject
    private FederatedCacheNodeDirectory nodeDirectory;
    @Inject
    private PolicyStore policyStore;

    @Override
    public void initialize(ServiceExtensionContext context) {
        Monitor monitor = context.getMonitor();

        var nodesFile = context.getSetting(NODES_FILE_SETTING, "nodes.json");
        monitor.info("Using FCC Node directory file " + nodesFile);

        //generate+save assets
        var assets = createAssets(context.getConnectorId());
        assets.forEach(assetIndexLoader::accept);

        // generate+save policies
        var accessPolicies = IntStream.range(0, 10).mapToObj(i -> createAccessPolicy("edc-demo-access-policy-" + i)).peek(policyStore::save).collect(Collectors.toList());
        var contractPolicies = IntStream.range(0, 10).mapToObj(i -> createContractPolicy("edc-demo-contract-policy-" + i)).peek(policyStore::save).collect(Collectors.toList());

        //publish asset
        assets.stream().map(AssetEntry::getAsset)
                .forEach(a -> publishAsset(a, random(accessPolicies), random(contractPolicies)));

        // populate node directory
        var nodes = readNodesFromJson(nodesFile);
        nodes.forEach(nodeDirectory::insert);

        monitor.info("Catalog Data seeding done");
    }

    public void publishAsset(Asset asset, Policy accessPolicy, Policy contractPolicy) {
        var cdef = ContractDefinition.Builder.newInstance()
                .id(asset.getId())
                .accessPolicy(accessPolicy)
                .contractPolicy(contractPolicy)
                .selectorExpression(AssetSelectorExpression.Builder.newInstance().whenEquals(Asset.PROPERTY_ID, asset.getId()).build())
                .build();
        contractDefinitionStore.save(cdef);
    }

    private <T> T random(List<T> items) {
        var random = new SecureRandom();
        var rnd = random.nextInt(items.size());
        return items.get(rnd);
    }

    private Policy createAccessPolicy(String id) {
        return Policy.Builder.newInstance()
                .id(id)
                .permission(Permission.Builder.newInstance()
                        .target("")
                        .action(Action.Builder.newInstance()
                                .type("USE")
                                .build())
                        .constraint(AtomicConstraint.Builder.newInstance()
                                .leftExpression(new LiteralExpression("foo"))
                                .operator(Operator.EQ)
                                .rightExpression(new LiteralExpression("bar"))
                                .build())
                        .build())
                .build();
    }

    private Policy createContractPolicy(String id) {
        return Policy.Builder.newInstance()
                .id(id)
                .permission(Permission.Builder.newInstance()
                        .target("")
                        .action(Action.Builder.newInstance()
                                .type("USE")
                                .build())
                        .build())
                .build();
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

    private List<AssetEntry> createAssets(String connectorId) {

        var asset1 = Asset.Builder.newInstance()
                .property("type", "file")
                .name("test-document")
                .id("test-document_" + connectorId)
                .contentType("text/plain")
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
                .version("1.0")
                .build();

        var dataAddress3 = DataAddress.Builder.newInstance()
                .type("AzureStorage")
                .property("account", "edcshowcasegpstorage")
                .property("container", "src-container")
                .property("blobname", "complex_schematic_drawing.jpg")
                .build();

        var asset4 = Asset.Builder.newInstance()
                .property("type", "http")
                .name("demo-todos")
                .id("demo-todos_" + connectorId)
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
                .version("1.0")
                .build();

        var dataAddress5 = DataAddress.Builder.newInstance()
                .type("http")
                .property("targetUrl", "https://jsonplaceholder.typicode.com/todos/2")
                .build();


        return List.of(new AssetEntry(asset1, dataAddress1), new AssetEntry(asset2, dataAddress2), new AssetEntry(asset3, dataAddress3), new AssetEntry(asset4, dataAddress4), new AssetEntry(asset5, dataAddress5));
    }
}


