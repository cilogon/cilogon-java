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
import net.sf.json.JSONObject;
import org.cilogon.d2.storage.User;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;

import javax.servlet.http.HttpServletRequest;
import java.util.StringTokenizer;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/10/18 at  8:15 AM
 */
public class JSONAttrbuteClaimSource extends BasicClaimsSourceImpl{
    public JSONAttrbuteClaimSource(OA2SE oa2SE) {
        super(oa2SE);
    }

    public JSONAttrbuteClaimSource() {
    }

    public String SHIBBOLETH_MEMBER_OF = "member_of";

    @Override
    public JSONObject process(JSONObject claims, ServiceTransaction transaction) throws UnsupportedScopeException {
        // In the case of CILogon, the username on the transaction is the unique user id in the database, so get the user
        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getOa2SE();
        User user = se.getUserStore().get(BasicIdentifier.newID(transaction.getUsername())) ;
         if(user == null){
             throw new NFWException("Error: user not found for identifier \"" + transaction.getUsername() + "\"");
         }
        if(user.getAttr_json() == null || user.getAttr_json().isEmpty()){
            return claims; // basically there were no specific Shib headers that were passed in for this user, which is just fine.
        }
        JSONObject saml = JSONObject.fromObject(user.getAttr_json());
        for(Object key0 : saml.keySet()){
             String key = key0.toString();
           //keys should be strings!! But just in case, make sure it is one
           // TODO - stick this in a proper GroupHandler one of these days...
            if(key.equals(SHIBBOLETH_MEMBER_OF)){
                Groups group = new Groups();
                String rawGroups = saml.getString(key);
                StringTokenizer st = new StringTokenizer(rawGroups,";", false);
                while(st.hasMoreElements()){
                    GroupElement groupElement = new GroupElement(st.nextToken());
                    group.put(groupElement);
                }
             claims.put(OA2Claims.IS_MEMBER_OF, group);
                // parse into a group structure
            }else{
                claims.put(key.toString(), saml.getJSONObject(key.toString()));
            }

        }
        return claims;
    }

    @Override
    public JSONObject process(JSONObject claims, HttpServletRequest request, ServiceTransaction transaction) throws UnsupportedScopeException {
        if(request != null) {
            throw new IllegalArgumentException("Error: this is not supported for servlet requests, ");
        }
        return process(claims, transaction);
    }

    @Override
    public boolean isRunAtAuthorization() {
        return false;
    }
}
