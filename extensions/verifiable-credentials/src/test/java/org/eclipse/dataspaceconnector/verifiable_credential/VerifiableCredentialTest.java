package org.eclipse.dataspaceconnector.verifiable_credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class VerifiableCredentialTest {


    private ECKey privateKey;

    @BeforeEach
    void setup() throws JOSEException {
        String contents = readFile("testkey.pem");

        privateKey = (ECKey) ECKey.parseFromPEMEncodedObjects(contents);
    }

    private String readFile(String filename) {
        var stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        Scanner s = new Scanner(Objects.requireNonNull(stream)).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Test
    void createVerifiableCredential() throws ParseException {
        var vc = VerifiableCredential.create(privateKey, Map.of("did-url", "someUrl"), "test-connector");

        assertThat(vc).isNotNull();
        assertThat(vc.getJWTClaimsSet().getClaim("did-url")).isEqualTo("someUrl");
        assertThat(vc.getJWTClaimsSet().getClaim("iss")).isEqualTo("test-connector");
        assertThat(vc.getJWTClaimsSet().getClaim("sub")).isEqualTo("verifiable-credential");
    }

    @Test
    void ensureSerialization() throws ParseException {
        var vc = VerifiableCredential.create(privateKey, Map.of("did-url", "someUrl"), "test-connector");

        assertThat(vc).isNotNull();
        String jwtString = vc.serialize();

        //deserialize
        var deserialized = VerifiableCredential.parse(jwtString);

        assertThat(deserialized.getJWTClaimsSet()).isEqualTo(vc.getJWTClaimsSet());
        assertThat(deserialized.getHeader().getAlgorithm()).isEqualTo(vc.getHeader().getAlgorithm());
        assertThat(deserialized.getPayload().toString()).isEqualTo(vc.getPayload().toString());
    }

    @Test
    void verifyJwt() throws JOSEException {
        var vc = VerifiableCredential.create(privateKey, Map.of("did-url", "someUrl"), "test-connector");
        String jwtString = vc.serialize();

        //deserialize
        var jwt = VerifiableCredential.parse(jwtString);
        var pubKey = readFile("testkey.pub.pem");

        assertThat(VerifiableCredential.verify(jwt, (ECKey) ECKey.parseFromPEMEncodedObjects(pubKey))).isTrue();

    }
}