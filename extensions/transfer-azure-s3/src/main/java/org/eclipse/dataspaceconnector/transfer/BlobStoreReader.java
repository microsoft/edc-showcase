package org.eclipse.dataspaceconnector.transfer;

import org.eclipse.dataspaceconnector.common.azure.BlobStoreApiImpl;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;

import java.io.ByteArrayInputStream;

class BlobStoreReader implements DataReader {

    private final Vault vault;

    BlobStoreReader(Vault vault) {
        this.vault = vault;
    }

    @Override
    public ByteArrayInputStream read(DataAddress source) {

        var account = source.getProperty("account");
        var container = source.getProperty("container");
        var blobName = source.getProperty("blobname");
        var blobStoreApi = new BlobStoreApiImpl(vault, "https://" + account + ".blob.core.windows.net");
        return new ByteArrayInputStream(blobStoreApi.getBlob(account, container, blobName));
    }
}
