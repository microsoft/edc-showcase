package org.eclipse.dataspaceconnector.identity;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.SignedJWT;
import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.*;
import org.eclipse.dataspaceconnector.spi.iam.ClaimToken;
import org.eclipse.dataspaceconnector.spi.iam.IdentityService;
import org.eclipse.dataspaceconnector.spi.iam.TokenResult;
import org.eclipse.dataspaceconnector.spi.iam.VerificationResult;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.verifiablecredential.ECKeyConverter;
import org.eclipse.dataspaceconnector.verifiablecredential.EcPublicKeyWrapper;
import org.eclipse.dataspaceconnector.verifiablecredential.VerifiableCredential;
import org.eclipse.dataspaceconnector.verifiablecredential.spi.VerifiableCredentialProvider;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DistributedIdentityService implements IdentityService {

    private final VerifiableCredentialProvider verifiableCredentialProvider;
    private final DidResolver didResolver;
    private final DidPublicKeyResolver publicKeyResolver;
    private final CredentialsVerifier credentialsVerifier;
    private final Monitor monitor;

    public DistributedIdentityService(VerifiableCredentialProvider vcProvider, DidResolver didResolver, DidPublicKeyResolver publicKeyResolver, CredentialsVerifier credentialsVerifier, Monitor monitor) {
        this.verifiableCredentialProvider = vcProvider;
        this.didResolver = didResolver;
        this.publicKeyResolver = publicKeyResolver;
        this.credentialsVerifier = credentialsVerifier;
        this.monitor = monitor;
    }

    @Override
    public TokenResult obtainClientCredentials(String scope) {

        var jwt = verifiableCredentialProvider.get();
        var token = jwt.serialize();
        var expiration = new Date().getTime() + TimeUnit.MINUTES.toMillis(10);

        return TokenResult.Builder.newInstance().token(token).expiresIn(expiration).build();
    }

    @Override
    public VerificationResult verifyJwtToken(String token, String audience) {
        try {
            var jwt = SignedJWT.parse(token);

            var did = didResolver.resolve(jwt.getJWTClaimsSet().getIssuer());
            Optional<VerificationMethod> publicKey = getPublicKey(did);
            if (publicKey.isEmpty()) {
                return new VerificationResult("Public Key not found in DID Document!");
            }
            EllipticCurvePublicKey publicKeyJwk = publicKey.get().getPublicKeyJwk();
            ECKey publicKeyEC = ECKeyConverter.toECKey(publicKeyJwk, publicKey.get().getId());

            if (!VerifiableCredential.verify(jwt, publicKeyEC)) {
                return new VerificationResult("Token could not be verified!");
            }

            var credentialsResult = credentialsVerifier.verifyCredentials(getHubUrl(did), new EcPublicKeyWrapper(publicKeyEC));

            var tokenBuilder = ClaimToken.Builder.newInstance();
            var claimToken = tokenBuilder.claims(credentialsResult.getValidatedCredentials()).build();

            return new VerificationResult(claimToken);
        } catch (ParseException e) {
            monitor.info("Error parsing JWT", e);
            return new VerificationResult("Error parsing JWT");
        }
    }

    private String getHubUrl(DidDocument did) {
        return did.getService().stream().filter(service -> service.getType().equals("IdentityHub")).map(Service::getServiceEndpoint).findFirst().orElseThrow();
    }

    @NotNull
    private Optional<VerificationMethod> getPublicKey(DidDocument did) {
        return did.getVerificationMethod().stream().filter(vm -> vm.getType().equals("EcdsaSecp256k1VerificationKey2019")).findFirst();
    }
}
