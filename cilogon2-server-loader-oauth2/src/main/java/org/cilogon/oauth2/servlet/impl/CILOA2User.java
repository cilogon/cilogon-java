package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.security.core.Identifier;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.util.SerialStrings;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/11/15 at  2:59 PM
 */
public class CILOA2User extends User {
    public CILOA2User(Identifier id, SerialStrings ss) {
        super(id, ss);
    }

    public CILOA2User(Identifier uid, String firstName, String lastName, String idP, String iDPName, String email, String serialString) {
        super(uid, firstName, lastName, idP, iDPName, email, serialString);
    }

    @Override
    public String toString() {
        String x= super.toString();
        if(x!= null){
            x = x.substring(0,x.length()-1); //drop final ]
        }
        x=x+",ou="+getOrganizationalUnit()+",affiliation="+getAffiliation()+",displayName="+getDisplayName()+"]";
        return x;
    }

}
