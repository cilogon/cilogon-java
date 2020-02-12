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
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.cilogon.d2.storage.User;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;

import javax.servlet.http.HttpServletRequest;
import java.util.StringTokenizer;

/**
 * This will read SAML attributes that are sent in the header from the IDP then stored in an attribute that
 * is send. At the right time, this attribute is read and parsed into information about the user and
 * returned as a set of claims. NOTE that this is created by introspection (hence the no arg constructor)
 * and hence never seem to be used in the code base. Several clients, however, require it.
 * <p>Created by Jeff Gaynor<br>
 * on 7/10/18 at  8:15 AM
 */
public class SAMLAttrbuteClaimSource extends BasicClaimsSourceImpl {
    public SAMLAttrbuteClaimSource(OA2SE oa2SE) {
        super(oa2SE);
    }

    public SAMLAttrbuteClaimSource() {
        super();
    }

    public String SHIBBOLETH_MEMBER_OF_KEY = "member_of";
    public static String SHIBBOLETH_LIST_DELIMITER = ";";

    @Override
    public JSONObject process(JSONObject claims, ServiceTransaction transaction) throws UnsupportedScopeException {
        return oldProcess(claims, transaction);
    }

    /**
     * The assumption is that all attributes will be JSONArrays since SAML supports multi-valued attributes.
     *  <br/>Update: CIL-532 requires eduPersonEntitlement support. After discussions, we have decided not to
     *  have pre-parsed JSON, but stick with the {@link #oldProcess(JSONObject, ServiceTransaction)}. This code
     *  should be kept for a bit in case we decide to change our minds, since it is debugged and works, but it
     *  may ultimately go away.
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
     * need to do the parsing ourselves. Note that these attributes have been sent over out of band and
     * stashed in a {@link User} attribute named {@link User#getAttr_json()}, since the assumption is that
     * this servlet is access through Apache (or some other web server) and therefore cannot have direct access to
     * the secure headers.
     *
     * @param claims
     * @param transaction
     * @return
     * @throws UnsupportedScopeException
     */
    protected JSONObject oldProcess(JSONObject claims, ServiceTransaction transaction) throws UnsupportedScopeException {
        // In the case of CILogon, the username on the transaction is the unique user id in the database, so get the user
        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getOa2SE();
        //   ServletDebugUtil.trace(this, ".oldProcess: username=" + transaction.getUsername());
        ServletDebugUtil.trace(this, ".oldProcess: service env=" + se);
        User user = se.getUserStore().get(BasicIdentifier.newID(transaction.getUsername()));
        if (user == null) {
            throw new NFWException("Error: user not found for identifier \"" + transaction.getUsername() + "\"");
        }
        if (user.getAttr_json() == null || user.getAttr_json().isEmpty()) {
            ServletDebugUtil.trace(this,".oldProcess: No SAML attributes found");
            return claims; // basically there were no specific Shib headers that were passed in for this user, which is just fine.
        }
        ServletDebugUtil.trace(this,".oldProcess: attr_json = \"" + user.getAttr_json() + "\"");

        JSONObject saml = JSONObject.fromObject(user.getAttr_json());
        ServletDebugUtil.trace(this,".oldProcess: attr_json parses as JSONObject to = \"" + saml);
        if (saml == null) {
            ServletDebugUtil.trace(this, ".oldProcess: No SAML attributes found for user " + transaction.getUsername() + ". Skipping.");
            return claims;
        }
        return getJsonObject(claims, saml);
    }

    private JSONObject getJsonObject(JSONObject claims, JSONObject saml) {

        for (Object key0 : saml.keySet()) {
            String key = key0.toString();
            //keys should be strings!! But just in case, make sure it is one
            // TODO - stick this in a proper GroupHandler one of these days...
            ServletDebugUtil.trace(this,".getJsonObject: key = \"" + key + "\"");
            if (key.equals(SHIBBOLETH_MEMBER_OF_KEY)) {

                Groups group = new Groups();
                String rawGroups = saml.getString(key);
                StringTokenizer st = new StringTokenizer(rawGroups, SHIBBOLETH_LIST_DELIMITER, false);
                while (st.hasMoreElements()) {
                    GroupElement groupElement = new GroupElement(st.nextToken());
                    group.put(groupElement);
                }
                claims.put(OA2Claims.IS_MEMBER_OF, group.toJSON()); // or the JSON object tries to turn it into something weird.
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
            throw new IllegalArgumentException("Error: this is not supported for servlet requests.");
        }
        return process(claims, transaction);
    }

    @Override
    public boolean isRunAtAuthorization() {
        return false;
    }

    @Override
    public String toString() {
        return "SAMLAttrbuteClaimSource{" +
                "SHIBBOLETH_MEMBER_OF_KEY='" + SHIBBOLETH_MEMBER_OF_KEY + '\'' +
                ",runAtAuthTime=" + isRunAtAuthorization() +
                ", service env =" + getOa2SE() +
                '}';
    }

    public static void main(String[] arg) {
        // just a test on the old form attributes from the CILogon service. Make sure they format right.
        // String x = "{\"member_of\":\"c13b7ba3-b038-4abb-b062-4491d1f9f12b;09895d05-1b79-4529-9f9d-9367752a1d0a\",\"acr\":\"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport\"}";

        try {
              test1();
              test2();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    protected static void test2() throws Exception{
        String x = "{\"acr\":\"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport\",\"entitlement\":\"urn:mace:dir:entitlement:common-lib-terms\"}";
        JSONObject saml = JSONObject.fromObject(x);
        SAMLAttrbuteClaimSource samlAttrbuteClaimSource = new SAMLAttrbuteClaimSource(null);
        //JSONObject claims = samlAttrbuteClaimSource.process(new JSONObject(), null);
        JSONObject claims = new JSONObject();
        claims = samlAttrbuteClaimSource.getJsonObject(claims, saml);
        System.out.println("\n ----- test2 ----- \n" + claims);
    }
    protected static void test1() throws Exception{
        JSONObject saml = new JSONObject();
              saml.put("member_of", "c13b7ba3-b038-4abb-b062-4491d1f9f12b;09895d05-1b79-4529-9f9d-9367752a1d0a");
              saml.put("acr", "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
              saml.put("entitlement", "urn:mace:exampleIdP.org:demoservice:demo-admin");

              String rawGroups = saml.getString("member_of");
              Groups group = new Groups();

              StringTokenizer st = new StringTokenizer(rawGroups, SHIBBOLETH_LIST_DELIMITER, false);
              while (st.hasMoreElements()) {
                  GroupElement groupElement = new GroupElement(st.nextToken());
                  group.put(groupElement);
              }
              JSONObject foo = new JSONObject();
              System.out.println(group.toJSON());
              foo.put("isMemberOf", group.toJSON());
              System.out.println("\nFrom group processor:");
              System.out.println(foo);
              System.out.println("\nFrom attr_json processor:");
              SAMLAttrbuteClaimSource samlAttrbuteClaimSource = new SAMLAttrbuteClaimSource(null);

              System.out.println(samlAttrbuteClaimSource.getJsonObject(new JSONObject(), saml).toString(2));
    }
}
