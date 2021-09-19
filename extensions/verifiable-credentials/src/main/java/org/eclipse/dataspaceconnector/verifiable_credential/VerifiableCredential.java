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

public class VerifiableCredential {


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

    public static boolean verify(SignedJWT verifiableCredential, ECKey publicKey) {
        try {
            return verifiableCredential.verify(new ECDSAVerifier(publicKey));
        } catch (JOSEException e) {
            throw new CryptoException(e);
        }
    }

    public static SignedJWT parse(String jwtString) {
        try {
            return SignedJWT.parse(jwtString);
        } catch (ParseException e) {
            throw new CryptoException(e);
        }
    }
}
