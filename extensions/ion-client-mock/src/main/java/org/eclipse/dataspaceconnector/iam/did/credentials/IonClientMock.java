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

import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.EllipticCurvePublicKey;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.Service;
import org.eclipse.dataspaceconnector.ion.model.IonRequest;
import org.eclipse.dataspaceconnector.ion.spi.IonClient;
import org.eclipse.dataspaceconnector.ion.util.KeyPairFactory;

import java.util.Collections;
import java.util.List;

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
        return getDocumentForUrl(didUrl);
    }

    private DidDocument getDocumentForUrl(String didUrl) {
        var service = new Service("#hub1", "IdentityHubUrl", "https://test.service.com");

        var eckey = (ECKey) KeyPairFactory.generateKeyPair().getPublicKey();
        var publicKey = new EllipticCurvePublicKey(eckey.getCurve().getName(), eckey.getKeyType().getValue(), eckey.getX().toString(), eckey.getY().toString());

        return DidDocument.Builder.newInstance()
                .id(didUrl)
                .authentication(Collections.singletonList("#key-1"))
                .service(List.of(service))
                .verificationMethod("#key-1", "EcdsaSecp256k1VerificationKey2019", publicKey)
                .build();

    }
}
