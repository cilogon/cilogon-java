package org.cilogon.oauth2.servlet.claims;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.BasicClaimsSourceImpl;
import edu.uiuc.ncsa.oa4mp.delegation.oa2.OA2Scopes;
import edu.uiuc.ncsa.oa4mp.delegation.oa2.server.UnsupportedScopeException;
import edu.uiuc.ncsa.oa4mp.delegation.oa2.server.claims.OA2Claims;
import edu.uiuc.ncsa.oa4mp.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import net.freeutils.charset.UTF7Charset;
import net.sf.json.JSONObject;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2ServiceTransaction;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserNotFoundException;
import org.cilogon.oauth2.servlet.storage.transaction.AbstractCILServiceTransaction;

import javax.servlet.http.HttpServletRequest;

import static edu.uiuc.ncsa.oa4mp.delegation.oa2.server.claims.OA2Claims.PREFERRED_USERNAME;


/**
 * This actually introspects the user database and does not require an LDAP configuration.
 * Note that it is built into the server config for CILogon and is therefore never really
 * accessed directly.
 * <p>Created by Jeff Gaynor<br>
 * on 8/20/15 at  1:37 PM
 */
public class UserClaimSource extends BasicClaimsSourceImpl implements OA2Scopes {
    public UserClaimSource(QDLStem stem) {
        super(stem);
    }

    MyLoggingFacade logger;

    public UserClaimSource(MyLoggingFacade logger) {
        this.logger = logger;
    }


    String IDP = "idp";
    String IDP_NAME = "idp_name";
    String EPPN = "eppn";
    String EPTID = "eptid";
    String OPENID = "openid";
    String OIDC = "oidc";
    String PAIRWISE_ID = "pairwise_id";
    String SUBJECT_ID = "subject_id";
    String OU = "ou";
    String AFFILIATION = "affiliation";
    String CERT_SUBJECT_DN = "cert_subject_dn";
     String AUTHENTICATION_CLASS_REFERENCE = "acr";
     String AUTHENTICATION_METHOD_REFERENCE  = "amr";
    /*
     (ACR) and Authentication Method Reference (AMR)
     */

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

    public JSONObject process(JSONObject claims, ServiceTransaction transaction) throws UnsupportedScopeException {
        return process(claims, null, transaction);
    }

    boolean isEmpty(String x) {
        return x == null || x.isEmpty();
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
        JSONObject jsonAttributes = getJSONAttributes(user.getAttr_json());
        /*
        These scopes are honored as per CILogon's operational policies.
         */
        if (t.getScopes().contains(OPENID)) {
            // CIL-1667
            // if there is an ACR/AMR claim, return it here. The reason is that this is information about
            // how the user logged in and several institutions need to track this
            if (jsonAttributes.containsKey(AUTHENTICATION_CLASS_REFERENCE)) {
                claims.put(AUTHENTICATION_CLASS_REFERENCE, jsonAttributes.getString(AUTHENTICATION_CLASS_REFERENCE));
            }
            jsonAttributes.remove(AUTHENTICATION_CLASS_REFERENCE); // done with it.
            if (jsonAttributes.containsKey(AUTHENTICATION_METHOD_REFERENCE)) {
                claims.put(AUTHENTICATION_METHOD_REFERENCE, jsonAttributes.getString(AUTHENTICATION_METHOD_REFERENCE));
            }
            jsonAttributes.remove(AUTHENTICATION_METHOD_REFERENCE); // done with it.

        }
        if (t.getScopes().contains(SCOPE_EMAIL)) {

            if (!isEmpty(user.getEmail())) {
                // Even if the scope is requested, the user might not have one.
                claims.put(OA2Claims.EMAIL, user.getEmail());
            }
        }
        if (t.getScopes().contains(SCOPE_PROFILE)) {
            if (!isEmpty(user.getFirstName())) {
                claims.put(OA2Claims.GIVEN_NAME, convertFromUTF7ToUTF8(user.getFirstName()));
            }
            if (!isEmpty(user.getLastName())) {
                claims.put(OA2Claims.FAMILY_NAME, convertFromUTF7ToUTF8(user.getLastName()));
            }
            if (!isEmpty(user.getDisplayName())) {
                claims.put(OA2Claims.NAME, convertFromUTF7ToUTF8(user.getDisplayName()));
            }
            //Fixes CIL-1019
            if (!user.getAttr_json().isEmpty()) {
                DebugUtil.trace(this, "has a json attrib");
                JSONObject saml = JSONObject.fromObject(user.getAttr_json());
                if (saml.containsKey(PREFERRED_USERNAME)) {
                    claims.put(PREFERRED_USERNAME, saml.getString(PREFERRED_USERNAME));
                    DebugUtil.trace(this, "asserting " + PREFERRED_USERNAME + ":\"" + saml.getString(PREFERRED_USERNAME));
                }
            }
        }

        // Fixes CIL-210

        if (t.getScopes().contains(SCOPE_CILOGON_INFO)) {

            // CIL-371, CIL-444 cert subject does not contain the email, hence the "false" flag.
            try {
                if (user.canGetCert()) {
                    claims.put(CERT_SUBJECT_DN, user.getDN((AbstractCILServiceTransaction) t, false));
                }
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
            if (user.hasPairwiseID()) {
                // Fixes CIL-540
                claims.put(PAIRWISE_ID, user.getPairwiseID().getName());
            }
            if (user.hasSubjectID()) {
                // Fixes CIL-540
                claims.put(SUBJECT_ID, user.getSubjectID().getName());
            }
            if (user.hasOpenID()) {
                claims.put(OPENID, user.getOpenID().getName());
            }

            if (user.hasOpenIDConnect()) {
                // Fixes CIL-365
                claims.put(OIDC, user.getOpenIDConnect().getName());
            }


            if (!isEmpty(user.getOrganizationalUnit())) {
                claims.put(OU, user.getOrganizationalUnit());
            }
            if (!isEmpty(user.getAffiliation())) {
                claims.put(AFFILIATION, user.getAffiliation());
            }
            // Commenting this out for CIL-1411
        /*    if (!isEmpty(user.getDisplayName())) {
                claims.put(OA2Claims.NAME, user.getDisplayName());
            }*/
            for (Object key : jsonAttributes.keySet()) {
                // Fixes CIL-462
                // CIL-532 fix -- put in ALL of the values in the JSON attribute field and let the
                // configuration select them rather than having this in the code.
                if (!isEmpty(key.toString())) {
                    claims.put(key.toString(), jsonAttributes.getString(key.toString()));
                }
            }
        }
        return claims;
    }

    /**
     * Take the user's json_attr string and convert it into JSON for reference.
     * @param rawJSON
     * @return
     */
    protected JSONObject getJSONAttributes(String rawJSON) {
        if (!isEmpty(rawJSON)) {
            try {
                return JSONObject.fromObject(rawJSON);
            } catch (Exception x) {
                ServletDebugUtil.trace(this, "Error: was not able to parse the attr_json field into elements. Message=\"" + x.getMessage() + "\". Processing will continue...");
                // rock on, no recognizable json.
            }
        }
        return new JSONObject();
    }

    @Override
    public QDLStem toQDL() {
        QDLStem stem = super.toQDL();
        stem.put(CILCSConstants.CS_DEFAULT_TYPE, CILCSConstants.CS_TYPE_USER);
        return stem;
    }

    @Override
    public boolean isRunOnlyAtAuthorization() {
        return false;
    }

}
