package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.security.core.Identifier;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/3/15 at  3:22 PM
 */
public interface AbstractCILServiceTransaction {


   String getLoa();
    void setLoa(String loa);

    String getAffiliation();
    void setAffiliation(String affiliation);

    String getDisplayName();
    void setDisplayName(String displayName);

    String getOrganizationalUnit();
    void setOrganizationalUnit(String organizationalUnit);

    Identifier getUserUID();
    void setUserUID(Identifier userUID);
}
