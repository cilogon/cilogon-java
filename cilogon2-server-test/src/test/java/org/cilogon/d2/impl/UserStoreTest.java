package org.cilogon.d2.impl;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.util.TestBase;
import org.cilogon.d2.CILTestStoreProviderI2;
import org.cilogon.d2.RemoteDBServiceTest;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.storage.*;
import org.cilogon.d2.storage.impl.memorystore.MemoryUserStore;
import org.cilogon.d2.storage.provider.UserIdentifierProvider;
import org.cilogon.d2.storage.provider.UserProvider;
import org.cilogon.d2.util.DNUtil;
import org.cilogon.d2.util.Incrementable;
import org.junit.Test;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static edu.uiuc.ncsa.security.core.util.BeanUtils.checkNoNulls;
import static org.cilogon.d2.ServiceTestUtils.getSerialStrings;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Mar 12, 2010 at  6:41:46 PM
 */
public  class UserStoreTest extends TestBase {
    public void testAll() throws Exception{
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMemoryStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getFsStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMySQLStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getPgStoreProvider());
    }

    public void doTests(CILTestStoreProviderI2 provider) throws Exception{
        testFNAL(provider.getUserStore());
        testNextValue(provider.getSequence());
        testSerialStringIncrement(provider.getUserStore());
        testMapInterface(provider.getUserStore());
        testGetUser(provider.getUserStore());
        testUserCreate(provider.getUserStore(), provider.getIDP());
        testCIL68(provider.getUserStore());
        testCIL68a(provider.getUserStore());
        testOAUTH108(provider.getUserStore(), provider.getIDP());
        testInCommon(provider.getUserStore(), provider.getIDP());
        testLIGOUser(provider.getUserStore());
        testOpenIdUser(provider.getUserStore());
    }


    public void testNextValue(Incrementable incrementable) throws Exception {
        //Incrementable incrementable = getCILStoreTestProvider().getSequence();
        long start = incrementable.nextValue();
        for (int i = 0; i < count; i++) {
            assert incrementable.nextValue() == start + i + 1;
        }
        System.out.println("\nnext value from incrementable \"" + incrementable.getClass().getSimpleName() + "\" is " + (start + count));
    }

   protected UserMultiKey createRU(String x){
       return RemoteDBServiceTest.createRU(x);
   }

    protected UserMultiKey createUMK(String x){
        return RemoteDBServiceTest.createUMK(x);
    }
    @Test
    public void testGetUser(UserStore userStore) throws Exception {
        try {
            userStore.get(createRU(getRandomString(10)), getRandomString(10));
            assert false : "Regression test failed: get User (remoteUser,idp) should throw an exception if the user does not exist";
        } catch (UserNotFoundException x) {
            assert true;
        }
        try {
            userStore.get(newID("foo:bar:" + getRandomString(10)));
            assert false : "Regression test failed: getUser(Identifier) should throw a UserNotFoundException if user does not exist";
        } catch (UserNotFoundException x) {
            assert true;
        }
    }

    /**
     * Test to show OAUTH-108 is resolved.
     * @throws Exception
     */
    @Test
    public void testOAUTH108(UserStore userStore, IdentityProviderStore identityProviderStore) throws Exception {
        IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
        identityProviderStore.register(idp);
        String random = getRandomString();
        UserMultiKey umk = createUMK(random);
        User bob = userStore.createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob", "smith", "bob@smith.com",
                "affiliation-" + random, "displayName-" + random, "orgUnit-" + random);
        bob.setePPN(umk.getEppn());
        assert checkNoNulls(umk.getEppn(), bob.getePPN()) : "Setter fails for ePPN";
        bob.setePTID(umk.getEptid());
        assert checkNoNulls(umk.getEptid(), bob.getePTID()) : "Setter fails for ePTID";
        bob.setOpenID(umk.getOpenID());
        assert checkNoNulls(umk.getOpenID(), bob.getOpenID()) : "Setter fails for Open ID";
        bob.setePTID(umk.getEptid());
        bob.setOpenID(umk.getOpenID());
        userStore.save(bob);
        User bob2 = userStore.get(bob.getIdentifier());
        assert checkNoNulls(bob.getePPN(), bob2.getePPN()) : "ePPN check fails. Expected \"" + bob.getePPN() + "\" and got \"" + bob2.getePPN() + "\"";
        assert checkNoNulls(bob.getePTID(), bob2.getePTID()) : "ePTID check fails. Expected \"" + bob.getePTID() + "\" and got \"" + bob2.getePTID() + "\"";
        assert checkNoNulls(bob.getOpenID(), bob2.getOpenID()) : "Open ID check fails. Expected \"" + bob.getOpenID() + "\" and got \"" + bob2.getOpenID() + "\"";
        // and just check that something else didn't break if there was a change
        assert bob.equals(bob2) : "Failed object equality check when testing for ePPN, ePTID and openID.";
    }

    protected User getSingleUser(UserStore userStore, UserMultiKey umk, IdentityProvider  idp) throws Exception {
        Collection<User> users  = userStore.get(umk, idp.getIdentifierString());
        assert users.size() == 1 : "Error: unexpected or no users found. ";
        return users.iterator().next();
    }
    public void testUserCreate(UserStore userStore, IdentityProviderStore identityProviderStore) throws Exception {

        User bob = null;
        IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
        String r = getRandomString();
        UserMultiKey umk =  createRU(r);
        identityProviderStore.register(idp);
        bob = userStore.createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob", "smith", "bob@smith.com",
                "affiliation"+r, "display" + r, "urn:ou:" + r);


        User bob2 = userStore.get(bob.getIdentifier());

        assert bob2.equals(bob);
        User bob3 = getSingleUser(userStore, umk, idp);
        assert bob3.equals(bob);

        assert userStore.getUserID(umk, bob.getIdP()).equals(bob.getIdentifier());

        assert userStore.containsKey(bob.getIdentifier());
        // remove user not in the database. This should just return true since the user is no longer there.
        assert userStore.remove(BasicIdentifier.newID("mairzy:doats/and/dozy/doats")) == null;
        //assert userStore.remove("noRemoteUser", "noIdP");
        System.out.println("Default DN test: Does this look right? \"" + bob.getDN(null, true) + "\"");
        bob.setFirstName("Rinaldo");
        bob.setLastName("Sarducci");
        try {
            userStore.register(bob);
        } catch (GeneralException x) {
            assert true; // since bob is already in the store.
        }
        userStore.update(bob);
        assert userStore.containsKey(bob.getIdentifier());

    }

    /**
     * The user store now implements the map interface. These calls are simply passed
     * to the original implementation. This test just shows that they pass off the
     * calls correctly.
     *
     * @throws Exception
     */
    @Test
    public void testMapInterface(UserStore userStore) throws Exception {
        Set<Identifier> keys = userStore.keySet();
        assert keys.size() == userStore.size() : "Got " + keys.size() + " keys for the user store, but the reported size of the store is " + userStore.size();
        if (keys.size() == 0) {
            return;
        }
        Identifier testKey = keys.iterator().next();
        assert userStore.containsKey(testKey);
        User user = userStore.get(testKey);
        assert userStore.containsValue(user);
        Set<Map.Entry<Identifier, User>> m = userStore.entrySet();
        assert m.size() == keys.size();
    }

    /**
     * Update call in the store must update the serial identifier in the argument.
     * Can't claim what the value will be, just that is must be different from what was
     * there to start with (probably will be incremented by one, but in a real life
     * use case, other user creation requests might come in between the time the user
     * is made and updated, so there is no guarantee that the values will be consecutive.)
     *
     * @throws Exception
     */
    public void testCIL68(UserStore userStore) throws Exception {
        User user = userStore.create(true);
        Identifier serialIdentifier = user.getSerialIdentifier();
        // issue is to update the user
        user.setFirstName("Bob");
        user.setUserMultiKey(createRU( "remote-" + getRandomString()));
        user.setIdP("idp-" + getRandomString());
        userStore.register(user);
        userStore.update(user);
        assert !serialIdentifier.equals(user.getSerialIdentifier());
        assert serialIdentifier.equals(user.getIdentifier()) : "After an update, the user id and serial id should not match, but they do.";
    }

    /**
      * See note in previous test. This does the same thing but with the save function, since that is
     * supposed to invoke update as needed.
      * @throws Exception
      */
     public void testCIL68a(UserStore userStore) throws Exception {
         User user = userStore.create(true);
         Identifier serialIdentifier = user.getSerialIdentifier();
         // issue is to update the user
         user.setFirstName("Bob");
         user.setUserMultiKey(createRU( "remote-" + getRandomString()));
         user.setIdP("idp-" + getRandomString());
         userStore.register(user);
         userStore.save(user);
         assert !serialIdentifier.equals(user.getSerialIdentifier());
         assert serialIdentifier.equals(user.getIdentifier()) : "After an update, the user id and serial id should not match, but they do.";
     }

    public void testOpenIdUser(UserStore pStore) throws Exception {
        User bob = null;
        String x = getRandomString();
        bob = pStore.createAndRegisterUser(createRU("remote-"+getRandomString()), "urn:identity/prov/" + getRandomString(), "idp-name:"+x,
                "first "+x, "last-"+x, x+"@email.com",
                "affiliation" + x, "display-" + x, "urn:ou" + x);
        assert true;
        System.out.println("OpenID DN Test: Does this look right? \"" + bob.getDN(null, true) + "\"");
    }

    public void testLIGOUser(UserStore pStore) throws Exception {
        User bob = null;
        bob = pStore.createAndRegisterUser(createRU("LIGO-ePPN-" + getRandomString()), DNUtil.LIGO_IDP, "LIGO", "firstName", "lastName", "my-email@ligo.org",
                null,null,null);
        assert true;
        System.out.println("LIGO DN test: Does this look right? \"" + bob.getDN(null,true) + "\"");
    }

    public void testSerialStrings() throws Exception {
        // This test doesn't save the user and only tests the machinery for working with serial strings and identifiers
        User user = new User(BasicIdentifier.newID("test:foo://user/"), getSerialStrings());
        user.setSerialString("A123");
        try {
            user.setSerialString("A234");
            assert true; // we're good
        } catch (Exception x) {
            assert false : "Error: the serial string was reset and this should not cause an exception.";
        }
        try {
            getSerialStrings().fromSerialString("Q333");
            assert true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            user = new User(BasicIdentifier.newID("test:foo://user/"), getSerialStrings());
            user.setSerialString("http://www.foo.fnord");
            assert false : "Error: was able to set the serial string to an illegal value";
        } catch (IllegalArgumentException e) {
            assert true;
        }
    }

    /**
     * Test using a real user and his data
     * @param userStore
     * @throws Exception
     */
      public void testFNAL(UserStore userStore) throws Exception{
          /*
           User[
           uid="http://cilogon.org/serverA/users/18351",
           key=UserMultiKey[
              remoteUser=https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!CJtWNa2SKrwXb0NHzPGlWwr7uTE=,
              eptid=https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!6258095E940C4172B7E4096063C3C447A91B0F14,
              eppn=kreymer@fnal.gov],
              IdP=https://idp.fnal.gov/idp/shibboleth",
              first name="Arthur",
              last name="Kreymer",
              email="kreymer@fnal.gov",
              idp display="Fermi National Accelerator Laboratory",
              US IDP?="true",
              ou=Robots:minos27.fnal.gov:cron,
              affiliation=null,
              displayName=Arthur E Kreymer",
              attr_json=null]
           */
          RemoteUserName remoteUser = new RemoteUserName("https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!CJtWNa2SKrwXb0NHzPGlWwr7uTE=");
          EduPersonTargetedID eptid = new EduPersonTargetedID("https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!6258095E940C4172B7E4096063C3C447A91B0F14");
          EduPersonPrincipleName eppn = new EduPersonPrincipleName("kreymer@fnal.gov");
          UserMultiKey umk = new UserMultiKey(remoteUser,eppn,eptid,null,null);



          User user = userStore.createAndRegisterUser(
                  umk,
                  "https://idp.fnal.gov/idp/shibboleth", //idp
                  "Fermi National Accelerator Laboratory", //ipd display name
                  "Arthur", //first name
                  "Kreymer", // last name
                  "kreymer@fnal.gov", // email
                  null, // affiliation
                  "Arthur E Kreymer", // display name
                  "Robots:minos27.fnal.gov:cron" // organizational unit
          );
          user.setAttr_json(null);
          user.setUseUSinDN(true);

          System.out.println("============ START FNAL TEST");
          DNUtil.setComputeFNAL(false);
          System.out.println("-> computeFNAL = false");
          System.out.println("get email=true: "+ user.getDN(null, true));
          System.out.println("get email=false: "+ user.getDN(null, false));
          DNUtil.setComputeFNAL(true);
          System.out.println("-> computeFNAL = true");
          System.out.println("get email=true: "+ user.getDN(null, true));
          System.out.println("get email=false: "+ user.getDN(null, false));
          System.out.println("============ END FNAL TEST");
          // clean up
          userStore.remove(user.getIdentifier());
      }
      public void testSerialStringIncrement(UserStore userStore) throws Exception {
        User user = userStore.create(true);
        String x = getRandomString();
        user.setFirstName("first-" + x);
        user.setLastName("last-" + x);
        user.setIdP("urn:idp:" + x);
        userStore.save(user);


        Identifier identifier = user.getSerialIdentifier();
       // so change a field and update the user. The serial string should be different
        String y = getRandomString();
        user.setFirstName("first-" + y);
        userStore.update(user);
        assert !user.getSerialIdentifier().equals(identifier) : "old serial id=" + identifier + ", new id=" + user.getSerialIdentifier() + ". These should not be equal.";

        // And now check that the save method actually does choose the right method, updating if the user exists.
        identifier = user.getSerialIdentifier();
        user.setLastName("last-" + y);
        userStore.save(user);
        assert !user.getSerialIdentifier().equals(identifier) : "old serial id=" + identifier + ", new id=" + user.getSerialIdentifier() + ". These should not be equal.";
        userStore.remove(user.getIdentifier());
      }

    /**
     * This tests that the incommon flag in the user object behaves correctly, viz., that it is not setm
     * then once set, attempts to change it fail.
     * @throws Exception
     */
      @Test
    public void testInCommon(UserStore userStore, IdentityProviderStore identityProviderStore) throws Exception{
          IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
              identityProviderStore.register(idp);
              String random = getRandomString();
              UserMultiKey umk = createUMK(random);
              User bob = userStore.createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob", "smith", "bob@smith.com",
                      "affiliation-" + random, "displayName-" + random, "orgUnit-" + random);
          // so at this point this user should not have this set:

          // A positive test.
          bob.setUseUSinDN(true);
          userStore.save(bob);
          bob = userStore.get(bob.getIdentifier());
          assert bob.isUseUSinDN() : "Error: Provenance of IDP set to true, but the stored value is false";

          IdentityProvider idp2 = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
              identityProviderStore.register(idp2);
              random = getRandomString();
              umk = createUMK(random);
              User bob2 = userStore.createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob2", "smith", "bob2@smith.com",
                      "affiliation-" + random, "displayName-" + random, "orgUnit-" + random);

          bob2.setUseUSinDN(false);
          userStore.save(bob2);
          bob2 = userStore.get(bob2.getIdentifier());
          assert !bob2.isUseUSinDN() : "Error: Provenance of IDP set to false, but the stored value is true";

          // Clean up.
          identityProviderStore.remove(idp);
          identityProviderStore.remove(idp2);
          userStore.remove(bob.getIdentifier());
          userStore.remove(bob2.getIdentifier());
      }

    /**
     * This tests for a badly behaving incrementable against an in-memory user store.
     * @throws Exception
     */
    @Test
      public void testBadIncrementable() throws Exception {
          MemoryUserStore store = new MemoryUserStore(new UserProvider(new MyUIDProvider(), null));
          String r = getRandomString();

          // It has been saved as part of the registration process and is in the store.
          // Now create another one that is not. Here the user provider can only generate a single user id, mimicking the failure of
          // an SQL store or a file store to increment correctly.
          r = getRandomString();

          try {
            User  user = store.createAndRegisterUser(createRU("remote-" + r),
                      "idp:/" + r,
                      "idp-name-" + r,
                      "first-" + r,
                      "last-" + r,
                      "foo@bar." + r,
                      "affiliation" + r,
                      "displayName" + r,
                      "urn:ou:" + r);
              store.save(user);
              assert false : "Was able to create another user in the store with the same id.";
          } catch (InvalidUserIdException ix) {
              assert true;
          }
      }

      public class MyUIDProvider extends UserIdentifierProvider {
          public MyUIDProvider() {
              super(new BadIncrementable(), "fake:server");
          }
      }

      /**
       * A test class that does not increment right so we get bad identifiers from it.
       */
      public class BadIncrementable implements Incrementable {
          long onlyValue = 2L;

          @Override
          public boolean createNew(long initialValue) {
              return false;
          }

          @Override
          public long nextValue() {
              return onlyValue;
          }

          @Override
          public boolean destroy() {
              return false;
          }

          @Override
          public boolean init() {
              return false;
          }

          @Override
          public boolean createNew() {
              return false;
          }

          @Override
          public boolean isCreated() {
              return false;
          }

          @Override
          public boolean isInitialized() {
              return false;
          }

          @Override
          public boolean isDestroyed() {
              return false;
          }
      }
}
