package org.cilogon.d2;

import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.storage.XMLMap;
import org.cilogon.d2.storage.EduPersonPrincipleName;
import org.cilogon.d2.storage.RemoteUserName;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserMultiKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/12/14 at  2:51 PM
 */
public class DBServiceUserIDTests extends RemoteDBServiceTest {
    /**
     * Test that each of the ids can be user to identify a user and retrieve it.
     *
     * @throws Exception
     */
    @Test
    public void testEPPN() throws Exception {
        // Get a set of keys.
        UserMultiKey umk = createUMK();

        User user = newUser();
        UserMultiKey newUmk = new UserMultiKey(umk.getEppn());
        user.setUserMultiKey(newUmk);
        getUserStore().save(user);
        Collection<User> users = getUserStore().get(newUmk, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());
        XMLMap userMap = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(userMap, user);
    }


    @Test
    public void testEPTID() throws Exception {
        // Get a set of keys.
        UserMultiKey umk = createUMK();

        User user = newUser();
        UserMultiKey newUmk = new UserMultiKey(umk.getEptid());
        user.setUserMultiKey(newUmk);
        getUserStore().save(user);
        Collection<User> users = getUserStore().get(newUmk, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());
        XMLMap userMap = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(userMap, user);

    }

    @Test
    public void testEPTID2() throws Exception {
        // Get a set of keys.
        UserMultiKey umk = createUMK();
        RemoteUserName ru = umk.getRemoteUserName();
        User user = newUser(); //saved
        UserMultiKey eptid = new UserMultiKey(umk.getEptid());
        user.setUserMultiKey(new UserMultiKey(ru));
        getUserStore().save(user);
        // This puts a new user with a remote user fireld (legacy case) in the store. Now we try and get the user
        // with *both* the remote user and an eptid

        user.setUserMultiKey(new UserMultiKey(ru, null, eptid.getEptid(), null, null));
        XMLMap userMap = getDBSClient().getUser(user);
        Collection<User> users = getUserStore().get(eptid, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        User testUser = users.iterator().next();
        user.setSerialIdentifier(testUser.getSerialIdentifier()); // this was reset in the getUser command to the AbstractDBService client.
        assert user.equals(users.iterator().next());
        userMap = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(userMap, user);

    }


    @Test
    public void testOpenID() throws Exception {
        // Get a set of keys.
        UserMultiKey umk = createUMK();

        User user = newUser();
        UserMultiKey newUmk = new UserMultiKey(umk.getOpenID());
        user.setUserMultiKey(newUmk);
        getUserStore().save(user);
        Collection<User> users = getUserStore().get(newUmk, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());
        XMLMap userMap = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(userMap, user);
    }


    @Test
    public void testOpenIDConnect() throws Exception {
        // Get a set of keys.
        UserMultiKey umk = createUMK();

        User user = newUser();
        UserMultiKey newUmk = new UserMultiKey(umk.getOpenIDConnect());
        user.setUserMultiKey(newUmk);
        getUserStore().save(user);
        Collection<User> users = getUserStore().get(newUmk, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());
        XMLMap userMap = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(userMap, user);
    }

    /**
     * User is created with EPPN. Then fetch with both EPPN and EPTID. The user
     * should be updated to now have the eptid as well.
     *
     * @throws Exception
     */

    @Test
    public void testEPPNThenTwo() throws Exception {
        UserMultiKey umk = createUMK(getRandomString());
        UserMultiKey eppnKey = new UserMultiKey(umk.getEppn());
        UserMultiKey umk2 = new UserMultiKey(umk.getEppn(), umk.getEptid());
        User user = testOneThenTwoIds(eppnKey, umk2);
        user = getUserStore().get(user.getIdentifier());
        assert user.getePPN().equals(umk2.getEppn());
        assert user.getePTID().equals(umk2.getEptid());

    }

    @Test
    public void testOpenIDThenTwo() throws Exception {
        UserMultiKey umk = createUMK(getRandomString());
        UserMultiKey eppnKey = new UserMultiKey(umk.getOpenID());
        UserMultiKey umk2 = new UserMultiKey(umk.getOpenID(), umk.getOpenIDConnect());
        User user = testOneThenTwoIds(eppnKey, umk2);
        user = getUserStore().get(user.getIdentifier());
        assert user.getOpenID().equals(umk2.getOpenID());
        assert user.getOpenIDConnect().equals(umk2.getOpenIDConnect());
    }

    /**
     * This test covers the case where one ID is given, then later a pair are given
     * (such as eppn then eppn,eptid or openid then openid, oidc). This ensures that the system can
     * resolved these correctly.
     *
     * @param key1
     * @param umk2
     * @throws Exception
     */
    public User testOneThenTwoIds(UserMultiKey key1, UserMultiKey umk2) throws Exception {
        User user = newUser();
        user.setUserMultiKey(key1);
        getUserStore().save(user); // otherwise the next step returns a different user id.
        XMLMap map = getDBSClient().getUser(user.getUserMultiKey(),
                user.getIdP(), user.getIDPName(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getAffiliation(),
                user.getDisplayName(),
                user.getOrganizationalUnit());

        checkUserAgainstMap(map, user);
        // now reget with both the first and second key
        map = getDBSClient().getUser(umk2, user.getIdP());
        user.setUserMultiKey(umk2);
        checkUserAgainstMap(map, user);
        // final bit of this is to check that giving just first key in the future will return the right user.
        map = getDBSClient().getUser(key1, user.getIdP());
        // user.setUserMultiKey(key1);
        checkUserAgainstMap(map, user);
        return user;
    }

    /**
     * Very similar to the previous test, but start with eptid, then fetch by eptid & eppn,
     * then show getting by eptid still works.
     *
     * @throws Exception
     */
    @Test
    public void testEPTIDThenTwo() throws Exception {
        UserMultiKey umk = createUMK();
        UserMultiKey eptidKey = new UserMultiKey(umk.getEptid());
        User user = newUser();
        user.setUserMultiKey(eptidKey);
        getUserStore().save(user); // otherwise the next step returns a different user id.
        XMLMap map = getDBSClient().getUser(user.getUserMultiKey(),
                user.getIdP(),
                user.getIDPName(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAffiliation(),
                user.getDisplayName(),
                user.getOrganizationalUnit());

        checkUserAgainstMap(map, user);
        // now reget with both eppn and eptid
        UserMultiKey umk2 = new UserMultiKey(umk.getEppn(), umk.getEptid());
        map = getDBSClient().getUser(umk2, user.getIdP());
        // Backend regression check
        User user2 = getUserStore().get(user.getIdentifier());
        assert user2.getePPN().equals(umk.getEppn()) : "EPPNs do not match after AbstractDBService multi-get";
        assert user2.getePTID().equals(umk.getEptid()) : "EPTIDs do not match after AbstractDBService multi-get";
        user.setUserMultiKey(umk2);
        // now check that the local copy didn't lose anything when it went through the service.
        checkUserAgainstMap(map, user);
        // final bit of this is to check that giving just eppn in the future will return the right user.
        map = getDBSClient().getUser(eptidKey, user.getIdP());
        checkUserAgainstMap(map, user);
    }

    /**
     * User is created with EPPN and EPTID. User should be recoverable from either EPPN or
     * EPTID alone
     *
     * @throws Exception
     */
    @Test
    public void testTwoIDs() throws Exception {
        // Get a set of keys.
        UserMultiKey umk = createUMK();

        User user = newUser();
        UserMultiKey newUmk = new UserMultiKey(umk.getEppn(), umk.getEptid());
        user.setUserMultiKey(newUmk);
        getUserStore().save(user);
        Collection<User> users = getUserStore().get(newUmk, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());
        // Now reget with just EPTID. Since that is globally unique, this should always work.
        UserMultiKey newUmk2 = new UserMultiKey(umk.getEptid());
        users = getUserStore().get(newUmk2, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());

        XMLMap userMap = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(userMap, user);
    }

    /**
     * Check that if there are multiple EPPNs. This is the case where a user is in the system with a
     * (non-unique). Since this could be re-used, we treat that case here. At some later point,
     * the site re-uses the EPPN. multiple users should be resolved on the basis of time-stamp.
     *
     * @throws Exception
     */
    @Test
    public void testSameEPPNs() throws Exception {
        // Get a set of keys.
        ArrayList<UserMultiKey> umks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            umks.add(createUMK());
        }
        User user = newUser();
        User mostRecentUser = user;
        String idp = user.getIdP();
        long oneYear = 31557600000L; // in ms.
        long currentTime = System.currentTimeMillis();
        ArrayList<User> newUsers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            newUsers.add(user);

            // all have the same EPPN, but different eptids
            UserMultiKey newUmk = new UserMultiKey(umks.get(0).getEppn(), umks.get(i).getEptid());
            Date date = new Date(currentTime - (i + 1) * oneYear); // have these spaced one year apart.
            user.setCreationTime(date);
            user.setUserMultiKey(newUmk);
            getUserStore().save(user);
            user = newUser();
            user.setIdP(idp);
        }
        UserMultiKey newUmk = new UserMultiKey(umks.get(0).getEppn());

        Collection<User> users = getUserStore().get(newUmk, idp);

        assert users.size() == count : "Incorrect number of users found. Expected " + count + " and got " + users.size();
        XMLMap userMap = getDBSClient().getUser(newUmk, idp);
        checkUserAgainstMap(userMap, mostRecentUser);
    }

    /**
     * in this test, a user is created with both an eptid and an eppn. The eppn changes. The user should be
     * resolved on the eptid since that is globally unique.
     *
     * @throws Exception
     */
    @Test
    public void testEPTIDWins() throws Exception {
        // Get a set of keys.
        UserMultiKey umk = createUMK();

        User user = newUser();
        UserMultiKey newUmk = new UserMultiKey(umk.getEppn(), umk.getEptid());
        user.setUserMultiKey(newUmk);
        getUserStore().save(user);
        UserMultiKey umkNoEPPN = new UserMultiKey(umk.getEptid());
        UserMultiKey umkNewEPPN = new UserMultiKey(new EduPersonPrincipleName("eppn:" + getRandomString()), umk.getEptid());

        Collection<User> users = getUserStore().get(umkNoEPPN, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());

        users = getUserStore().get(umkNewEPPN, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());

        XMLMap userMap = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(userMap, user);
    }

    /**
     * Part of our contract is that either openID is set or at least one of eppn/eptid are. An
     * exception should occur if all are present, at least now.
     *
     * @throws Exception
     */
    @Test
    public void testAll() throws Exception {
        UserMultiKey umk = createUMK();
        User user = newUser();
        user.setUserMultiKey(umk);
        // First, try and save it
        try {
            getDBSClient().getUser(umk, user.getIdP());
            assert true;
        } catch (Throwable t) {
            assert false : "Error: was able to specify values for all ids. This is not allowed";
        }
        // ok, so now we stick this user in storage directly. The store should allow for
        // multiple ids, the AbstractDBService (which has the logic to disambiguate them) does not.
        getUserStore().save(user);
        try {
            getDBSClient().getUser(user.getIdentifier());
            assert true;
        } catch (Throwable t) {
            assert false : "Error: saved user with id=" + user.getIdentifierString() + " could not be retrieved";
        }

        try {
            getDBSClient().getUser(umk, user.getIdP());
            assert false : "Error: Was able to get user with key=" + umk + ". Having all Ids set should be refused by AbstractDBService";
        } catch (Throwable t) {
            assert true;
        }
    }

    /**
     * In reality, keys consist of the id and the IDP. This tests that if the eppn or open id
     * are re-used with an idp, that this is resolved satisfactorily, meaning, it is quite
     * possible to have the same eppn with different idps (e.g. a user has an external email
     * which is used as the identifier and has affiliations with multiple idps.)
     * In the case of EPTIDs there must be an exact match at all times, since EPTIDs
     * are globally unique. Therefore no variance in IDPs is permitted.
     *
     * @throws Exception
     */
    @Test
    public void testKeyUniqueness() throws Exception {
        UserMultiKey umk = createUMK();
        UserMultiKey eppnKey = new UserMultiKey(umk.getEppn());
        UserMultiKey eptidKey = new UserMultiKey(umk.getEptid());
        UserMultiKey openIdKey = new UserMultiKey(umk.getOpenID());
        UserMultiKey openIdConnectKey = new UserMultiKey(umk.getOpenIDConnect());
        UserMultiKey ruKey = new UserMultiKey(umk.getRemoteUserName());

        String otherIDP = "idp:other:" + getRandomString();
        checkIDPUniqueness(eppnKey, otherIDP);
        checkIDPUniqueness(openIdConnectKey, otherIDP);
        checkIDPUniqueness(openIdKey, otherIDP);
        checkIDPUniqueness(ruKey, otherIDP);

        User eptidUser = newUser();
        eptidUser.setUserMultiKey(eptidKey);
        getUserStore().save(eptidUser);
        try {
            getDBSClient().getUser(eptidKey, otherIDP);
            assert false : "Error: eptid must match idp exactly.";
        } catch (Throwable t) {
            assert true;
        }

    }

    // used only in previous method
    private void checkIDPUniqueness(UserMultiKey eppnKey, String otherIDP) throws Exception {
        User eppnUser = newUser();
        eppnUser.setUserMultiKey(eppnKey);
        getUserStore().save(eppnUser);
        eppnUser.setIdP(otherIDP);
        getUserStore().save(eppnUser);
        try {
            getDBSClient().getUser(eppnKey, otherIDP);
            assert true;
        } catch (Throwable t) {
            assert false : "Error: multiple IDP values are supported.";
        }
    }

    /**
     * In this case, any of the ids may be used with a given idp. Getting the user with a different idp
     * will result in an error.
     *
     * @throws Exception
     */
    @Test
    public void testIDPChange() throws Exception {
        UserMultiKey umk = createUMK();
        UserMultiKey eppnKey = new UserMultiKey(umk.getEppn());
        UserMultiKey eptidKey = new UserMultiKey(umk.getEptid());
        UserMultiKey openIdKey = new UserMultiKey(umk.getOpenID());
        UserMultiKey openIdConnectKey = new UserMultiKey(umk.getOpenIDConnect());
        UserMultiKey ruKey = new UserMultiKey(umk.getRemoteUserName());

        String badIdp = "fake:idp";
        // now we save a user with the right id, then try to get it with a different idp.
        checkGetUser(ruKey, badIdp);
        checkGetUser(eppnKey, badIdp);
        checkGetUser(eptidKey, badIdp);
        checkGetUser(openIdKey, badIdp);
        checkGetUser(openIdConnectKey, badIdp);
    }

    // useronly in previous method.
    private void checkGetUser(UserMultiKey key, String badIdp) throws Exception {
        User user = newUser();
        user.setUserMultiKey(key);
        getUserStore().save(user);
        try {
            getDBSClient().getUser(key, badIdp);
            assert false : "Error: was able to get the user with a wrong idp.";
        } catch (Throwable t) {
            assert true;
        }
    }

    /**
     * Test that archiving a user with identifiers faithfully stores them.
     *
     * @throws Exception
     */
    @Test
    public void testGetArchiveUser() throws Exception {
        UserMultiKey umk = createUMK();
        UserMultiKey eppnKey = new UserMultiKey(umk.getEppn());
        UserMultiKey eptidKey = new UserMultiKey(umk.getEptid());
        UserMultiKey openIdKey = new UserMultiKey(umk.getOpenID());
        UserMultiKey ruKey = new UserMultiKey(umk.getRemoteUserName());

        User user = newUser();
        user.setUserMultiKey(ruKey);
        // First test: the getUser command here will not get the user by its unique ID, but by the 6+ parameters that
        // define it. At this point we have made a user and updated the ruKey. Getting the user should return a completele
        // new user.
        XMLMap map = getDBSClient().getUser(user);
        assert !user.getIdentifierString().equals(map.get(userKeys.identifier())) : "User ids match and should not";

        user.setIdentifier(BasicIdentifier.newID(map.get(userKeys.identifier()).toString()));
        user.setSerialIdentifier(user.getIdentifier());
        // The previous step got a new identifier. We should use that from now on or our methods to
        // check equality will always fail.

        // Now we are ready to actually check this user.
        String x = getRandomString();
        User user2 = (User) user.clone(); // so we can check it did get archived right later
        user.setLastName("last-" + x);
        user.setFirstName("first-" + x);
        user.setEmail(user.getFirstName() + "." + user.getLastName() + "@foo-" + x + ".edu");
        user.setIDPName("idpname-" + x);
        map = getDBSClient().getUser(user);  // This will update this, archive the current version and update the serial string.
        User user3 = getUserStore().get(user.getIdentifier());
        checkUserAgainstMap(map, user3); // giggle test to make sure it really is right.
        XMLMap amap = getDBSClient().getLastArchivedUser(user.getIdentifier());
        user.setSerialIdentifier(user3.getSerialIdentifier());
        checkUserAgainstMap(amap, user2);
        checkUserAgainstMap(map, user);
        // now we change things that are not identifiers and show that the fields all update correctly.
        getArchivedUserStore().archiveUser(user.getIdentifier());
        map = getDBSClient().getLastArchivedUser(user.getIdentifier());

        checkUserAgainstMap(map, user);
    }
}
