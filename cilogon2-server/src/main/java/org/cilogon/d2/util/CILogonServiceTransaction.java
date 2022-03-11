package org.cilogon.d2.util;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPServiceTransaction;
import edu.uiuc.ncsa.security.core.Identifier;

import static edu.uiuc.ncsa.security.core.util.BeanUtils.checkEquals;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/14/12 at  3:38 PM
 */
public class CILogonServiceTransaction extends OA4MPServiceTransaction implements AbstractCILServiceTransaction {
    public CILogonServiceTransaction(Identifier identifier) {
        super(identifier);
    }

    static final long serialVersionUID = 0xcafed00d3L;
    boolean complete;
    String loa;

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getLoa() {
        return loa;
    }

    public void setLoa(String loa) {
        this.loa = loa;
    }

    String affiliation;
    String organizationalUnit;
    String displayName;

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
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof CILogonServiceTransaction)) return false;
        CILogonServiceTransaction st = (CILogonServiceTransaction) obj;
        if (!checkEquals(getCallback(), st.getCallback())) return false;
        if (getLifetime() != st.getLifetime()) return false;
        if (!checkEquals(getLoa(), st.getLoa())) return false;
        return true;
    }

    // Next are not implemented in OAUth1
    @Override
    public Identifier getUserUID() {
        return null;
    }

    @Override
    public void setUserUID(Identifier userUID) {

    }
}
