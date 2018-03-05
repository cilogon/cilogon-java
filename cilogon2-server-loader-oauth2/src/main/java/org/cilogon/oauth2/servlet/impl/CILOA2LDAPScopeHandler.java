package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.LDAPScopeHandler;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.oauth_2_0.UserInfo;
import edu.uiuc.ncsa.security.oauth_2_0.server.UnsupportedScopeException;
import edu.uiuc.ncsa.security.oauth_2_0.server.config.LDAPConfiguration;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/8/16 at  11:47 AM
 */
public class CILOA2LDAPScopeHandler extends LDAPScopeHandler {
    public CILOA2LDAPScopeHandler(LDAPConfiguration ldapConfiguration, MyLoggingFacade myLogger) {
        super(ldapConfiguration, myLogger);
    }

    @Override
    public String getSearchName(UserInfo userInfo, HttpServletRequest request, ServiceTransaction transaction) {
        DebugUtil.dbg(this, "Getting search name");

        String searchName = (String) userInfo.getMap().get(getCfg().getSearchNameKey());
        // NOTE this is to check if the LDAP server is the NCSA server. In this case, the username is the
        // email address and we peel off the kerberos name to use for the query.
        // This will be fixed in a future release to make it configurable.
        if (getCfg().getServer().equals("ldap.ncsa.illinois.edu") && "https://idp.ncsa.illinois.edu/idp/shibboleth".equals(userInfo.getMap().get(CILogonScopeHandler.CILogonClaims.IDP))) {
            DebugUtil.dbg(this, "Getting search name for NCSA LDAP");
            //searchName = (String) userInfo.getMap().get(CILogonScopeHandler.CILogonClaims.EPPN);
            searchName = (String) userInfo.getMap().get(getCfg().getSearchNameKey());
            searchName = searchName.substring(0, searchName.indexOf("@")); // take the name from the email
            // This is to look in the NCSA's LDAP handler
        }
        DebugUtil.dbg(this,"search name=" + searchName);
        //       return eppn;
        return searchName;
    }

    @Override
    public synchronized UserInfo process(UserInfo userInfo, HttpServletRequest request, ServiceTransaction transaction) throws UnsupportedScopeException {
        DebugUtil.dbg(this,"Starting in " + getClass().getSimpleName());
        DebugUtil.dbg(this, "server=" + getCfg().getServer() + ", cfg=" + getCfg());
        DebugUtil.dbg(this, "enabled? " + getCfg().isEnabled());
        DebugUtil.dbg(this,"user info=" + userInfo.getMap());
        DebugUtil.dbg(this,"starting to process...");
        String idp = String.valueOf(userInfo.getMap().get(CILogonScopeHandler.CILogonClaims.IDP));
        DebugUtil.dbg(this,"user " + CILogonScopeHandler.CILogonClaims.IDP + " = " + idp);

        if (getCfg().getServer().equals("ldap.ncsa.illinois.edu") && !("https://idp.ncsa.illinois.edu/idp/shibboleth".equals(idp))) {
            return userInfo;
        }

        return super.process(userInfo, request, transaction);
    }
}
