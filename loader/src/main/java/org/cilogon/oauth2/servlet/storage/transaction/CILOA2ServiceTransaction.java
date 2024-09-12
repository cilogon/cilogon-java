package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.security.core.Identifier;
import org.cilogon.oauth2.servlet.CILConfigToCS;
import org.oa4mp.delegation.common.token.AuthorizationGrant;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2ServiceTransaction;
import org.oa4mp.server.loader.qdl.claims.ConfigtoCS;

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
    Identifier userUID;
    @Override
    public Identifier getUserUID() {
        return userUID;
    }

    @Override
    public void setUserUID(Identifier userUID) {
         this.userUID = userUID;
    }

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

    @Override
    public ConfigtoCS getConfigToCS() {
        if(configtoCS == null){
            configtoCS = new CILConfigToCS();
        }
        return configtoCS;
    }
}
