package org.eclipse.dataspaceconnector.test;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.dataspaceconnector.iam.did.credentials.IonClientMock;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.ClientResponse;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHubClient;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.keys.PublicKeyWrapper;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.message.ObjectQueryRequest;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.security.PrivateKeyResolver;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.common.types.Cast.cast;

@ExtendWith(EdcExtension.class)
public class QueryRunner {

    private static final String PROVIDER_CONNECTOR = "http://localhost:9191/";

    @BeforeAll
    static void setProperties() {
        System.setProperty("edc.identity.did.url", "did:ion:random-url");
        System.setProperty("dataspaceconnector.connector.name", "test-query-connector");
    }

    @Test
    void queryWithVerifiableCredentials(RemoteMessageDispatcherRegistry dispatcherRegistry) throws Exception {

        var query = QueryRequest.Builder.newInstance()
                .connectorAddress(PROVIDER_CONNECTOR)
                .connectorId("consumer")
                .queryLanguage("dataspaceconnector")
                .query("select *")
                .protocol("ids-rest").build();

        CompletableFuture<List<String>> future = cast(dispatcherRegistry.send(List.class, query, () -> null));

        var artifacts = future.get();
        assertThat(artifacts).isNotNull().isNotEmpty();
    }

    @BeforeEach
    void before(EdcExtension extension) throws IOException, JOSEException {

        String privateKeyString = new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("private.pem").readAllBytes(), Charset.defaultCharset());

        var ecKey = ECKey.parseFromPEMEncodedObjects(privateKeyString);

        IonClientMock ionClient = new IonClientMock();
        var idHubclient = new IdentityHubClient() {
            @Override
            public ClientResponse<Map<String, Object>> queryCredentials(ObjectQueryRequest objectQueryRequest, String s, PublicKeyWrapper publicKey) {
                return null;
            }
        };


        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.identityServiceExtension());
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.identityHubExtension(idHubclient));
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.ionClientMockExtension(ionClient));
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.keyResolvers(new PrivateKeyResolver() {
            @Override
            public <T> @Nullable T resolvePrivateKey(String s, Class<T> aClass) {
                return aClass.cast(ecKey);
            }
        }));
    }

}
