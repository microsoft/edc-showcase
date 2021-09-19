package org.eclipse.dataspaceconnector.verifiable_credential;

import net.jodah.failsafe.RetryPolicy;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.security.VaultResponse;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static net.jodah.failsafe.Failsafe.with;

public class VerifiableCredentialLoaderExtension implements ServiceExtension {

    private static final String DID_URL_SETTING = "edc.identity.did.url";

    @Override
    public Set<String> provides() {
        return Set.of("edc:identity:verifiable-credential");
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        monitor.info("Initializing Verifiable Credential");
        var didUrl = context.getSetting(DID_URL_SETTING, null);
        if (didUrl == null) {
            throw new EdcException(format("The DID Url setting '(%s)' was null!", DID_URL_SETTING));
        }

        // we'll use the connector name to restore the Private Key
        var connectorName = context.getConnectorId();
        var vault = context.getService(Vault.class);
        var privateKeyString = vault.resolveSecret(connectorName); //to get the private key

        // we'll store the VC in the vault if not already exists
        var vcSecretName = VerifiableCredential.getVaultSecretName(connectorName);
        // lets use a retry policy, bc sometimes delete and create are not 100% sequential operations...
        RetryPolicy<Object> retryPolicy = new RetryPolicy<>().withMaxRetries(5).withDelay(500, 1000, ChronoUnit.MILLIS);

        if (vault.resolveSecret(vcSecretName) != null) {
            monitor.info("Old VC found in Vault. Will delete and re-create");
            with(retryPolicy).run(() -> vault.deleteSecret(vcSecretName));
        }

        monitor.info("Creating VC...");
        var vc = VerifiableCredential.create(privateKeyString, Map.of(DID_URL_SETTING, didUrl), connectorName);
        with(retryPolicy).run(() -> {
            monitor.debug("Attempting to store secret");
            if (vault.storeSecret(vcSecretName, vc.serialize()) != VaultResponse.OK) {
                throw new EdcException("error storing VC"); //will be swallowed during retry.
            }
            monitor.info(format("Verifiable Credential for \"%s\" stored in Vault.", connectorName));
        });

    }

    @Override
    public void start() {
        ServiceExtension.super.start();
    }
}
