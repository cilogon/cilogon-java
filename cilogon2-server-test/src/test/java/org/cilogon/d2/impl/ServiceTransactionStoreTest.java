package org.cilogon.d2.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.StoreProvidable;
import edu.uiuc.ncsa.myproxy.oa4mp.TransactionStoreTest;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.util.CILogonServiceTransaction;
import org.junit.Test;

import java.net.URI;

import static org.cilogon.d2.RemoteDBServiceTest.createRU;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/17/12 at  11:54 AM
 */
public abstract class ServiceTransactionStoreTest extends TransactionStoreTest implements StoreProvidable {
      public CILTestStoreProvider getCILStoreTestProvider(){
            return (CILTestStoreProvider) getTSProvider();
        }

    @Test
    public void testTransaction() throws Exception {
        CILogonServiceTransaction st = (CILogonServiceTransaction) getStore().create();
        st.setCallback(URI.create("http://callback"));
        st.setMyproxyUsername(getRandomString(256));
        st.setAuthorizationGrant(getTSProvider().getTokenForge().getAuthorizationGrant());
        st.setAuthGrantValid(false);

        getStore().save(st);
        assert getStore().containsKey(st.getIdentifier());
        assert st.equals(getStore().get(st.getIdentifier()));
        assert st.equals(getStore().get(st.getAuthorizationGrant()));

        // now emulate doing oauth type transactions with it.
        // First leg sets the verifier and user

        String r = getRandomString(12);
        User user = getCILStoreTestProvider().getUserStore().createAndRegisterUser(createRU("urn:remoteUser/" + r),
                "urn:test:idp/" + r, "idp display name", "first-Name", "last-Name", r + "@foo.com",
                "archmage", "first and last", "my:fake:university");

        st.setVerifier(getTSProvider().getTokenForge().getVerifier());
        st.setUsername(user.getIdentifierString());
        st.setLifetime(12 * 60 * 60 * 1000); // set for 12 hours
        String loa = "http://incommonfederation.org/assurance/silver";
        st.setLoa(loa);
        getStore().save(st);

        assert st.equals(getStore().get(st.getVerifier()));
        // next leg creates the access tokens and invalidates the temp credentials
        st.setAccessToken(getTSProvider().getTokenForge().getAccessToken());
        st.setAuthGrantValid(false);
        st.setAccessTokenValid(true);
        getStore().save(st);
        assert st.equals(getStore().get(st.getIdentifier()));
        assert st.equals(getStore().get(st.getAccessToken()));
        CILogonServiceTransaction zzz = (CILogonServiceTransaction) getStore().get(st.getIdentifier());
        assert loa.equals(zzz.getLoa());
        st.setAccessTokenValid(false);
        getStore().save(st);
        assert st.equals(getStore().get(st.getIdentifier()));
        //and we're done
        getStore().remove(st.getIdentifier());
        assert !getStore().containsKey(st.getIdentifier());
    }

}
