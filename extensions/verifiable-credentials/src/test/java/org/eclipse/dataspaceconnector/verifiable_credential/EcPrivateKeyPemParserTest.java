package org.eclipse.dataspaceconnector.verifiable_credential;

import com.nimbusds.jose.jwk.ECKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.interfaces.DHPrivateKey;
import java.security.interfaces.RSAPrivateKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.dataspaceconnector.verifiable_credential.TestHelper.readFile;

class EcPrivateKeyPemParserTest {

    private EcPrivateKeyPemParser keyParser;

    @BeforeEach
    void setup() {
        keyParser = new EcPrivateKeyPemParser();
    }

    @Test
    void canParse() {
        assertThat(keyParser.canParse(RSAPrivateKey.class)).isFalse();
        assertThat(keyParser.canParse(DHPrivateKey.class)).isFalse();
        assertThat(keyParser.canParse(ECKey.class)).isTrue();
    }

    @Test
    void parse() {
        var pemContent = readFile("testkey.pem");
        var key = keyParser.parse(pemContent);
        assertThat(key).isInstanceOf(ECKey.class);

        assertThat(key.isPrivate()).isTrue();
        assertThat(key.getCurve().getName()).isEqualTo("secp256k1");
    }

    @Test
    void parse_invalidEncoding() {
        var content = "this_is_not_PEM_format";
        assertThatThrownBy(() -> keyParser.parse(content)).isInstanceOf(CryptoException.class);
    }

    @Test
    void parse_illegalParams() {
        assertThatThrownBy(() -> keyParser.parse(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> keyParser.parse("")).isInstanceOf(CryptoException.class);
    }
}