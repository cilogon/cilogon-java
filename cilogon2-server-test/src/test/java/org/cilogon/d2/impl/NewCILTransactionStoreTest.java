package org.cilogon.d2.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.NewTransactionTest;
import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPServiceTransaction;
import edu.uiuc.ncsa.security.delegation.storage.TransactionStore;
import org.cilogon.d2.CILTestStoreProviderI2;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.util.CILServiceTransactionInterface;

import java.net.URI;

import static org.cilogon.d2.RemoteDBServiceTest.createRU;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/18/18 at  5:26 PM
 */
public class NewCILTransactionStoreTest extends NewTransactionTest {

    public void testTransaction(CILTestStoreProviderI2 provider) throws Exception {
        TransactionStore store =  provider.getTransactionStore();
          OA4MPServiceTransaction st = (OA4MPServiceTransaction) store.create();
          st.setCallback(URI.create("http://callback"));
          st.setMyproxyUsername(getRandomString(256));
          st.setAuthorizationGrant(provider.getTokenForge().getAuthorizationGrant());
          st.setAuthGrantValid(false);

          store.save(st);
          assert store.containsKey(st.getIdentifier());
          assert st.equals(store.get(st.getIdentifier()));
          assert st.equals(store.get(st.getAuthorizationGrant()));

          // now emulate doing oauth type transactions with it.
          // First leg sets the verifier and user

          String r = getRandomString(12);
          User user = provider.getUserStore().createAndRegisterUser(createRU("urn:remoteUser/" + r),
                  "urn:test:idp/" + r, "idp display name", "first-Name", "last-Name", r + "@foo.com",
                  "archmage", "first and last", "my:fake:university");

          st.setVerifier(provider.getTokenForge().getVerifier());
          st.setUsername(user.getIdentifierString());
          st.setLifetime(12 * 60 * 60 * 1000); // set for 12 hours
          String loa = "http://incommonfederation.org/assurance/silver";
          // CILogon specific test
          ((CILServiceTransactionInterface)st).setLoa(loa);
          store.save(st);

          assert st.equals(store.get(st.getVerifier()));
          // next leg creates the access tokens and invalidates the temp credentials
          st.setAccessToken(provider.getTokenForge().getAccessToken());
          st.setAuthGrantValid(false);
          st.setAccessTokenValid(true);
          store.save(st);
          assert st.equals(store.get(st.getIdentifier()));
          assert st.equals(store.get(st.getAccessToken()));
          CILServiceTransactionInterface zzz = (CILServiceTransactionInterface) store.get(st.getIdentifier());
          assert loa.equals(zzz.getLoa());
          st.setAccessTokenValid(false);
          store.save(st);
          assert st.equals(store.get(st.getIdentifier()));
          //and we're done
          store.remove(st.getIdentifier());
          assert !store.containsKey(st.getIdentifier());
      }
}
