package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.LDAPScopeHandler;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.oauth_2_0.UserInfo;

import javax.servlet.http.HttpServletRequest;

import static org.cilogon.oauth2.servlet.impl.CILogonScopeHandler.CILogonClaims.EPPN;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/8/16 at  11:47 AM
 */
public class CILOA2LDAPScopeHandler extends LDAPScopeHandler {

    @Override
    public String getSearchName(UserInfo userInfo, HttpServletRequest request, ServiceTransaction transaction) {
        String eppn = (String) userInfo.getMap().get(EPPN);
        // This is to look in the NCSA's LDAP handler
        String username = eppn.substring(0, eppn.indexOf("@")); // take the name from the email
        return username;

    }
}
