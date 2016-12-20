package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.BasicScopeHandler;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.LDAPScopeHandler;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Scopes;
import edu.uiuc.ncsa.security.oauth_2_0.UserInfo;
import edu.uiuc.ncsa.security.oauth_2_0.server.LDAPConfiguration;
import edu.uiuc.ncsa.security.oauth_2_0.server.OA2Claims;
import edu.uiuc.ncsa.security.oauth_2_0.server.UnsupportedScopeException;
import net.freeutils.charset.UTF7Charset;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserNotFoundException;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;

import static org.cilogon.oauth2.servlet.impl.CILogonScopeHandler.CILogonClaims.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/20/15 at  1:37 PM
 */
public class CILogonScopeHandler extends BasicScopeHandler implements OA2Scopes {
    LDAPConfiguration configuration;
    MyLoggingFacade logger;

    public CILogonScopeHandler(LDAPConfiguration configuration, MyLoggingFacade logger) {
        this.configuration = configuration;
        this.logger = logger;
    }

    public interface CILogonClaims extends OA2Claims {
        String IDP = "idp";
        String IDP_NAME = "idp_name";
        String EPPN = "eppn";
        String EPTID = "eptid";
        String OPENID = "openid";
        String OU = "ou";
        String AFFILIATION = "affiliation";

    }

    public LDAPScopeHandler getLdapScopeHandler() {
        if (ldapScopeHandler == null) {
            // *if* there is a global (i.e. default) server ldap configuration, create the handler.
            if (getOa2SE().getLdapConfiguration() == null) {
                LDAPConfiguration cfg = new LDAPConfiguration();
                cfg.setEnabled(false);
                ldapScopeHandler = new CILOA2LDAPScopeHandler(cfg, getOa2SE().getMyLogger());
            }else{
                ldapScopeHandler = new CILOA2LDAPScopeHandler(getOa2SE().getLdapConfiguration(), getOa2SE().getMyLogger());
            }
        }
        return ldapScopeHandler;
    }

    LDAPScopeHandler ldapScopeHandler = null;


    public CILogonOA2ServiceEnvironment getServiceEnvironment() {
        return (CILogonOA2ServiceEnvironment) getOa2SE();
    }


    UTF7Charset utf7Charset = new UTF7Charset();

    /**
     * ****************************
     * KEEP THIS!            *
     * *****************************
     */
    protected String convertFromUTF7ToUTF8(String utf7String) {
        // NOTE for right now this does nothing. Getting UTF-7 converted to UTF-8 was relatively easy,
        // but Tomcat does not play nicely with UTF-8 at all and requires a charset filter to be implemented
        // and specified. Look at http://stackoverflow.com/questions/138948/how-to-get-utf-8-working-in-java-webapps
        return utf7String;
        // KEEP THIS! What follows works great. When Tomcat can actually pass UTF-8 along, uncomment this.
       /* if (utf7String == null || utf7String.isEmpty()) {
            return utf7String;
        }
        utf7String = utf7String + "+MNUw6yAVMOo-";
        byte[] ba = utf7String.getBytes();
        String out = new String(ba, utf7Charset);
        System.err.println(getClass().getSimpleName() + ".convert: got " + utf7String + " returned " + out);
        return out;*/
    }

    @Override
    public UserInfo process(UserInfo userInfo, ServiceTransaction transaction) throws UnsupportedScopeException {
        if (getServiceEnvironment() == null) {
            throw new IllegalStateException("Error: this handler has not been initialized correctly. It must have a service environment specified");
        }
        OA2ServiceTransaction t = (OA2ServiceTransaction) transaction;

        User user = getServiceEnvironment().getUserStore().get(BasicIdentifier.newID(t.getUsername()));
        if (user == null) {
            throw new UserNotFoundException("No user was found with identifier \"" + t.getUsername() + "\"");
        }
        if (t.getScopes().contains(SCOPE_EMAIL)) {
            userInfo.setEmail(user.getEmail());
        }
        if (t.getScopes().contains(SCOPE_PROFILE)) {
            userInfo.setGiven_name(convertFromUTF7ToUTF8(user.getFirstName()));
            userInfo.setFamily_name(convertFromUTF7ToUTF8(user.getLastName()));
        }
        // Fixes CIL-210

        if (t.getScopes().contains(SCOPE_CILOGON_INFO)) {
            userInfo.getMap().put(IDP, user.getIdP());
            userInfo.getMap().put(IDP_NAME, user.getIDPName());
            if (user.hasEPPN()) {
                userInfo.getMap().put(EPPN, user.getePPN().getName());
            }
            if (user.hasEPTID()) {
                userInfo.getMap().put(EPTID, user.getePTID().getName());
            }
            if (user.hasOpenID()) {
                userInfo.getMap().put(OPENID, user.getOpenID().getName());
            }

            if (user.getOrganizationalUnit() != null) {
                userInfo.getMap().put(OU, user.getOrganizationalUnit());
            }
            if (user.getAffiliation() != null) {
                userInfo.getMap().put(AFFILIATION, user.getAffiliation());
            }
            if (user.getDisplayName() != null) {
                userInfo.getMap().put(NAME, user.getDisplayName());
            }

        }
    /*    if (getOa2SE().getLdapConfiguration().isEnabled()) {
            // Fixes CIL-303: Invoke the NCSA LDAP handler if the user is affiliated with NCSA.
            EduPersonPrincipleName eppn = user.getePPN();
            if (eppn.getName() == null || eppn.getName().length() == 0) {
                return userInfo; // nix to do.
            }
            if (eppn.getName().contains("ncsa.illinois.edu")) {
                getLdapScopeHandler().process(userInfo, transaction);
            }
        }*/
        if (getLdapScopeHandler() != null) {
            getLdapScopeHandler().process(userInfo, transaction);
        }

        return userInfo;
    }

}
