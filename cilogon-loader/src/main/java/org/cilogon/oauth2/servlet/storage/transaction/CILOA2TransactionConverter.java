package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.transactions.OA2TConverter;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.Client;
import edu.uiuc.ncsa.oa4mp.delegation.common.token.TokenForge;
import edu.uiuc.ncsa.oa4mp.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.storage.data.ConversionMap;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/12/15 at  12:03 PM
 */
public class CILOA2TransactionConverter<V extends CILOA2ServiceTransaction> extends OA2TConverter<V> {
    public CILOA2TransactionConverter(CILOA2TransactionKeys keys,
                                      IdentifiableProvider<V> identifiableProvider,
                                      TokenForge tokenForge,
                                      ClientStore<? extends Client> cs) {
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
        st.setUserUID(BasicIdentifier.newID(map.getString(getTK().userUID())));
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
        if(t.getUserUID() != null){
            map.put(getTK().userUID(), t.getUserUID());
        }
    }
}
