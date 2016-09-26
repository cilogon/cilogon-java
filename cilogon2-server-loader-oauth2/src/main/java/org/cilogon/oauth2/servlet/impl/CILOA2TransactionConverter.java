package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.OA2TConverter;
import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.storage.data.ConversionMap;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/12/15 at  12:03 PM
 */
public class CILOA2TransactionConverter<V extends CILOA2ServiceTransaction> extends OA2TConverter<V> {
    public CILOA2TransactionConverter(CILOA2TransactionKeys keys, IdentifiableProvider<V> identifiableProvider, TokenForge tokenForge, ClientStore<? extends Client> cs) {
        super(keys, identifiableProvider, tokenForge, cs);
    }
    CILOA2TransactionKeys getTK(){
        return (CILOA2TransactionKeys) getTCK();
    }
    @Override
    public V fromMap(ConversionMap<String, Object> map, V v) {
        V st = super.fromMap(map, v);
        st.setOrganizationalUnit(map.getString(getTK().organizationalUnit()));
        st.setAffiliation(map.getString(getTK().affiliation()));
        st.setDisplayName(map.getString(getTK().displayName()));
        st.setLoa(map.getString(getTK().LOA()));
        return st;
    }

    @Override
    public void toMap(V t, ConversionMap<String, Object> map) {
        super.toMap(t, map);
        if(t.getAffiliation()!=null){
        map.put(getTK().affiliation(), t.getAffiliation());}
        if(t.getDisplayName() != null){
            map.put(getTK().displayName(), t.getDisplayName());
        }
        if(t.getLoa()!=null){
            map.put(getTK().LOA(), t.getLoa());
        }
        if(t.getOrganizationalUnit()!= null){
            map.put(getTK().organizationalUnit(), t.getOrganizationalUnit());
        }
    }
}
