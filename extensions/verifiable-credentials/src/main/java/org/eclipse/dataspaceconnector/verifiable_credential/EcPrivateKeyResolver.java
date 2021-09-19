package org.eclipse.dataspaceconnector.verifiable_credential;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.dataspaceconnector.spi.security.PrivateKeyResolver;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.jetbrains.annotations.Nullable;

public class EcPrivateKeyResolver implements PrivateKeyResolver<ECKey> {

    private final Vault vault;

    public EcPrivateKeyResolver(Vault vault) {
        this.vault = vault;
    }

    @Override
    public @Nullable ECKey resolvePrivateKey(String name) {
        var pkPemContent = vault.resolveSecret(name);

        if (pkPemContent == null) {
//            throw new EdcException("Private Key " + name + " not found in Vault!");
            return null;
        }
        try {
            return (ECKey) ECKey.parseFromPEMEncodedObjects(pkPemContent);
        } catch (JOSEException e) {
            throw new CryptoException(e);
        }
    }
}
