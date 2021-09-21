package org.eclipse.dataspaceconnector.verifiablecredential;

import com.nimbusds.jose.JOSEException;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.VerificationMethod;
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

        VerificationMethod verificationMethod = didDocument.getVerificationMethod().get(0);
        var jwk = verificationMethod.getPublicKeyJwk();
        try {
            var key = ECKeyConverter.toECKey(jwk, verificationMethod.getId());

            return key.toPublicKey();
        } catch (IllegalArgumentException e) {
            throw new PublicKeyResolutionException("Public Key was not a valid EC Key!  Details: " + e.getMessage());
        } catch (JOSEException e) {
            throw new PublicKeyResolutionException(e);
        }
    }

}
