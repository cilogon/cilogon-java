package org.cilogon.oauth2.servlet.claims;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.BasicClaimsSourceImpl;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Scopes;
import edu.uiuc.ncsa.security.oauth_2_0.server.UnsupportedScopeException;
import edu.uiuc.ncsa.security.oauth_2_0.server.claims.OA2Claims;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import net.freeutils.charset.UTF7Charset;
import net.sf.json.JSONObject;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserNotFoundException;
import org.cilogon.d2.util.AbstractCILServiceTransaction;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.storage.CILOA2ServiceTransaction;

import javax.servlet.http.HttpServletRequest;

import static org.cilogon.oauth2.servlet.claims.UserClaimSource.CILogonClaims.*;

/**
 * This actually introspects the user database and does not require an LDAP configuration.
 * <p>Created by Jeff Gaynor<br>
 * on 8/20/15 at  1:37 PM
 */
public class UserClaimSource extends BasicClaimsSourceImpl implements OA2Scopes {
    MyLoggingFacade logger;

    public UserClaimSource(MyLoggingFacade logger) {
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


    public CILogonOA2ServiceEnvironment getServiceEnvironment() {
        return (CILogonOA2ServiceEnvironment) getOa2SE();
    }


    UTF7Charset utf7Charset = new UTF7Charset();

    /**
     * *****************************
     * *
     *         KEEP THIS!          *
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

    public JSONObject process(JSONObject claims, ServiceTransaction transaction) throws UnsupportedScopeException {
        return process(claims, null, transaction);
    }

    @Override
    protected JSONObject realProcessing(JSONObject claims, HttpServletRequest request, ServiceTransaction transaction) throws UnsupportedScopeException {
        if (getServiceEnvironment() == null) {
            throw new IllegalStateException("Error: this handler has not been initialized correctly. It must have a service environment specified");
        }
        CILOA2ServiceTransaction t = (CILOA2ServiceTransaction) transaction;
        User user = getServiceEnvironment().getUserStore().get(BasicIdentifier.newID(t.getUsername()));
        if (user == null) {
            throw new UserNotFoundException("No user was found with identifier \"" + t.getUsername() + "\"");
        }
        /*
        These scopes are honored as per CILogon's operational policies.
         */
        if (t.getScopes().contains(SCOPE_EMAIL)) {
            claims.put(OA2Claims.EMAIL, user.getEmail());
        }
        if (t.getScopes().contains(SCOPE_PROFILE)) {
            claims.put(OA2Claims.GIVEN_NAME,convertFromUTF7ToUTF8(user.getFirstName()));
            claims.put(OA2Claims.FAMILY_NAME,convertFromUTF7ToUTF8(user.getLastName()));
        }

        // Fixes CIL-210

        if (t.getScopes().contains(SCOPE_CILOGON_INFO)) {

            // CIL-371, CIL-444 cert subject does not contain the email, hence the "false" flag.
            try {
                claims.put(CERT_SUBJECT_DN, user.getDN((AbstractCILServiceTransaction) t, false));
            } catch (Throwable ttt) {
                // Should never happen, but just in case...
                logger.info("Unable to determine user's DN for user " + user.getIdentifierString() + ". Message is " + ttt.getMessage());
            }
            claims.put(IDP, user.getIdP());
            claims.put(IDP_NAME, user.getIDPName());
            if (user.hasEPPN()) {
                claims.put(EPPN, user.getePPN().getName());
            }
            if (user.hasEPTID()) {
                claims.put(EPTID, user.getePTID().getName());
            }
            if (user.hasOpenID()) {
                claims.put(OPENID, user.getOpenID().getName());
            }

            if (user.hasOpenIDConnect()) {
                // Fixes CIL-365
                claims.put(OIDC, user.getOpenIDConnect().getName());
            }

            if (user.getOrganizationalUnit() != null) {
                claims.put(OU, user.getOrganizationalUnit());
            }
            if (user.getAffiliation() != null) {
                claims.put(AFFILIATION, user.getAffiliation());
            }
            if (user.getDisplayName() != null) {
                claims.put(OA2Claims.NAME, user.getDisplayName());
            }
            // Fixes CIL-462
            String rawJSON = user.getAttr_json();
            if (rawJSON != null && !rawJSON.isEmpty()) {
                try {
                    JSONObject json = JSONObject.fromObject(rawJSON);
                    // CIL-532 fix -- put in ALL of the values in the JSON attribute field and let the
                    // configuration select them rather than having this in the code.
                    for(Object key : json.keySet()){
                        claims.put(key.toString(), json.getString(key.toString()));
                    }
/*                   Old way was to select only the acr explicitly.
                    if (json.containsKey(AUTHENTICATION_CONTEXT_CLASS_REFERENCE)) {
                        claims.put(AUTHENTICATION_CONTEXT_CLASS_REFERENCE, json.getString(AUTHENTICATION_CONTEXT_CLASS_REFERENCE));
                    }
*/
                } catch (Exception x) {
                    ServletDebugUtil.trace(this,"Error: was not able to parse the attr_json field into elements. Message=\"" + x.getMessage() + "\". Processing will continue...");
                    // rock on, no acr.
                }
            }
        }
        return claims;
    }

    @Override
    public boolean isRunAtAuthorization() {
        return false;
    }
}
