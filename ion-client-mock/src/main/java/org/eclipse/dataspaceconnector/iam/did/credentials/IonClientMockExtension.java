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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import org.eclipse.dataspaceconnector.ion.spi.IonClient;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;


public class IonClientMockExtension implements ServiceExtension {

    @Override
    public Set<String> provides() {
        return Set.of("edc:ion:client");
    }


    @Override
    public void initialize(ServiceExtensionContext context) {
        var ionClient = new IonClientMock();
        context.registerService(IonClient.class, ionClient);
    }

    private JWK parsePemAsJWK(String resourceName) {

        try {
            var pemContents = Files.readString(Path.of(resourceName));
            return ECKey.parseFromPEMEncodedObjects(pemContents);

        } catch (JOSEException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
