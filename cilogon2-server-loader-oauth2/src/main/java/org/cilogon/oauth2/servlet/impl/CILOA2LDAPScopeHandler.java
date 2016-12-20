package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.LDAPScopeHandler;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.oauth_2_0.server.LDAPConfiguration;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/8/16 at  11:47 AM
 */
public class CILOA2LDAPScopeHandler extends LDAPScopeHandler {
    public CILOA2LDAPScopeHandler(LDAPConfiguration ldapConfiguration, MyLoggingFacade myLogger) {
        super(ldapConfiguration, myLogger);
    }

/*    @Override
    public String getSearchName(UserInfo userInfo, HttpServletRequest request, ServiceTransaction transaction) {
        String searchName = (String) userInfo.getMap().get(getCfg().getSearchNameKey());

        // This is to look in the NCSA's LDAP handler
        String username = searchName.substring(0, searchName.indexOf("@")); // take the name from the email
 //       return eppn;
        return username;
    }*/
}
