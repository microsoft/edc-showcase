package org.eclipse.dataspaceconnector.identity;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolver;
import org.eclipse.dataspaceconnector.spi.iam.IdentityService;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.verifiable_credential.spi.VerifiableCredentialProvider;

import java.util.Set;

public class DistributedIdentityServiceExtension implements ServiceExtension {
    @Override
    public Set<String> provides() {
        return Set.of(IdentityService.FEATURE);
    }

    @Override
    public Set<String> requires() {
        return Set.of(VerifiableCredentialProvider.FEATURE, DidResolver.FEATURE, DidPublicKeyResolver.FEATURE, CredentialsVerifier.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var vcProvider = context.getService(VerifiableCredentialProvider.class);
        var didResolver = context.getService(DidResolver.class);
        var publicKeyResolver = context.getService(DidPublicKeyResolver.class);
        var credentialsVerifier = context.getService(CredentialsVerifier.class);
        var identityService = new DistributedIdentityService(vcProvider, didResolver, publicKeyResolver, credentialsVerifier, context.getMonitor());
        context.registerService(IdentityService.class, identityService);

        context.getMonitor().info("Initialized DistributedIdentityService");

    }

    @Override
    public void start() {
        ServiceExtension.super.start();
    }
}
