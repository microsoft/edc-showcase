package org.eclipse.dataspaceconnector.verifiable_credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

/**
 * Convenience/helper class to generate, verify and deserialize verifiable credentials, which are, in fact, Signed JSON Web Tokens (JWTs).
 */
public class VerifiableCredential {

    public static final String FEATURE = "edc:identity:verifiable-credential";

    /**
     * Creates a signed JWT {@link SignedJWT} that contains a set of claims and an issuer
     *
     * @param privateKeyPemContent The contents of a private key stored in PEM format. Although all private key types are possible, in the context of Distributed Identity and ION
     *                             using an Elliptic Curve key ({@code secp256k1}) is advisable. This can be achieved using OpenSSL CLI:
     *                             <p>
     *                             {@code openssl ecparam -name secp256k1 -genkey -noout -out secp256k1-key.pem}
     *                             </p>
     * @param claims               a list of key-value-pairs that contain claims
     * @param issuer               the "owner" of the VC, in most cases this will be the connector ID. The VC will store this in the "iss" claim
     * @return a {@code SignedJWT} that is signed with the private key and contains all claims listed
     */
    public static SignedJWT create(String privateKeyPemContent, Map<String, String> claims, String issuer) {
        try {
            var key = ECKey.parseFromPEMEncodedObjects(privateKeyPemContent);
            return create((ECKey) key, claims, issuer);
        } catch (JOSEException e) {
            throw new CryptoException(e);
        }
    }

    public static SignedJWT create(ECKey privateKey, Map<String, String> claims, String issuer) {
        var claimssetBuilder = new JWTClaimsSet.Builder();

        claims.forEach(claimssetBuilder::claim);
        var claimsSet = claimssetBuilder.issuer(issuer)
                .subject("verifiable-credential")
                .jwtID(UUID.randomUUID().toString())
                .build();

        var header = new JWSHeader(JWSAlgorithm.ES256K);

        var vc = new SignedJWT(header, claimsSet);
        try {
            vc.sign(new ECDSASigner(privateKey));
            return vc;
        } catch (JOSEException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Verifies a VerifiableCredential using the issuer's public key
     *
     * @param verifiableCredential a {@link SignedJWT} that was sent by the claiming party.
     * @param publicKey            The claiming party's public key
     * @return true if verified, false otherwise
     */
    public static boolean verify(SignedJWT verifiableCredential, ECKey publicKey) {
        try {
            return verifiableCredential.verify(new ECDSAVerifier(publicKey));
        } catch (JOSEException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Verifies a VerifiableCredential using the issuer's public key
     *
     * @param verifiableCredential a {@link SignedJWT} that was sent by the claiming party.
     * @param publicKeyPemContent  The claiming party's public key, i.e. the contents of the public key PEM file.
     * @return true if verified, false otherwise
     */
    public static boolean verify(SignedJWT verifiableCredential, String publicKeyPemContent) {
        try {
            var key = ECKey.parseFromPEMEncodedObjects(publicKeyPemContent);
            return verify(verifiableCredential, (ECKey) key);
        } catch (JOSEException e) {
            throw new CryptoException(e);
        }

    }

    /**
     * Parses a {@link SignedJWT} back to a Java object from its serialized form.
     *
     * @param jwtString The serialized form of the {@code SignedJWT}, which can be generated using {@link SignedJWT#serialize()}.
     * @return a {@link SignedJWT} containing the decoded information
     */
    public static SignedJWT parse(String jwtString) {
        try {
            return SignedJWT.parse(jwtString);
        } catch (ParseException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * A helper method to construct the name of the secret in the vault, which contains the VC.
     */
    public static String getVaultSecretName(String issuer) {
        return issuer + "-vc";
    }
}
