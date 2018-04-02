package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.LDAPClaimSourceFactory;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.LDAPClaimSourceFactoryRequest;
import edu.uiuc.ncsa.security.oauth_2_0.server.ClaimSource;
import edu.uiuc.ncsa.security.oauth_2_0.server.ClaimSourceFactoryRequest;
import org.cilogon.oauth2.servlet.impl.CILogonClaimSource;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/19/16 at  4:27 PM
 */
public class CILOA2ClaimSourceFactory extends LDAPClaimSourceFactory {
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
