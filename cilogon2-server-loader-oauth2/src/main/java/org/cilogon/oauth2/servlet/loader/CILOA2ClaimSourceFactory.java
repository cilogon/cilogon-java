package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.ClaimSourceFactoryImpl;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.LDAPClaimSourceFactoryRequest;
import edu.uiuc.ncsa.security.oauth_2_0.server.claims.ClaimSource;
import edu.uiuc.ncsa.security.oauth_2_0.server.claims.ClaimSourceFactoryRequest;
import org.cilogon.oauth2.servlet.impl.CILogonClaimSource;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/19/16 at  4:27 PM
 */
public class CILOA2ClaimSourceFactory extends ClaimSourceFactoryImpl {
    @Override
    public ClaimSource create(ClaimSourceFactoryRequest request) {
        if(request instanceof LDAPClaimSourceFactoryRequest) {
            LDAPClaimSourceFactoryRequest req = (LDAPClaimSourceFactoryRequest) request;
            CILogonClaimSource handler = new CILogonClaimSource(
                    req.getLdapConfiguration(),
                    req.getLogger());
            handler.setScopes(req.getScopes());
            return handler;
        }
        return super.create(request);
    }
}
