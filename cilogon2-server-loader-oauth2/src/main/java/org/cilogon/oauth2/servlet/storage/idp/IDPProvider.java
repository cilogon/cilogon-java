package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/6/12 at  11:17 AM
 */
public class IDPProvider implements IdentifiableProvider<IdentityProvider> {

    @Override
    public IdentityProvider get(boolean createNewIdentifier) {
        if(createNewIdentifier){
            // This exists to fulfill the contract of the identifiable provider interface and
            // is used by a couple of utilities. Generally though this is not the best way to create
            // one of these....
            SecureRandom s = new SecureRandom();
            byte[] b = new byte[32];
            s.nextBytes(b);
            BigInteger bigInt = new BigInteger(b);
            return new IdentityProvider(BasicIdentifier.newID("urn:oa4mp/idp/" + bigInt.abs().toString(16)));
        }
        return new IdentityProvider((Identifier) null);
    }

    @Override
    public IdentityProvider get() {
        return get(true);
    }
}
