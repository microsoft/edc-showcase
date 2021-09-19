package org.eclipse.dataspaceconnector.verifiable_credential;

import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.easymock.EasyMock.*;
import static org.eclipse.dataspaceconnector.verifiable_credential.TestHelper.readFile;

class EcPrivateKeyResolverTest {

    private static final String SECRET_ALIAS = "mySecret";
    private Vault vault;
    private EcPrivateKeyResolver resolver;

    @BeforeEach
    void setup() {
        vault = niceMock(Vault.class);
        resolver = new EcPrivateKeyResolver(vault);
    }


    @Test
    void resolvePrivateKey_whenNotExists() {
        expect(vault.resolveSecret(SECRET_ALIAS)).andReturn(readFile("testkey.pem"));
        replay(vault);

        assertThat(resolver.resolvePrivateKey("not_exist")).isNull();
    }

    @Test
    void resolvePrivateKey() {
        expect(vault.resolveSecret(SECRET_ALIAS)).andReturn(readFile("testkey.pem"));
        replay(vault);

        var secret = resolver.resolvePrivateKey(SECRET_ALIAS);
        assertThat(secret).isNotNull();
        assertThat(secret.getCurve().getName()).isEqualTo("secp256k1");
        assertThat(secret.isPrivate()).isTrue();
    }

    @Test
    void resolvePrivateKey_notPemFormat() {
        expect(vault.resolveSecret(SECRET_ALIAS)).andReturn("certainly this is not PEM!");
        replay(vault);

        assertThatThrownBy(() -> resolver.resolvePrivateKey(SECRET_ALIAS)).isInstanceOf(CryptoException.class);
    }
}