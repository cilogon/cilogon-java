package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.delegation.token.AuthorizationGrant;
import org.cilogon.d2.util.CILServiceTransactionInterface;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/12/15 at  12:21 PM
 */
public class CILOA2ServiceTransaction extends OA2ServiceTransaction implements CILServiceTransactionInterface {
    public CILOA2ServiceTransaction(AuthorizationGrant ag) {
        super(ag);
    }

    public CILOA2ServiceTransaction(Identifier identifier) {
        super(identifier);
    }

    public String getLoa() {
        return loa;
    }

    public void setLoa(String loa) {
        this.loa = loa;
    }

    String loa;

    String affiliation;

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    String displayName;
    String organizationalUnit;

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    @Override
    public void setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    @Override
    public String toString() {
        String x = super.toString();
           x = x.substring(0,x.length()-1);
           x = x + ",OU="+getOrganizationalUnit();
           x = x + ",display name="+getDisplayName();
           x = x + ",affiliation="+getAffiliation();
           x = x + ",LOA="+getLoa() + "]";
           return x;
    }
}
