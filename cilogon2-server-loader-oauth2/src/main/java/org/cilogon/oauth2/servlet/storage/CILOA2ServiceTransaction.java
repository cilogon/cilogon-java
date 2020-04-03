package org.cilogon.oauth2.servlet.storage;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.delegation.token.AuthorizationGrant;
import org.cilogon.d2.util.AbstractCILServiceTransaction;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/12/15 at  12:21 PM
 */
public class CILOA2ServiceTransaction extends OA2ServiceTransaction implements AbstractCILServiceTransaction {
    public CILOA2ServiceTransaction(AuthorizationGrant ag) {
        super(ag);
    }

    public CILOA2ServiceTransaction(Identifier identifier) {
        super(identifier);
    }

    String affiliation;
    String displayName;
    String organizationalUnit;
    String loa;

    public String getLoa() {
        return loa;
    }

    public void setLoa(String loa) {
        this.loa = loa;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    @Override
    public String toString() {
        String x = super.toString();
        x = x.substring(0, x.length() - 1);
        x = x + ",OU=" + getOrganizationalUnit();
        x = x + ",display name=" + getDisplayName();
        x = x + ",affiliation=" + getAffiliation();
        x = x + ",LOA=" + getLoa() + "]";
        return x;
    }
}
