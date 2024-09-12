package org.cilogon.oauth2.servlet.servlet;


import org.oa4mp.delegation.server.OA2Scopes;
import org.oa4mp.server.loader.oauth2.servlet.OA2RegistrationServlet;

import java.util.Collection;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/9/23 at  8:19 AM
 */
public class CILOA2RegistrationServlet extends OA2RegistrationServlet {
    @Override
    protected Collection<String> getDisplayScopes() {
        Collection<String> ds =  super.getDisplayScopes();
        ds.remove(OA2Scopes.SCOPE_USER_INFO); // This is not displayed for CILogon in preference to its own.
        return ds;
    }
}
