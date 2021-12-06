package org.eclipse.dataspaceconnector.transfer.httproxy;

import com.nimbusds.jwt.PlainJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.transfer.httproxy.TestFunctions.createRequest;

class RestDataProxyTest {
    RestDataProxy proxy;

    @BeforeEach
    void setUp() {
        List<String> issuedTokens = new ArrayList<>();
        issuedTokens.add("test-token");
        proxy = new RestDataProxy("test/path", "test-connector-id", issuedTokens);
    }

    @Test
    void getData() throws ParseException {
        var rq = createRequest();
        var entry = proxy.getData(rq);
        assertThat(entry).isNotNull();
        assertThat(entry.getType()).isEqualTo(rq.getDestinationType());
        assertThat(entry.getProperties()).isNotEmpty().hasSize(2)
                .containsEntry("url", "test/path/" + rq.getAssetId())
                .containsKey("token");

        PlainJWT.parse(entry.getProperties().get("token").toString()); //should not throw
    }


}