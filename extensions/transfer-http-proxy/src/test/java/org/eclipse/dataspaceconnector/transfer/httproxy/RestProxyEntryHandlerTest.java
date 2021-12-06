package org.eclipse.dataspaceconnector.transfer.httproxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.niceMock;
import static org.eclipse.dataspaceconnector.transfer.httproxy.TestFunctions.createProxyEntry;
import static org.eclipse.dataspaceconnector.transfer.httproxy.TestFunctions.createRequest;

class RestProxyEntryHandlerTest {

    private RestProxyEntryHandler handler;
    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        server = new MockWebServer();

        handler = new RestProxyEntryHandler(niceMock(Monitor.class), httpClient);
    }

    //
    @Test
    void accept() throws IOException {
        var objectMapper = new ObjectMapper();
        var expectedBody = objectMapper.writeValueAsString(createProxyEntry());
        server.enqueue(new MockResponse().setBody(expectedBody));
        server.start();

        var result = handler.accept(createRequest(server.url("").toString()), createProxyEntry(server.url("someAsset/").toString()));

        assertThat(result).isInstanceOf(String.class).isEqualTo(expectedBody);
    }
}