package org.cilogon.oauth2.servlet.claims;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2SE;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.BasicClaimsSourceImpl;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.GroupElement;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.Groups;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.oauth_2_0.server.UnsupportedScopeException;
import edu.uiuc.ncsa.security.oauth_2_0.server.claims.OA2Claims;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.cilogon.d2.storage.User;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;

import javax.servlet.http.HttpServletRequest;
import java.util.StringTokenizer;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/10/18 at  8:15 AM
 */
public class JSONAttrbuteClaimSource extends BasicClaimsSourceImpl {
    public JSONAttrbuteClaimSource(OA2SE oa2SE) {
        super(oa2SE);
    }

    public JSONAttrbuteClaimSource() {
    }

    public String SHIBBOLETH_MEMBER_OF_KEY = "member_of";
    public static String SHIBBOLETH_LIST_DELIMITER = ";";

    @Override
    public JSONObject process(JSONObject claims, ServiceTransaction transaction) throws UnsupportedScopeException {
        return oldProcess(claims, transaction);
    }

    /**
     * The assumption is that all attributes will be JSONArrays since SAML supports multi-valued attributes.
     *
     * @param claims
     * @param transaction
     * @return
     * @throws UnsupportedScopeException
     */
    protected JSONObject newProcess(JSONObject claims, ServiceTransaction transaction) throws UnsupportedScopeException {
        // the default group handler will create a group structure from a JSON array.
        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getOa2SE();
        User user = se.getUserStore().get(BasicIdentifier.newID(transaction.getUsername()));
        if (user == null) {
            throw new NFWException("Error: user not found for identifier \"" + transaction.getUsername() + "\"");
        }
        if (user.getAttr_json() == null || user.getAttr_json().isEmpty()) {
            return claims; // basically there were no specific Shib headers that were passed in for this user, which is just fine.
        }
        JSONObject saml = JSONObject.fromObject(user.getAttr_json());
        for (Object key0 : saml.keySet()) {
            String key = key0.toString();
            String value = saml.getString(key);
            if (key.equals(SHIBBOLETH_MEMBER_OF_KEY)) {
                JSONArray array = JSONArray.fromObject(value);
                Groups g = getGroupHandler().parse(array);
                claims.put(OA2Claims.IS_MEMBER_OF, g);
                // parse into a group structure
            } else {
                claims.put(key.toString(), value);
            }
        }
        return claims;
    }

    /**
     * As of next release (4.1) we should be getting SAML attributes that have been parsed into JSON, so we do not
     * need to do the parsing ourselves.
     *
     * @param claims
     * @param transaction
     * @return
     * @throws UnsupportedScopeException
     */
    protected JSONObject oldProcess(JSONObject claims, ServiceTransaction transaction) throws UnsupportedScopeException {
        // In the case of CILogon, the username on the transaction is the unique user id in the database, so get the user
        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getOa2SE();
        User user = se.getUserStore().get(BasicIdentifier.newID(transaction.getUsername()));
        if (user == null) {
            throw new NFWException("Error: user not found for identifier \"" + transaction.getUsername() + "\"");
        }
        if (user.getAttr_json() == null || user.getAttr_json().isEmpty()) {
            return claims; // basically there were no specific Shib headers that were passed in for this user, which is just fine.
        }
        JSONObject saml = JSONObject.fromObject(user.getAttr_json());
        for (Object key0 : saml.keySet()) {
            String key = key0.toString();
            //keys should be strings!! But just in case, make sure it is one
            // TODO - stick this in a proper GroupHandler one of these days...
            if (key.equals(SHIBBOLETH_MEMBER_OF_KEY)) {
                Groups group = new Groups();
                String rawGroups = saml.getString(key);
                StringTokenizer st = new StringTokenizer(rawGroups, SHIBBOLETH_LIST_DELIMITER, false);
                while (st.hasMoreElements()) {
                    GroupElement groupElement = new GroupElement(st.nextToken());
                    group.put(groupElement);
                }
                claims.put(OA2Claims.IS_MEMBER_OF, group);
                // parse into a group structure
            } else {
                // parse into a JSON array since SAML support multiple values for any attribute,
                String values = saml.getString(key);
                if (values.indexOf(SHIBBOLETH_LIST_DELIMITER) < 0) {
                    // A single value.
                    claims.put(key.toString(), values);
                } else {
                    // split it up.
                    StringTokenizer st = new StringTokenizer(values, SHIBBOLETH_LIST_DELIMITER, false);
                    JSONArray array = new JSONArray();
                    while (st.hasMoreElements()) {
                        array.add(st.nextToken());
                    }
                    claims.put(key.toString(), array);
                }
            }

        }
        return claims;
    }

    @Override
    public JSONObject process(JSONObject claims, HttpServletRequest request, ServiceTransaction transaction) throws UnsupportedScopeException {
        if (request != null) {
            throw new IllegalArgumentException("Error: this is not supported for servlet requests, ");
        }
        return process(claims, transaction);
    }

    @Override
    public boolean isRunAtAuthorization() {
        return false;
    }
}
