package org.eclipse.dataspaceconnector.test;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.dataspaceconnector.iam.did.credentials.IonClientMock;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.ClientResponse;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHubClient;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.message.ObjectQueryRequest;
import org.eclipse.dataspaceconnector.spi.iam.TokenResult;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.security.PrivateKeyResolver;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.eclipse.dataspaceconnector.common.types.Cast.cast;

@ExtendWith(EdcExtension.class)
public class QueryRunner {

    private static final String PROVIDER_CONNECTOR = "http://localhost:8181/";
    private static final TokenResult US_TOKEN = TokenResult.Builder.newInstance().token("mock-us").build();

    static {
        System.setProperty("edc.identity.did.url", "did:ion:random-url");
    }

    @Test
    void queryWithVerifiableCredentials(RemoteMessageDispatcherRegistry dispatcherRegistry) throws Exception {

        var query = QueryRequest.Builder.newInstance()
                .connectorAddress(PROVIDER_CONNECTOR)
                .connectorId(PROVIDER_CONNECTOR)
                .queryLanguage("dataspaceconnector")
                .query("select *")
                .protocol("ids-rest").build();

        CompletableFuture<List<String>> future = cast(dispatcherRegistry.send(List.class, query, () -> null));

        var artifacts = future.get();
        artifacts = artifacts.stream().findAny().stream().collect(Collectors.toList());
    }

    @BeforeEach
    void before(EdcExtension extension) throws IOException, JOSEException {


        String privateKeyString = new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("consumer-priv.pem").readAllBytes(), Charset.defaultCharset());

        var ecKey = ECKey.parseFromPEMEncodedObjects(privateKeyString);

        IonClientMock ionClient = new IonClientMock();
        var idHubclient = new IdentityHubClient() {
            @Override
            public ClientResponse<Map<String, Object>> queryCredentials(ObjectQueryRequest objectQueryRequest, String s, PublicKey publicKey) {
                return null;
            }
        };


        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.identityServiceExtension());
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.ionCredentialsVerifierExtension(idHubclient));
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.ionClientMockExtension(ionClient));
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.keyResolvers(new PrivateKeyResolver() {
            @Override
            public <T> @Nullable T resolvePrivateKey(String s, Class<T> aClass) {
                return aClass.cast(ecKey);
            }
        }));
    }

}
