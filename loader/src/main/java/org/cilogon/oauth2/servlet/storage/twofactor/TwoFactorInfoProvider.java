package org.cilogon.oauth2.servlet.storage.twofactor;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Two factor info objects share the user id as their unique identifier.
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  9:27 AM
 */
public class TwoFactorInfoProvider extends IdentifiableProviderImpl<TwoFactorInfo> {
    public TwoFactorInfoProvider(IdentifiableProvider<Identifier> idProvider) {
        super(idProvider);
    }


    public TwoFactorInfoProvider() {
        super(null);

    }

    @Override
       public TwoFactorInfo get(boolean createNewIdentifier) {
           if(createNewIdentifier){
               // This exists to fulfill the contract of the identifiable provider interface and
               // is used by a couple of utilities. Generally though this is not the best way to create
               // one of these....
               SecureRandom s = new SecureRandom();
               byte[] b = new byte[32];
               s.nextBytes(b);
               BigInteger bigInt = new BigInteger(b);
               return new TwoFactorInfo(BasicIdentifier.newID("urn:oa4mp/twoFactor/info/" + bigInt.abs().toString(16)));
           }
           return new TwoFactorInfo((Identifier) null);
       }


    public TwoFactorInfo get(Identifier identifier) {
        return new TwoFactorInfo(identifier);
    }
}
