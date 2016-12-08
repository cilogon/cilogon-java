package org.cilogon.d2.util;

import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.DSTransactionKeys;

import java.util.List;

import static org.cilogon.d2.util.CILogonConstants.CILOGON_INFO;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/9/12 at  1:27 PM
 */
public class CILTransactionKeys extends DSTransactionKeys {
    public CILTransactionKeys() {
        super();
        myproxyUsername(CILOGON_INFO);
    }
    String affiliation="affiliation";
    public String affiliation(String... x) {
        if (0 < x.length) affiliation = x[0];
        return affiliation;
    }

    public String organizationalUnit(String... x) {
        if (0 < x.length) organizationalUnit = x[0];
        return organizationalUnit;
    }

       String organizationalUnit="ou";
       String displayName="display_name";

    public String displayName(String... x) {
        if (0 < x.length) displayName = x[0];
        return displayName;
    }


    String LOA = "loa";

    public String LOA(String... x) {
        if (0 < x.length) LOA = x[0];
        return LOA;
    }

    String tempCredSS = "temp_token_ss";
    String accessTokenSS = "access_token_ss";
    String complete = "complete";

    public String tempCredSS(String... x) {
        if (0 < x.length) tempCredSS = x[0];
        return tempCredSS;
    }

    public String accessTokenSS(String... x) {
        if (0 < x.length) accessTokenSS = x[0];
        return accessTokenSS;
    }


    public String complete(String... x) {
        if (0 < x.length) complete = x[0];
        return complete;
    }

    @Override
    public List<String> allKeys(){
        List<String> allKeys = super.allKeys();
        allKeys.add(affiliation());
        allKeys.add(organizationalUnit());
        allKeys.add(LOA());
        allKeys.add(tempCredSS());
        allKeys.add(accessTokenSS());
        allKeys.add(complete());
        allKeys.add(displayName());
        return allKeys;
    }
}
