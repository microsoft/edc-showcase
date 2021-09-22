package org.eclipse.dataspaceconnector.dataseeding.catalog;

import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHubStore;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.message.Commit;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class IdentityHubDataseedingExtension implements ServiceExtension {
    @Override
    public Set<String> requires() {
        return Set.of(IdentityHubStore.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        var hubStore = context.getService(IdentityHubStore.class);
        // DEBUG!!
        //            String verifierPrivateKeyPem = Files.readString(Path.of("/home/paul/dev/ion-demo/keys/verifier-priv.pem"));
//            ECKey privateKey = (ECKey) ECKey.parseFromPEMEncodedObjects(verifierPrivateKeyPem);

        var objectId = "MyRandomObjectID";
        var payload =
                Map.of("region", "eu", "created", Instant.now().toEpochMilli());
        var commit = Commit.Builder.newInstance().type("RegistrationCredentials").context("ION Demo").iss(context.getConnectorId()).sub("test").objectId(objectId).payload(payload).build();
        hubStore.write(commit);


        // END DEBUG


        monitor.info("Catalog Data seeding done");
    }

}
