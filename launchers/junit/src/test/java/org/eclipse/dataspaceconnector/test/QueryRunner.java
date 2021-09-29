package org.eclipse.dataspaceconnector.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.ClientResponse;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHubClient;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.keys.PrivateKeyWrapper;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.keys.PublicKeyWrapper;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.message.ObjectQueryRequest;
import org.eclipse.dataspaceconnector.ion.DefaultIonClient;
import org.eclipse.dataspaceconnector.ion.crypto.EcPrivateKeyWrapper;
import org.eclipse.dataspaceconnector.ion.spi.IonClient;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.security.PrivateKeyResolver;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.QueryRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.common.types.Cast.cast;

@Disabled
@ExtendWith(EdcExtension.class)
public class QueryRunner {

    private static final String PROVIDER_CONNECTOR = "http://localhost:9191/";

    @BeforeAll
    static void setProperties() {
        // the url is the example one from identity.foundation/ion/explorer
        // this is the connector's own DID URL
        System.setProperty("edc.identity.did.url", "did:ion:EiAnKD8-jfdd0MDcZUjAbRgaThBrMxPTFOxcnfJhI7Ukaw");
        System.setProperty("dataspaceconnector.connector.name", "test-query-connector");
        System.setProperty("web.http.port", "7171");
    }

    @Test
    void queryWithVerifiableCredentials(RemoteMessageDispatcherRegistry dispatcherRegistry) throws Exception {

        var query = QueryRequest.Builder.newInstance()
                .connectorAddress(PROVIDER_CONNECTOR)
                .connectorId(System.getProperty("dataspaceconnector.connector.name"))
                .queryLanguage("dataspaceconnector")
                .query("select *")
                .protocol("ids-rest").build();

        CompletableFuture<List<String>> future = cast(dispatcherRegistry.send(List.class, query, () -> null));

        var artifacts = future.get();
        assertThat(artifacts).isNotNull().isNotEmpty();

        //TODO: run data request
    }

    @BeforeEach
    void before(EdcExtension extension) throws IOException, JOSEException {

        String privateKeyString = new String(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("private.pem")).readAllBytes(), Charset.defaultCharset());

        var ecKey = ECKey.parseFromPEMEncodedObjects(privateKeyString);

        IonClient ionClient = new DefaultIonClient(new ObjectMapper());
        var idHubclient = new IdentityHubClient() {
            @Override
            public ClientResponse<Map<String, Object>> queryCredentials(ObjectQueryRequest objectQueryRequest, String s, PublicKeyWrapper publicKey) {
                return null;
            }
        };


        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.identityServiceExtension());
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.identityHubClientExtension(idHubclient));
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.ionClientMockExtension(ionClient));
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.keyResolvers(new PrivateKeyResolver() {
            @Override
            public <T> T resolvePrivateKey(String s, Class<T> aClass) {
                if (aClass == ECKey.class) {
                    return aClass.cast(ecKey);
                } else if (aClass == PrivateKeyWrapper.class) {
                    return aClass.cast(new EcPrivateKeyWrapper((ECKey) ecKey));
                } else {
                    throw new RuntimeException("Cannot utilize key type " + aClass);
                }
            }
        }));
        extension.registerSystemExtension(ServiceExtension.class, TestExtensions.identityHubExtension());
    }

}
