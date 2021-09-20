package org.eclipse.dataspaceconnector.verifiable_credential.spi;

import com.nimbusds.jwt.SignedJWT;

import java.util.function.Supplier;

public interface VerifiableCredentialProvider extends Supplier<SignedJWT> {
    String FEATURE = "edc:identity:verifiable-credential:provider";
}
