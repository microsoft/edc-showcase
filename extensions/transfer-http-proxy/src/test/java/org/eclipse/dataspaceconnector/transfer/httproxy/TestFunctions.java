package org.eclipse.dataspaceconnector.transfer.httproxy;

import org.eclipse.dataspaceconnector.spi.transfer.synchronous.ProxyEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;

import java.util.Map;

public class TestFunctions {

    public static DataRequest createRequest() {
        return createRequest("http://test.domain.com");
    }

    public static DataRequest createRequest(String connectorAddress) {
        return DataRequest.Builder.newInstance()
                .destinationType("test-type")
                .assetId("asset1")
                .connectorAddress(connectorAddress)
                .id("test-request-id")
                .isSyncRequest(true)
                .build();
    }

    public static ProxyEntry createProxyEntry(String url) {
        return ProxyEntry.Builder.newInstance()
                .type("http")
                .properties(Map.of("url", url, "token", "testToken"))
                .build();
    }

    public static ProxyEntry createProxyEntry() {
        return createProxyEntry("someUrl");
    }
}
