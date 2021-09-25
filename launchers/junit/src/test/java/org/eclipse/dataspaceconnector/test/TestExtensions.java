/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
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

package org.eclipse.dataspaceconnector.test;/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

import org.eclipse.dataspaceconnector.iam.did.IdentityDidCoreHubExtension;
import org.eclipse.dataspaceconnector.iam.did.credentials.IdentityHubCredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.credentials.IonClientMock;
import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHubClient;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolver;
import org.eclipse.dataspaceconnector.identity.DistributedIdentityService;
import org.eclipse.dataspaceconnector.ion.spi.IonClient;
import org.eclipse.dataspaceconnector.spi.iam.IdentityService;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.security.PrivateKeyResolver;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.verifiablecredential.IonDidPublicKeyResolver;
import org.eclipse.dataspaceconnector.verifiablecredential.spi.VerifiableCredentialProvider;

import java.util.Set;


public class TestExtensions {

    private static final Monitor MONITOR = new Monitor() {
    };

    public static ServiceExtension identityServiceExtension() {


        return new ServiceExtension() {
            @Override
            public Set<String> provides() {
                return Set.of(IdentityService.FEATURE);
            }

            @Override
            public Set<String> requires() {
                return Set.of(VerifiableCredentialProvider.FEATURE, IonClient.FEATURE, IdentityHubClient.FEATURE, DidPublicKeyResolver.FEATURE);
            }

            @Override
            public void initialize(ServiceExtensionContext context) {
                var verifiableCredentialProvider = context.getService(VerifiableCredentialProvider.class);
                var ionClient = context.getService(IonClient.class);
                var idHubclient = context.getService(IdentityHubClient.class);
                DidPublicKeyResolver publicKeyResolver = context.getService(DidPublicKeyResolver.class);
                var identityService = new DistributedIdentityService(verifiableCredentialProvider, ionClient, publicKeyResolver, new IdentityHubCredentialsVerifier(idHubclient, MONITOR), MONITOR);
                context.registerService(IdentityService.class, identityService);
            }
        };
    }

    public static ServiceExtension identityHubClientExtension(IdentityHubClient hubclient) {
        return new ServiceExtension() {
            @Override
            public Set<String> provides() {
                return Set.of(CredentialsVerifier.FEATURE, IdentityHubClient.FEATURE);
            }

            @Override
            public void initialize(ServiceExtensionContext context) {
                context.registerService(CredentialsVerifier.class, new IdentityHubCredentialsVerifier(hubclient, MONITOR));
                context.registerService(IdentityHubClient.class, hubclient);
            }
        };
    }

    public static ServiceExtension ionClientMockExtension(IonClientMock ionClient) {
        return new ServiceExtension() {
            @Override
            public Set<String> provides() {
                return Set.of(IonClient.FEATURE, DidResolver.FEATURE);
            }


            @Override
            public void initialize(ServiceExtensionContext context) {
                context.registerService(IonClient.class, ionClient);
                context.registerService(DidResolver.class, ionClient);
            }
        };
    }

    public static ServiceExtension keyResolvers(PrivateKeyResolver privateKeyResolver) {
        return new ServiceExtension() {
            @Override
            public Set<String> provides() {
                return Set.of(PrivateKeyResolver.FEATURE, DidPublicKeyResolver.FEATURE);
            }

            @Override
            public Set<String> requires() {
                return Set.of(IonClient.FEATURE);
            }

            @Override
            public void initialize(ServiceExtensionContext context) {
                var ionClient = context.getService(IonClient.class);
                context.registerService(PrivateKeyResolver.class, privateKeyResolver);
                context.registerService(DidPublicKeyResolver.class, new IonDidPublicKeyResolver(ionClient));
            }
        };
    }

    public static ServiceExtension identityHubExtension() {
        return new IdentityDidCoreHubExtension();
    }
}
