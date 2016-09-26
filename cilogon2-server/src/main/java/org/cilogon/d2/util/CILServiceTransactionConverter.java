package org.cilogon.d2.util;

import edu.uiuc.ncsa.myproxy.oa4mp.server.util.TransactionConverter;
import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.storage.data.ConversionMap;
import edu.uiuc.ncsa.security.storage.data.SerializationKeys;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/16/12 at  1:23 PM
 */
public class CILServiceTransactionConverter<V extends CILogonServiceTransaction> extends TransactionConverter<V> {
    public CILServiceTransactionConverter(
            IdentifiableProvider<V> identifiableProvider,
            TokenForge tokenForge,
            ClientStore<? extends Client> cs) {
        super(new CILTransactionKeys(), identifiableProvider, tokenForge, cs);
    }

    public CILServiceTransactionConverter(SerializationKeys keys,
                                          IdentifiableProviderImpl<V> identifiableProvider,
                                          TokenForge tokenForge,
                                          ClientStore<? extends Client> cs) {
        super(keys, identifiableProvider, tokenForge, cs);
    }

    protected CILTransactionKeys getCILK() {
        return (CILTransactionKeys) keys;
    }

    @Override
    public V fromMap(ConversionMap<String, Object> map, V v) {
        V st = super.fromMap(map, v);
        String tokenSS = map.getString(getCILK().accessTokenSS());
        if (st.hasAccessToken()) {
            st.getAccessToken().setSharedSecret(tokenSS);
        }
        tokenSS = map.getString(getCILK().tempCredSS());
        if (st.hasAuthorizationGrant()) {
            st.getAuthorizationGrant().setSharedSecret(tokenSS);
        }
        st.setComplete(map.getBoolean(getCILK().complete()));
        st.setLoa(map.getString(getCILK().LOA()));
        st.setCallback(map.getURI(getCILK().callbackUri()));
        st.setAffiliation(map.getString(getCILK().affiliation()));
        st.setDisplayName(map.getString(getCILK().displayName()));
        st.setOrganizationalUnit(map.getString(getCILK().organizationalUnit()));
        return st;
    }

    @Override
    public void toMap(V t, ConversionMap<String, Object> map) {
        super.toMap(t, map);
        map.put(getCILK().tempCredSS(), t.getAuthorizationGrant() == null ? null : t.getAuthorizationGrant().getSharedSecret());
        map.put(getCILK().accessTokenSS(), t.getAccessToken() == null ? null : t.getAccessToken().getSharedSecret());
        map.put(getCILK().complete(), t.isComplete());
        map.put(getCILK().LOA(), t.getLoa());
        map.put(getCILK().affiliation(), t.getAffiliation());
        map.put(getCILK().displayName(), t.getDisplayName());
        map.put(getCILK().organizationalUnit(), t.getOrganizationalUnit());
    }
}
