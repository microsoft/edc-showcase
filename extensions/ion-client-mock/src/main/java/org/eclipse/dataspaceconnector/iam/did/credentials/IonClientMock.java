/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */
package org.eclipse.dataspaceconnector.iam.did.credentials;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolveResponse;
import org.eclipse.dataspaceconnector.ion.IonException;
import org.eclipse.dataspaceconnector.ion.spi.IonClient;
import org.eclipse.dataspaceconnector.ion.spi.request.IonRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Implements a mock IonClient, that can resolve three static DID documents
 */
public class IonClientMock implements IonClient {
    @Override
    public DidDocument submit(IonRequest ionRequest) {
        throw new UnsupportedOperationException("submitting DIDs is not supported");
    }

    @Override
    public DidDocument resolve(String didUrl) {
        try {
            return getDocumentForUrl(didUrl);
        } catch (IOException e) {
            throw new IonException(e);
        }
    }

    private DidDocument getDocumentForUrl(String didUrl) throws IOException {
        InputStream is;
        if (didUrl.equals("did:ion:EiAnKD8-jfdd0MDcZUjAbRgaThBrMxPTFOxcnfJhI7Ukaw")) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("consumer-did.json");
        } else if (didUrl.equals("did:ion:EiDfkaPHt8Yojnh15O7egrj5pA9tTefh_SYtbhF1-XyAeA")) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("provider-did.json");
        } else {
            throw new IllegalArgumentException(String.format("DID %s is not known to the system!", didUrl));
        }
        var response = new ObjectMapper().readValue(Objects.requireNonNull(is).readAllBytes(), DidResolveResponse.class);
        DidDocument didDocument = response.getDidDocument();
        didDocument.setId(didUrl);

        return didDocument;
    }
}
