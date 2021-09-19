package org.eclipse.dataspaceconnector.verifiable_credential;

import org.eclipse.dataspaceconnector.spi.EdcException;

public class CryptoException extends EdcException {
    public CryptoException(Exception inner) {
        super(inner);
    }
}
