package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.BasicClaimsSourceImpl;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.LDAPClaimsSource;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Scopes;
import edu.uiuc.ncsa.security.oauth_2_0.UserInfo;
import edu.uiuc.ncsa.security.oauth_2_0.server.claims.OA2Claims;
import edu.uiuc.ncsa.security.oauth_2_0.server.UnsupportedScopeException;
import edu.uiuc.ncsa.security.oauth_2_0.server.config.LDAPConfiguration;
import net.freeutils.charset.UTF7Charset;
import net.sf.json.JSONObject;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserNotFoundException;
import org.cilogon.d2.util.CILServiceTransactionInterface;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.storage.CILOA2ServiceTransaction;

import static org.cilogon.oauth2.servlet.impl.CILogonClaimSource.CILogonClaims.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/20/15 at  1:37 PM
 */
public class CILogonClaimSource extends BasicClaimsSourceImpl implements OA2Scopes {
    LDAPConfiguration configuration;
    MyLoggingFacade logger;

    public CILogonClaimSource(LDAPConfiguration configuration, MyLoggingFacade logger) {
        this.configuration = configuration;
        this.logger = logger;
    }

    public interface CILogonClaims extends OA2Claims {
        String IDP = "idp";
        String IDP_NAME = "idp_name";
        String EPPN = "eppn";
        String EPTID = "eptid";
        String OPENID = "openid";
        String OIDC = "oidc";
        String OU = "ou";
        String AFFILIATION = "affiliation";
        String AUTHENTICATION_CONTEXT_CLASS_REFERENCE = "acr";
        String CERT_SUBJECT_DN = "cert_subject_dn";
    }


    LDAPClaimsSource ldapClaimsSource = null;


    public CILogonOA2ServiceEnvironment getServiceEnvironment() {
        return (CILogonOA2ServiceEnvironment) getOa2SE();
    }


    UTF7Charset utf7Charset = new UTF7Charset();

    /**
     * *****************************
     * *
     * KEEP THIS!          *
     * *
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
        CILOA2ServiceTransaction t = (CILOA2ServiceTransaction) transaction;

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
            // CIL-371
            try {
                userInfo.getMap().put(CERT_SUBJECT_DN, user.getDN((CILServiceTransactionInterface) t, false));
            } catch (Throwable ttt) {
                // Should never happen, but just in case...
                logger.warn("Unable to determine user's DN for user " + user.getIdentifierString() + ". Message is " + ttt.getMessage());
            }
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

            if (user.hasOpenIDConnect()) {
                // Fixes CIL-365
                userInfo.getMap().put(OIDC, user.getOpenIDConnect().getName());
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
            // Fixes CIL-462
            String rawJSON = user.getAttr_json();
            if (rawJSON != null && !rawJSON.isEmpty()) {
                try {
                    JSONObject json = JSONObject.fromObject(rawJSON);
                    if (json.containsKey(AUTHENTICATION_CONTEXT_CLASS_REFERENCE)) {
                        userInfo.getMap().put(AUTHENTICATION_CONTEXT_CLASS_REFERENCE, json.getString(AUTHENTICATION_CONTEXT_CLASS_REFERENCE));
                    }
                } catch (Exception x) {
                    // rock on, no acr.
                }
            }
        }
        return userInfo;
    }

}
