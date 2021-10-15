package org.eclipse.dataspaceconnector.dataseeding.catalog;

import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.metadata.MetadataStore;
import org.eclipse.dataspaceconnector.spi.policy.PolicyRegistry;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.GenericDataCatalogEntry;

import java.util.Set;

import static org.eclipse.dataspaceconnector.policy.model.Operator.IN;

public class CatalogDataseedingExtension implements ServiceExtension {
    public static final String USE_EU_POLICY = "use-eu";
    public static final String USE_US_POLICY = "use-us";

    @Override
    public Set<String> requires() {
        return Set.of(MetadataStore.FEATURE, PolicyRegistry.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        savePolicies(context);
        saveEntries(context);

        monitor.info("Catalog Data seeding done");
    }

    private void saveEntries(ServiceExtensionContext context) {
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
        DataEntry entry3 = DataEntry.Builder.newInstance().id("schematic-drawing").policyId(USE_EU_POLICY).catalogEntry(file2).build();

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


