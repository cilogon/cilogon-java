package org.cilogon.d2.impl.postgres;

import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.impl.ServiceTransactionStoreTest;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.impl.sql.CILSQLTransactionStore;
import org.cilogon.d2.util.CILogonServiceTransaction;
import org.junit.Test;

import java.net.URI;

import static org.cilogon.d2.RemoteDBServiceTest.createRU;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/17/12 at  12:40 PM
 */
public class PGTransactionStoreTest extends ServiceTransactionStoreTest {
    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getPgStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return CILSQLTransactionStore.class;
    }

    @Test
      public void testOldTransaction() throws Exception {
        // Tests that an older CILogon transaction is stored and retried correctly.
          CILogonServiceTransaction st = (CILogonServiceTransaction) getStore().create();
          st.setCallback(URI.create("http://callback"));
          st.setMyproxyUsername(getRandomString(256));
          st.setAuthorizationGrant(getTSProvider().getTokenForge().getAuthorizationGrant());
          st.setAuthGrantValid(false);
     //     st.setPortalID(URI.create("urn:test:portalid/" + System.nanoTime()));
          URI successUri = URI.create("urn:test:success/uri");
          URI failureUri = URI.create("urn:test:failure/uri");
          String portalName = "portal test name " + getRandomString(16);

     //     st.setSuccessUri(successUri);
      //    st.setFailureUri(failureUri);
       //   st.setPortalName(portalName);

          getStore().save(st);
          assert getStore().containsKey(st.getIdentifier());
          assert st.equals(getStore().get(st.getIdentifier()));
          assert st.equals(getStore().get(st.getAuthorizationGrant()));

          // now emulate doing oauth type transactions with it.
          // First leg sets the verifier and user

          String r = getRandomString(12);
          User user = getCILStoreTestProvider().getUserStore().createAndRegisterUser(createRU("urn:remoteUser/" + r),
                  "urn:test:idp/" + r, "idp display name", "first-Name", "last-Name", r + "@foo.com",
                  "affiliation" + r,
                  "displayName" + r,
                  "urn:ou:" + r
                  );

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
