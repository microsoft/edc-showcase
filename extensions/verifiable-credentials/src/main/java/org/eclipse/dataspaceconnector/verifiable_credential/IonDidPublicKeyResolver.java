package org.eclipse.dataspaceconnector.verifiable_credential;

import org.eclipse.dataspaceconnector.iam.did.spi.resolver.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.ion.model.did.resolution.VerificationMethod;
import org.eclipse.dataspaceconnector.ion.spi.IonClient;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.List;
import java.util.stream.Collectors;

public class IonDidPublicKeyResolver implements DidPublicKeyResolver {
    private final IonClient ionClient;

    public IonDidPublicKeyResolver(IonClient ionClient) {
        this.ionClient = ionClient;
    }

    @Override
    public @Nullable PublicKey resolvePublicKey(String s) {
        var didDocument = ionClient.resolve(s);
        if (didDocument == null) {
            return null;
        }
        if (didDocument.getVerificationMethod() == null || didDocument.getVerificationMethod().isEmpty()) {
            throw new PublicKeyResolutionException("DID does not contain a Public Key!");
        }

        List<VerificationMethod> verificationMEthods = didDocument.getVerificationMethod().stream().filter(vm -> vm.getType().equals("EcdsaSecp256k1VerificationKey2019")).collect(Collectors.toList());
        if (verificationMEthods.size() > 1) {
            throw new PublicKeyResolutionException("DID contains more than one \"EcdsaSecp256k1VerificationKey2019\" public keys!");
        }

        var jwk = didDocument.getVerificationMethod().get(0).getPublicKeyJwk();
        throw new UnsupportedOperationException("Cannot convert to PublicKey right now");
    }

}
