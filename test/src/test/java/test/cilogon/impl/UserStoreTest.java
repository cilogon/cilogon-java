package test.cilogon.impl;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.util.TestBase;
import net.freeutils.charset.UTF7Charset;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProvider;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.user.*;
import org.cilogon.oauth2.servlet.util.DNUtil;
import org.cilogon.oauth2.servlet.util.Incrementable;
import org.junit.Test;
import test.cilogon.CILTestStoreProviderI2;
import test.cilogon.RemoteDBServiceTest;
import test.cilogon.ServiceTestUtils;

import java.math.BigInteger;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static edu.uiuc.ncsa.security.core.util.BeanUtils.checkNoNulls;
import static test.cilogon.ServiceTestUtils.getSerialStrings;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Mar 12, 2010 at  6:41:46 PM
 */
public class UserStoreTest extends TestBase {
    public void testAll() throws Exception {
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMySQLStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMemoryStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getFsStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getDerbyStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getPgStoreProvider());
    }


    public void doTests(CILTestStoreProviderI2 provider) throws Exception {
        testNoNewSerialStringOnUserGet(provider.getUserStore(),
                provider.getSequence(),
                provider.getIDP());
        testFNAL(provider.getUserStore());
        testUTF7(provider.getUserStore());
        testUTF7RegressionTest(provider.getUserStore());
        testNextValue(provider.getSequence());
        doSerialStringIncrement(provider.getUserStore());
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

    /**
     * regression test for CIL-1030: Getting a user repeatedly should never cause the {@link Incrementable}
     * to change.
     * @param userStore
     * @param incrementable
     * @throws Exception
     */
    public void testNoNewSerialStringOnUserGet(UserStore userStore,
                                               Incrementable incrementable,
                                               IdentityProviderStore identityProviderStore) throws Exception {
        User user = null;
         IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
         String r = getRandomString();
         UserMultiID umk = createRU(r);
         identityProviderStore.register(idp);
         user = userStore.createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob", "smith", "bob@smith.com",
                 "affiliation" + r, "display" + r, "urn:ou:" + r);
        Identifier serialID = user.getSerialIdentifier();
        System.out.println("starting user ss: " + serialID);
         for (int i = 0; i < 2*count; i++) {
             userStore.get(user.getIdentifier());
         }
        System.out.println("Post get\n   user ss: " + user.getSerialIdentifier());
         // repeat for updates.
        for (int i = 0; i < 2*count; i++) {
            userStore.update(user);
        }
       System.out.println("Post update\n   user ss: " + user.getSerialIdentifier());
       // repeat for save
        for (int i = 0; i < 2*count; i++) {
            userStore.save(user);
        }
       System.out.println("Post update\n   user ss: " + user.getSerialIdentifier());
       long originalSS = grabSerialNumber(serialID);
       long nextIncr = incrementable.nextValue();
        //assert originalSS == nextIncr-1 : "expected " + originalSS + " but got " + (nextIncr - 1);
        System.out.println( "expected " + originalSS + " but got " + (nextIncr - 1));
     }
      protected long grabSerialNumber(Identifier serialID){
          String p = serialID.getUri().getPath();
          p = p.substring(p.lastIndexOf("/")+1);
          return Long.parseLong(p);

      }
    public void testNextValue(Incrementable incrementable) throws Exception {
        //Incrementable incrementable = getCILStoreTestProvider().getSequence();
        long start = incrementable.nextValue();
        for (int i = 0; i < count; i++) {
            assert incrementable.nextValue() == start + i + 1;
        }
        System.out.println("\nnext value from incrementable \"" + incrementable.getClass().getSimpleName() + "\" is " + (start + count));
    }

    protected UserMultiID createRU(String x) {
        return RemoteDBServiceTest.createRU(x);
    }

    protected UserMultiID createUMK(String x) {
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
     *
     * @throws Exception
     */
    @Test
    public void testOAUTH108(UserStore userStore, IdentityProviderStore identityProviderStore) throws Exception {
        IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
        identityProviderStore.register(idp);
        String random = getRandomString();
        UserMultiID umk = createUMK(random);
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

    protected User getSingleUser(UserStore userStore, UserMultiID umk, IdentityProvider idp) throws Exception {
        Collection<User> users = userStore.get(umk, idp.getIdentifierString());
        assert users.size() == 1 : "Error: unexpected or no users found. ";
        return users.iterator().next();
    }

    public void testUserCreate(UserStore userStore, IdentityProviderStore identityProviderStore) throws Exception {

        User bob = null;
        IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
        String r = getRandomString();
        UserMultiID umk = createRU(r);
        identityProviderStore.register(idp);
        bob = userStore.createAndRegisterUser(umk, idp.getIdentifierString(), "idp display name", "bob", "smith", "bob@smith.com",
                "affiliation" + r, "display" + r, "urn:ou:" + r);


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
        if (keys.size() == 0) {
            return;
        }
        Identifier testKey = keys.iterator().next();
        assert userStore.containsKey(testKey);
        User user = userStore.get(testKey);
        assert userStore.containsValue(user);
        Set<Map.Entry<Identifier, User>> m = userStore.entrySet();
        assert m.size() == keys.size();
        assert keys.size() == userStore.size() : "Got " + keys.size() + " keys for the user store of type \"" +
                userStore.getClass().getSimpleName() + "\", " +
                "but the reported size of the store is " + userStore.size() + ". This probably means you have extra files in your user directory if its a file store...";

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
        user.setUserMultiKey(createRU("remote-" + getRandomString()));
        user.setIdP("idp-" + getRandomString());
        userStore.register(user);
        userStore.update(user); // update and trigger serial string change
        assert !serialIdentifier.equals(user.getSerialIdentifier());
        assert serialIdentifier.equals(user.getIdentifier()) : "After an update, the user id and serial id should not match, but they do.";
    }

    /**
     * See note in previous test. This does the same thing but with the save function, since that is
     * supposed to invoke update as needed. The save function does not update the serial string!
     *
     * @throws Exception
     */
    public void testCIL68a(UserStore userStore) throws Exception {
        User user = userStore.create(true);
        Identifier serialIdentifier = user.getSerialIdentifier();
        System.out.println("testCIL68a serial id:" + serialIdentifier);

        // issue is to update the user
        user.setFirstName("Bob");
        user.setUserMultiKey(createRU("remote-" + getRandomString()));
        user.setIdP("idp-" + getRandomString());
        userStore.register(user);
        // update does not keep the serial string, save would but we don't want that.
        System.out.println("testCIL68a user before save:\n" + user.toString(1));
        userStore.save(user); // update, no serial string change.
        System.out.println("testCIL68a user after save:\n" + user.toString(1));
        System.out.println("testCIL68a user after sID:\n" + user.getSerialIdentifier());
        assert serialIdentifier.equals(user.getSerialIdentifier()): "After a save, the user id and serial id should match, but they do not.";
        userStore.remove(user);
    }

    public void testOpenIdUser(UserStore pStore) throws Exception {
        User bob = null;
        String x = getRandomString();
        bob = pStore.createAndRegisterUser(createRU("remote-" + getRandomString()), "urn:identity/prov/" + getRandomString(), "idp-name:" + x,
                "first " + x, "last-" + x, x + "@email.com",
                "affiliation" + x, "display-" + x, "urn:ou" + x);
        assert true;
        System.out.println("OpenID DN Test: Does this look right? \"" + bob.getDN(null, true) + "\"");
        pStore.remove(bob);
    }

    public void testLIGOUser(UserStore userStore) throws Exception {
        User vulnavia = null;
        vulnavia = userStore.createAndRegisterUser(createRU("LIGO-remote-user-" + getRandomString()),
                "https://login2.ligo.org/idp/shibboleth", //idp
                "LIGO", // IDP display name
                "Vulnavia", // first name
                "Phibes", // last name
                "v.phibes@ligo.org", //email
                null, // affiliation
                "Vulnavia Phibes", // display name
                null // organizational unit -- null for people, set only for robots
        );
        System.out.println("\n============> LIGO People (with remote user)");
        System.out.println("get email = false\": " + vulnavia.getDN(null, false) + "\"");
        System.out.println("get email = true\": " + vulnavia.getDN(null, true) + "\"");
        userStore.remove(vulnavia);


        UserMultiID umk = new UserMultiID(
                new RemoteUserName(""),
                new EduPersonPrincipleName("vulnavia.phibes@ligo.org"),
                new EduPersonTargetedID("test:ligo/user/vulnavia.phibes"),
                null, //open id
                null // OIDC
        );
        vulnavia = userStore.createAndRegisterUser(
                umk, // user multi-key
                "https://login2.ligo.org/idp/shibboleth", //idp
                "LIGO", // IDP display name
                "Vulnavia", // first name
                "Phibes", // last name
                "v.phibes@ligo.org", //email
                null, // affiliation
                "Vulnavia Phibes", // display name
                null
        );
        System.out.println("============> LIGO People (with EPPN)");
        System.out.println("get email = false\": " + vulnavia.getDN(null, false) + "\"");
        System.out.println("get email = true\": " + vulnavia.getDN(null, true) + "\"");
        userStore.remove(vulnavia);


        // test the robot

        umk = new UserMultiID(
                new RemoteUserName("test:ligo/user/anton.phibes"),
                new EduPersonPrincipleName("anton.phibes@ligo.org"),
                new EduPersonTargetedID("test:ligo/user/anton.phibes"),
                null, //open id
                null // OIDC
        );

        User anton = userStore.createAndRegisterUser(
                umk,
                "https://login2.ligo.org/idp/shibboleth", //idp
                "LIGO", //ipd display name
                "Anton", //first name
                "Phibes", // last name
                "anton.phibes@ligo.org", // email
                null, // affiliation
                "Anton Phibes", // display name
                "Robots:o3.ncsa.illinois.edu:cron" // organizational unit
        );
        anton.setAttr_json(null);
        anton.setUseUSinDN(true);
        System.out.println("============> LIGO Robots (with eppn)");
        System.out.println("get email=true: " + anton.getDN(null, true));
        System.out.println("get email=false: " + anton.getDN(null, false));
        System.out.println("============> END LIGO\n\n");
        userStore.remove(anton);

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
     * This takes a known user in the database and checks that the computed DN has not changed.
     *
     * @param userStore
     * @throws Exception
     */
    public void testUTF7RegressionTest(UserStore userStore) throws Exception {

        String refDN = "/DC=org/DC=cilogon/C=US/O=Google/CN=+MNUw6yAVMOowxjDqIBU- +MNUw6yAVMOo- D3170 email=boomerangfish@gmail.com";
        String firstName = "フル―リテリ―";
        String lastName = "フル―リ";
        String email = "boomerangfish@gmail.com";
        String affiliation = "affilation test";
        String displayName = firstName + " " + lastName;
        String organizationalUnit = "People:minos27.fnal.gov:cron"; // organizational unit
        RemoteUserName remoteUser = new RemoteUserName("https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!CJtWNa2SKrwXb0NHzPGlWwr7uTE=");
        EduPersonTargetedID eptid = new EduPersonTargetedID("https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!6258095E940C4172B7E4096063C3C447A91B0F14");
        EduPersonPrincipleName eppn = new EduPersonPrincipleName("boomerangfish@gmail.com");
        UserMultiID umk = new UserMultiID(remoteUser, eppn, eptid, null, null);

        User peopleUser = userStore.createAndRegisterUser(
                umk,
                "https://accounts.google.com/o/oauth2/auth", //idp
                "Google", //ipd display name
                firstName, //first name
                lastName, // last name
                email, // email
                affiliation, // affiliation
                displayName, // display name
                organizationalUnit // organizational unit
        );
        peopleUser.setUseUSinDN(true);
        String dn = DNUtil.getDN(peopleUser, null, true);
        System.out.println("reference DN = \"" + refDN);
        System.out.println("computed UTF7 DN = \"" + dn + "\"");
        System.out.println("Are these equal up to serial string?");
        userStore.remove(peopleUser.getIdentifier());
    }

    public void testUTF7(UserStore userStore) throws Exception {
        // test is to send new information for a user.
        String firstName = "Дмитрий";
        String lastName = "Шостакович+源";  // Russian - Japanese last name...
        String email = "Шоста@和楽器.com";
        String affiliation = "affilation" + lastName;
        String displayName = firstName + " " + lastName;
        String organizationalUnit = "People:minos27.fnal.gov:cron"; // organizational unit
        RemoteUserName remoteUser = new RemoteUserName("https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!CJtWNa2SKrwXb0NHzPGlWwr7uTE=");
        EduPersonTargetedID eptid = new EduPersonTargetedID("https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!6258095E940C4172B7E4096063C3C447A91B0F14");
        EduPersonPrincipleName eppn = new EduPersonPrincipleName("kreymer@fnal.gov");
        UserMultiID umk = new UserMultiID(remoteUser, eppn, eptid, null, null);

        User peopleUser = userStore.createAndRegisterUser(
                umk,
                "https://idp.fnal.gov/idp/shibboleth", //idp
                "Fermi National Accelerator Laboratory", //ipd display name
                firstName, //first name
                lastName, // last name
                email, // email
                affiliation, // affiliation
                displayName, // display name
                organizationalUnit // organizational unit
        );

        String dn = DNUtil.getDN(peopleUser, null, true);
        System.out.println("UTF7 DN = \"" + dn + "\"");
        userStore.remove(peopleUser.getIdentifier());
    }

    public static void main(String[] args) {
        try {
            byte[] bytes = new byte[] { 0x1 };
               BigInteger bi = new BigInteger(bytes);
            int decimal = Integer.parseInt("11100",2);
            String hexStr = Integer.toString(decimal,16);
               System.out.println(hexStr);

            String firstName = "Дмитрий";
            String lastName = "Шостакович+源";  // Russian - Japanese last name...
            String email = "Шоста@和楽器.com";

            UTF7Charset utf7 = new UTF7Charset();
            byte[] emailBytes = email.getBytes(utf7);
            System.out.println(new String(emailBytes));
            System.out.println(new String(emailBytes, utf7));


        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Test using a real user and his data
     *
     * @param userStore
     * @throws Exception
     */
    public void testFNAL(UserStore userStore) throws Exception {

        RemoteUserName remoteUser = new RemoteUserName("https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!CJtWNa2SKrwXb0NHzPGlWwr7uTE=");
        EduPersonTargetedID eptid = new EduPersonTargetedID("https://idp.fnal.gov/idp/shibboleth!https://cilogon.org/shibboleth!6258095E940C4172B7E4096063C3C447A91B0F14");
        EduPersonPrincipleName eppn = new EduPersonPrincipleName("kreymer@fnal.gov");
        UserMultiID umk = new UserMultiID(remoteUser, eppn, eptid, null, null);


        User robotUser = userStore.createAndRegisterUser(
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
        robotUser.setAttr_json(null);
        robotUser.setUseUSinDN(true);
        System.out.println("============ START FNAL TEST");
        System.out.println("============ Robots");

        DNUtil.setComputeFNAL(false);
        System.out.println("-> computeFNAL = false");
        System.out.println("ca get cert:" + robotUser.canGetCert());
        System.out.println("get email=true: " + robotUser.getDN(null, true));
        System.out.println("get email=false: " + robotUser.getDN(null, false));
        DNUtil.setComputeFNAL(true);
        System.out.println("-> computeFNAL = true");
        System.out.println("get email=true: " + robotUser.getDN(null, true));
        System.out.println("get email=false: " + robotUser.getDN(null, false));
        System.out.println("============ END Robots");
        // clean up
        userStore.remove(robotUser.getIdentifier());

        // And now to test people
        User peopleUser = userStore.createAndRegisterUser(
                umk,
                "https://idp.fnal.gov/idp/shibboleth", //idp
                "Fermi National Accelerator Laboratory", //ipd display name
                "Arthur", //first name
                "Kreymer", // last name
                "kreymer@fnal.gov", // email
                null, // affiliation
                "Arthur E Kreymer", // display name
                "People:minos27.fnal.gov:cron" // organizational unit
        );

        peopleUser.setAttr_json(null);
        peopleUser.setUseUSinDN(true);

        System.out.println("============ People");
        DNUtil.setComputeFNAL(false);
        System.out.println("-> computeFNAL = false");
        System.out.println("get email=true: " + peopleUser.getDN(null, true));
        System.out.println("get email=false: " + peopleUser.getDN(null, false));
        DNUtil.setComputeFNAL(true);
        System.out.println("-> computeFNAL = true");
        System.out.println("get email=true: " + peopleUser.getDN(null, true));
        System.out.println("get email=false: " + peopleUser.getDN(null, false));
        System.out.println("============ END People");
        // clean up
        userStore.remove(peopleUser.getIdentifier());


    }

    public void doSerialStringIncrement(UserStore userStore) throws Exception {
        User originalUser = userStore.create(true);
        String x = getRandomString();
        originalUser.setFirstName("first-" + x);
        originalUser.setLastName("last-" + x);
        originalUser.setIdP("urn:idp:" + x);
        userStore.save(originalUser);

        Identifier oldSerialIdentifier = originalUser.getSerialIdentifier();
        System.out.println("doSerialStringIncrement: original user:\n" + originalUser.toString(1));
        System.out.println("doSerialStringIncrement: serial identifier = " + oldSerialIdentifier);
        // so change a field and update the user. The serial string should be different
        String y = getRandomString();
        originalUser.setFirstName("first-" + y); // This should trigger a change
        userStore.update(originalUser);
        System.out.println("\ndoSerialStringIncrement: original user:\n" + originalUser.toString(1));
        User updatedUser = userStore.get(originalUser.getIdentifier());
        System.out.println("doSerialStringIncrement: updated user:\n" + updatedUser.toString(1));
        assert !updatedUser.getSerialString().equals(oldSerialIdentifier) :
                "old serial id=" + oldSerialIdentifier + ", new id=" + updatedUser.getSerialIdentifier() + ". These should not be equal.";

        // And now check that the save method actually does choose the right method,
        // updating if the user exists.
/*        oldSerialIdentifier = updatedUser.getSerialIdentifier();
        updatedUser.setLastName("last-" + y);
        userStore.save(updatedUser);
        User updatedUser2 = userStore.get(originalUser.getIdentifier()); // ID should not change!
        System.out.println("\ndoSerialStringIncrement: updated user #2:\n" + updatedUser2.toString(1));

        assert !updatedUser2.getSerialIdentifier().equals(oldSerialIdentifier) :
                "old serial id=" + oldSerialIdentifier + ", new id="
                        + updatedUser2.getSerialIdentifier() + ". These should not be equal for store " + userStore;*/
        userStore.remove(originalUser.getIdentifier());
    }

    /**
     * This tests that the incommon flag in the user object behaves correctly, viz., that it is not set,
     * then once set, attempts to change it fail.
     *
     * @throws Exception
     */
    @Test
    public void testInCommon(UserStore userStore, IdentityProviderStore identityProviderStore) throws Exception {
        IdentityProvider idp = new IdentityProvider(newID(URI.create("urn:identity/prov/" + getRandomString())));
        identityProviderStore.register(idp);
        String random = getRandomString();
        UserMultiID umk = createUMK(random);
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
     *
     * @throws Exception
     */
/*    @Test
    public void testBadIncrementable() throws Exception {
        MemoryUserStore store = new MemoryUserStore(new UserProvider(new MyUIDProvider(),
                null), new MemorySequence(2L));
        String r = getRandomString();

        // It has been saved as part of the registration process and is in the store.
        // Now create another one that is not. Here the user provider can only generate a single user id, mimicking the failure of
        // an SQL store or a file store to increment correctly.
        r = getRandomString();

        try {
            User user = store.createAndRegisterUser(createRU("remote-" + r),
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
    }*/

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
