package org.cilogon.oauth2.servlet.util;

import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.TransactionConverter;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.Client;
import edu.uiuc.ncsa.oa4mp.delegation.common.token.TokenForge;
import edu.uiuc.ncsa.oa4mp.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.data.ConversionMap;
import edu.uiuc.ncsa.security.storage.data.SerializationKeys;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/16/12 at  1:23 PM
 */
public  class CILServiceTransactionConverter<V extends CILogonServiceTransaction> extends TransactionConverter<V> {
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
       /* if (st.hasAccessToken() && st.getAccessToken() instanceof OA1AccessTokenImpl) {
            ((OA1AccessTokenImpl) st.getAccessToken()).setSharedSecret(tokenSS);
        }*/
        tokenSS = map.getString(getCILK().tempCredSS());
/*
        if (st.hasAuthorizationGrant() && st.getAuthorizationGrant() instanceof OA1AuthorizationGrantImpl) {
            ((OA1AuthorizationGrantImpl) st.getAuthorizationGrant()).setSharedSecret(tokenSS);
        }
*/
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
        if (t.getAuthorizationGrant() == null) {
            map.put(getCILK().tempCredSS(), null);

        } else {
/*
            if (t.getAuthorizationGrant() instanceof OA1AuthorizationGrantImpl) {
                map.put(getCILK().tempCredSS(), ((OA1AuthorizationGrantImpl)t.getAuthorizationGrant()).getSharedSecret());
            }
*/
        }
        if(t.getAccessToken() == null){
            map.put(getCILK().accessTokenSS(), null );
        }else{
/*
            if(t.getAccessToken() instanceof OA1AccessTokenImpl) {
                map.put(getCILK().accessTokenSS(), ((OA1AccessTokenImpl)t.getAccessToken()).getSharedSecret());
            }
*/
        }
        map.put(getCILK().complete(), t.isComplete());
        map.put(getCILK().LOA(), t.getLoa());
        map.put(getCILK().affiliation(), t.getAffiliation());
        map.put(getCILK().displayName(), t.getDisplayName());
        map.put(getCILK().organizationalUnit(), t.getOrganizationalUnit());
    }
}
