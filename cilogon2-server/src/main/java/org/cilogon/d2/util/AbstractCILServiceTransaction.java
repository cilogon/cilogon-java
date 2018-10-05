package org.cilogon.d2.util;

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
}
