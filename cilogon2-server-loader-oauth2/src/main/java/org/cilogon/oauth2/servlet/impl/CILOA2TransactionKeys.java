package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.OA2TransactionKeys;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/8/15 at  8:30 AM
 */
public class CILOA2TransactionKeys extends OA2TransactionKeys {
    String LOA = "loa";
    public String LOA(String... x) {
          if (0 < x.length) LOA = x[0];
          return LOA;
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
     @Override
    public List<String> allKeys(){
         List<String> allKeys = super.allKeys();
        allKeys.add(LOA());
        allKeys.add(affiliation());
        allKeys.add(organizationalUnit());
        allKeys.add(displayName());
        return allKeys;
    }
}
