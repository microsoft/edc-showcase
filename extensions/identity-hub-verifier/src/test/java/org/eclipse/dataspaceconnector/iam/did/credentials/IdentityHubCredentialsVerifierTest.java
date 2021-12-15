package org.eclipse.dataspaceconnector.iam.did.credentials;

import org.easymock.EasyMock;
import org.eclipse.dataspaceconnector.iam.did.crypto.key.RsaPublicKeyWrapper;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHubClient;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.message.ObjectQueryRequest;
import org.eclipse.dataspaceconnector.iam.did.spi.key.PublicKeyWrapper;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;


class IdentityHubCredentialsVerifierTest {
    private IdentityHubClient hubClient;
    private IdentityHubCredentialsVerifier credentialsVerifier;
    private RSAPublicKey publicKey;

    @Test
    void verifyCredentials() {
        EasyMock.expect(hubClient.queryCredentials(EasyMock.isA(ObjectQueryRequest.class), EasyMock.isA(String.class), EasyMock.isA(PublicKeyWrapper.class))).andReturn(Result.success(Map.of("region", "EU")));
        EasyMock.replay(hubClient);

        var result = credentialsVerifier.verifyCredentials("https://foo.com", new RsaPublicKeyWrapper(publicKey));
        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals("EU", result.getContent().get("region"));
        EasyMock.verify(hubClient);
    }

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        publicKey = (RSAPublicKey) kp.getPublic();
        hubClient = EasyMock.createMock(IdentityHubClient.class);
        credentialsVerifier = new IdentityHubCredentialsVerifier(hubClient, new Monitor() {
        }, "did:ion:test");

    }

}
