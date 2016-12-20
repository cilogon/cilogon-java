package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.LDAPScopeHandlerFactory;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.LDAPScopeHandlerFactoryRequest;
import edu.uiuc.ncsa.security.oauth_2_0.server.ScopeHandler;
import edu.uiuc.ncsa.security.oauth_2_0.server.ScopeHandlerFactoryRequest;
import org.cilogon.oauth2.servlet.impl.CILogonScopeHandler;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/19/16 at  4:27 PM
 */
public class CILOA2ScopeHandlerFactory extends LDAPScopeHandlerFactory {
    @Override
    public ScopeHandler create(ScopeHandlerFactoryRequest request) {
        if(request instanceof LDAPScopeHandlerFactoryRequest) {
            LDAPScopeHandlerFactoryRequest req = (LDAPScopeHandlerFactoryRequest) request;
            CILogonScopeHandler handler = new CILogonScopeHandler(
                    req.getLdapConfiguration(),
                    req.getLogger());
            handler.setScopes(req.getScopes());
            return handler;
        }
        return super.create(request);
    }
}
