package org.eclipse.dataspaceconnector.verifiable_credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64URL;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.VerificationMethod;
import org.eclipse.dataspaceconnector.ion.spi.IonClient;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.List;
import java.util.Set;
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
            ECKey key = new ECKey(Curve.parse(jwk.getCrv()),
                    Base64URL.from(jwk.getX()),
                    Base64URL.from(jwk.getY()),
                    KeyUse.SIGNATURE,
                    Set.of(KeyOperation.VERIFY),
                    null,
                    verificationMethod.getId(),
                    null, null, null, null, null
            );

            return key.toPublicKey();
        } catch (IllegalArgumentException e) {
            throw new PublicKeyResolutionException("Public Key was not a valid EC Key!  Details: " + e.getMessage());
        } catch (JOSEException e) {
            throw new PublicKeyResolutionException(e);
        }
    }

}
