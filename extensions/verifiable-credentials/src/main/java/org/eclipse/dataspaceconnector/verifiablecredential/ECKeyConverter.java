package org.eclipse.dataspaceconnector.verifiablecredential;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64URL;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.EllipticCurvePublicKey;

import java.util.Set;

public class ECKeyConverter {

    public static ECKey toECKey(EllipticCurvePublicKey jwk, String id) {
        return new ECKey(Curve.parse(jwk.getCrv()),
                Base64URL.from(jwk.getX()),
                Base64URL.from(jwk.getY()),
                KeyUse.SIGNATURE,
                Set.of(KeyOperation.VERIFY),
                null,
                id,
                null, null, null, null, null
        );
    }
}
