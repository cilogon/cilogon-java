package org.cilogon.d2.impl;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import org.cilogon.d2.CILStoreTest;
import org.cilogon.d2.RemoteDBServiceTest;
import org.cilogon.d2.storage.*;
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
public abstract class UserStoreTest extends CILStoreTest {

    public UserStore getUserStore() throws Exception {
        return getCILStoreTestProvider().getUserStore();
    }

    public IdentityProviderStore getIDP() throws Exception {
        return getCILStoreTestProvider().getIDP();
    }

    @Override
    public void checkStoreClass() throws Exception {
        testClassAsignability(getUserStore());
    }

    @Test
    public void testNextValue() throws Exception {
        Incrementable incrementable = getCILStoreTestProvider().getSequence();
        long start = incrementable.nextValue();
        for (int i = 0; i < count; i++) {
            assert incrementable.nextValue() == start + i + 1;
        }
        System.out.println("\nnext value from incrementable \"" + getCILStoreTestProvider().getSequence().getClass().getSimpleName() + "\" is " + (start + count));
    }

   protected UserMultiKey createRU(String x){
       return RemoteDBServiceTest.createRU(x);
   }

    protected UserMultiKey createUMK(String x){
        return RemoteDBServiceTest.createUMK(x);
    }
    @Test
    public void testGetUser() throws Exception {
        try {
            getUserStore().get(createRU(getRandomString(10)), getRandomString(10));
            assert false : "Regression test failed: get User (remoteUser,idp) should throw an exception if the user does not exist";
        } catch (UserNotFoundException x) {
            assert true;
        }
        try {
            getUserStore().get(newID("foo:bar:" + getRandomString(10)));
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
    public void testOAUTH108() throws Exception {
        IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
        getIDP().register(idp);
        String random = getRandomString();
        UserMultiKey umk = createUMK(random);
        User bob = getUserStore().createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob", "smith", "bob@smith.com",
                "affiliation-" + random,"displayName-" + random,"orgUnit-" + random);
        bob.setePPN(umk.getEppn());
        assert checkNoNulls(umk.getEppn(), bob.getePPN()) : "Setter fails for ePPN";
        bob.setePTID(umk.getEptid());
        assert checkNoNulls(umk.getEptid(), bob.getePTID()) : "Setter fails for ePTID";
        bob.setOpenID(umk.getOpenID());
        assert checkNoNulls(umk.getOpenID(), bob.getOpenID()) : "Setter fails for Open ID";
        bob.setePTID(umk.getEptid());
        bob.setOpenID(umk.getOpenID());
        getUserStore().save(bob);
        User bob2 = getUserStore().get(bob.getIdentifier());
        assert checkNoNulls(bob.getePPN(), bob2.getePPN()) : "ePPN check fails. Expected \"" + bob.getePPN() + "\" and got \"" + bob2.getePPN() + "\"";
        assert checkNoNulls(bob.getePTID(), bob2.getePTID()) : "ePTID check fails. Expected \"" + bob.getePTID() + "\" and got \"" + bob2.getePTID() + "\"";
        assert checkNoNulls(bob.getOpenID(), bob2.getOpenID()) : "Open ID check fails. Expected \"" + bob.getOpenID() + "\" and got \"" + bob2.getOpenID() + "\"";
        // and just check that something else didn't break if there was a change
        assert bob.equals(bob2) : "Failed object equality check when testing for ePPN, ePTID and openID.";
    }

    protected User getSingleUser(UserMultiKey umk, IdentityProvider  idp) throws Exception {
        Collection<User> users  = getUserStore().get(umk, idp.getIdentifierString());
        assert users.size() == 1 : "Error: unexpected or no users found. ";
        return users.iterator().next();
    }
    @Test
    public void testUserCreate() throws Exception {

        UserStore userStore = getUserStore();
        User bob = null;
        IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
        String r = getRandomString();
        UserMultiKey umk =  createRU(r);
        getIDP().register(idp);
        bob = userStore.createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob", "smith", "bob@smith.com",
                "affiliation"+r, "display" + r, "urn:ou:" + r);


        User bob2 = userStore.get(bob.getIdentifier());

        assert bob2.equals(bob);
        User bob3 = getSingleUser(umk, idp);
        assert bob3.equals(bob);

        assert userStore.getUserID(umk, bob.getIdP()).equals(bob.getIdentifier());

        assert userStore.containsKey(bob.getIdentifier());
        // remove user not in the database. This should just return true since the user is no longer there.
        assert userStore.remove(BasicIdentifier.newID("mairzy:doats/and/dozy/doats")) == null;
        //assert userStore.remove("noRemoteUser", "noIdP");
        System.out.println("Default DN test: Does this look right? \"" + bob.getDN(null) + "\"");
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
    public void testMapInterface() throws Exception {
        UserStore userStore = getUserStore();
        Set<Identifier> keys = userStore.keySet();
        assert keys.size() == userStore.size();
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
    @Test
    public void testCIL68() throws Exception {
        User user = getUserStore().create(true);
        Identifier serialIdentifier = user.getSerialIdentifier();
        // issue is to update the user
        user.setFirstName("Bob");
        user.setUserMultiKey(createRU( "remote-" + getRandomString()));
        user.setIdP("idp-" + getRandomString());
        getUserStore().register(user);
        getUserStore().update(user);
        assert !serialIdentifier.equals(user.getSerialIdentifier());
        assert serialIdentifier.equals(user.getIdentifier()) : "After an update, the user id and serial id should not match, but they do.";
    }

    /**
      * See note in previous test. This does the same thing but with the save function, since that is
     * supposed to invoke update as needed.
      * @throws Exception
      */
     @Test
     public void testCIL68a() throws Exception {
         User user = getUserStore().create(true);
         Identifier serialIdentifier = user.getSerialIdentifier();
         // issue is to update the user
         user.setFirstName("Bob");
         user.setUserMultiKey(createRU( "remote-" + getRandomString()));
         user.setIdP("idp-" + getRandomString());
         getUserStore().register(user);
         getUserStore().save(user);
         assert !serialIdentifier.equals(user.getSerialIdentifier());
         assert serialIdentifier.equals(user.getIdentifier()) : "After an update, the user id and serial id should not match, but they do.";
     }

    @Test
    public void testOpenIdUser() throws Exception {
        UserStore pStore = getUserStore();
        User bob = null;
        String x = getRandomString();
        bob = pStore.createAndRegisterUser(createRU("remote-"+getRandomString()), "urn:identity/prov/" + getRandomString(), "idp-name:"+x,
                "first "+x, "last-"+x, x+"@email.com",
                "affiliation" + x, "display-" + x, "urn:ou" + x);
        assert true;
        System.out.println("OpenID DN Test: Does this look right? \"" + bob.getDN(null) + "\"");
    }

    @Test
    public void testLIGOUser() throws Exception {
        UserStore pStore = getUserStore();
        User bob = null;
        bob = pStore.createAndRegisterUser(createRU("LIGO-ePPN-" + getRandomString()), DNUtil.LIGO_IDP, "LIGO", "firstName", "lastName", "my-email@ligo.org",
                null,null,null);
        assert true;
        System.out.println("LIGO DN test: Does this look right? \"" + bob.getDN(null) + "\"");
    }

    @Test
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

    @Test
      public void testSerialStringIncrement() throws Exception {
        User user = getUserStore().create(true);
        String x = getRandomString();
        user.setFirstName("first-" + x);
        user.setLastName("last-" + x);
        user.setIdP("urn:idp:" + x);
        getUserStore().save(user);

        Identifier identifier = user.getSerialIdentifier();
       // so change a field and update the user. The serial string should be different
        String y = getRandomString();
        user.setFirstName("first-" + y);
        getUserStore().update(user);
        assert !user.getSerialIdentifier().equals(identifier) : "old serial id=" + identifier + ", new id=" + user.getSerialIdentifier() + ". These should not be equal.";

        // And now check that the save method actually does choose the right method, updating if the user exists.
        identifier = user.getSerialIdentifier();
        user.setLastName("last-" + y);
        getUserStore().save(user);
        assert !user.getSerialIdentifier().equals(identifier) : "old serial id=" + identifier + ", new id=" + user.getSerialIdentifier() + ". These should not be equal.";
      }

    /**
     * This tests that the incommon flag in the user object behaves correctly, viz., that it is not setm
     * then once set, attempts to change it fail.
     * @throws Exception
     */
      @Test
    public void testInCommon() throws Exception{
          IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
              getIDP().register(idp);
              String random = getRandomString();
              UserMultiKey umk = createUMK(random);
              User bob = getUserStore().createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob", "smith", "bob@smith.com",
                      "affiliation-" + random,"displayName-" + random,"orgUnit-" + random);
          // so at this point this user should not have this set:

          // A positive test.
          bob.setUseUSinDN(true);
          getUserStore().save(bob);
          bob = getUserStore().get(bob.getIdentifier());
          assert bob.isUseUSinDN() : "Error: Provenance of IDP set to true, but the stored value is false";

          IdentityProvider idp2 = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
              getIDP().register(idp2);
              random = getRandomString();
              umk = createUMK(random);
              User bob2 = getUserStore().createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob2", "smith", "bob2@smith.com",
                      "affiliation-" + random,"displayName-" + random,"orgUnit-" + random);

          bob2.setUseUSinDN(false);
          getUserStore().save(bob2);
          bob2 = getUserStore().get(bob2.getIdentifier());
          assert !bob2.isUseUSinDN() : "Error: Provenance of IDP set to false, but the stored value is true";

          // Clean up.
          getUserStore().remove(bob.getIdentifier());
          getUserStore().remove(bob2.getIdentifier());
      }

}
