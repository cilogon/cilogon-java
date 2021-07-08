package org.cilogon.d2;

import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.storage.XMLMap;
import net.sf.json.JSONObject;
import org.cilogon.d2.storage.EduPersonPrincipleName;
import org.cilogon.d2.storage.RemoteUserName;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserMultiID;
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
        UserMultiID umk = createUMK();

        User user = newUser();
        UserMultiID newUmk = new UserMultiID(umk.getEppn());
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
        UserMultiID umk = createUMK();

        User user = newUser();
        UserMultiID newUmk = new UserMultiID(umk.getEptid());
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
        UserMultiID umk = createUMK();
        RemoteUserName ru = umk.getRemoteUserName();
        User user = newUser(); //saved
        UserMultiID eptid = new UserMultiID(umk.getEptid());
        user.setUserMultiKey(new UserMultiID(ru));
        getUserStore().save(user);
        // This puts a new user with a remote user fireld (legacy case) in the store. Now we try and get the user
        // with *both* the remote user and an eptid

        user.setUserMultiKey(new UserMultiID(ru, null, eptid.getEptid(), null, null));
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
        UserMultiID umk = createUMK();

        User user = newUser();
        UserMultiID newUmk = new UserMultiID(umk.getOpenID());
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
        UserMultiID umk = createUMK();

        User user = newUser();
        UserMultiID newUmk = new UserMultiID(umk.getOpenIDConnect());
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
        UserMultiID umk = createUMK(getRandomString());
        UserMultiID eppnKey = new UserMultiID(umk.getEppn());
        UserMultiID umk2 = new UserMultiID(umk.getEppn(), umk.getEptid());
        User user = testOneThenTwoIds(eppnKey, umk2);
        user = getUserStore().get(user.getIdentifier());
        assert user.getePPN().equals(umk2.getEppn());
        assert user.getePTID().equals(umk2.getEptid());

    }

    @Test
    public void testOpenIDThenTwo() throws Exception {
        UserMultiID umk = createUMK(getRandomString());
        UserMultiID eppnKey = new UserMultiID(umk.getOpenID());
        UserMultiID umk2 = new UserMultiID(umk.getOpenID(), umk.getOpenIDConnect());
        User user = testOneThenTwoIds(eppnKey, umk2);
        user = getUserStore().get(user.getIdentifier());
        assert user.getOpenID().equals(umk2.getOpenID());
        assert user.getOpenIDConnect().equals(umk2.getOpenIDConnect());
    }

    void printUserNice(String header, User user) throws Exception {
        XMLMap map = new XMLMap();
        getUserStore().getMapConverter().toMap(user, map);
        JSONObject j = new JSONObject();
        j.putAll(map);
        System.err.println(header);
        System.err.println(j.toString(2));

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
    public User testOneThenTwoIds(UserMultiID key1, UserMultiID umk2) throws Exception {
        User user = newUser();
        printUserNice("testOneThenTwoIds:", user);
        user.setUserMultiKey(key1);
        getUserStore().update(user, true); // otherwise the next step returns a different user id.

        XMLMap map = getDBSClient().getUser(user.getUserMultiKey(),
                user.getIdP(), user.getIDPName(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getAffiliation(),
                user.getDisplayName(),
                user.getOrganizationalUnit());
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(map);

        checkUserAgainstMap(map, user);
        // now re-get with both the first and second key
        map = getDBSClient().getUser(umk2, user.getIdP());

        user.setUserMultiKey(umk2);

        checkUserAgainstMap(map, user);
        // final bit of this is to check that giving just first key in the future will return the right user.
        map = getDBSClient().getUser(key1, user.getIdP());
        jsonObject = new JSONObject();
        jsonObject.putAll(map);
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
        UserMultiID umk = createUMK();
        UserMultiID eptidKey = new UserMultiID(umk.getEptid());
        User user = newUser();
        user.setUserMultiKey(eptidKey);
        getUserStore().update(user, true); // otherwise the next step returns a different user id.
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
        UserMultiID umk2 = new UserMultiID(umk.getEppn(), umk.getEptid());
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
        UserMultiID umk = createUMK();

        User user = newUser();
        UserMultiID newUmk = new UserMultiID(umk.getEppn(), umk.getEptid());
        user.setUserMultiKey(newUmk);
        getUserStore().save(user);
        Collection<User> users = getUserStore().get(newUmk, user.getIdP());
        assert users.size() == 1 : "Incorrect number of users found. Expected one and got " + users.size();
        assert user.equals(users.iterator().next());
        // Now reget with just EPTID. Since that is globally unique, this should always work.
        UserMultiID newUmk2 = new UserMultiID(umk.getEptid());
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
        ArrayList<UserMultiID> umks = new ArrayList<>();
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
            UserMultiID newUmk = new UserMultiID(umks.get(0).getEppn(), umks.get(i).getEptid());
            Date date = new Date(currentTime - (i + 1) * oneYear); // have these spaced one year apart.
            user.setCreationTime(date);
            user.setUserMultiKey(newUmk);
            getUserStore().update(user, true);
            user = newUser();
            user.setIdP(idp);
        }
        UserMultiID newUmk = new UserMultiID(umks.get(0).getEppn());

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
        UserMultiID umk = createUMK();

        User user = newUser();
        UserMultiID newUmk = new UserMultiID(umk.getEppn(), umk.getEptid());
        user.setUserMultiKey(newUmk);
        getUserStore().save(user);
        UserMultiID umkNoEPPN = new UserMultiID(umk.getEptid());
        UserMultiID umkNewEPPN = new UserMultiID(new EduPersonPrincipleName("eppn:" + getRandomString()), umk.getEptid());

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
        UserMultiID umk = createUMK();
        User user = newUser();
        user.setUserMultiKey(umk);
        // First, try and save it
        try {
            getDBSClient().getUser(umk, user.getIdP());
            assert false : "Error: was able to specify values for all ids. This is not allowed";
        } catch (Throwable t) {
            assert true;
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
        UserMultiID umk = createUMK();
        UserMultiID eppnKey = new UserMultiID(umk.getEppn());
        UserMultiID eptidKey = new UserMultiID(umk.getEptid());
        UserMultiID openIdKey = new UserMultiID(umk.getOpenID());
        UserMultiID openIdConnectKey = new UserMultiID(umk.getOpenIDConnect());
        UserMultiID ruKey = new UserMultiID(umk.getRemoteUserName());

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
    private void checkIDPUniqueness(UserMultiID eppnKey, String otherIDP) throws Exception {
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
        UserMultiID umk = createUMK();
        UserMultiID eppnKey = new UserMultiID(umk.getEppn());
        UserMultiID eptidKey = new UserMultiID(umk.getEptid());
        UserMultiID openIdKey = new UserMultiID(umk.getOpenID());
        UserMultiID openIdConnectKey = new UserMultiID(umk.getOpenIDConnect());
        UserMultiID ruKey = new UserMultiID(umk.getRemoteUserName());

        String badIdp = "fake:idp";
        // now we save a user with the right id, then try to get it with a different idp.
        checkGetUser(ruKey, badIdp);
        checkGetUser(eppnKey, badIdp);
        checkGetUser(eptidKey, badIdp);
        checkGetUser(openIdKey, badIdp);
        checkGetUser(openIdConnectKey, badIdp);
    }

    // user only in previous method.
    private void checkGetUser(UserMultiID key, String badIdp) throws Exception {
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
     * <h1>Updated 2020-03-24T15:45:49.545Z</h1>
     * This test is now disabled since we are aiming to remove archiving users from the database. This means that
     * there will be no further testing of these components and if they break because of code changes it will
     * be silent.
     * Test that archiving a user with identifiers faithfully stores them.
     *
     * @throws Exception
     */
    //@Test
    //public void testGetArchiveUser() throws Exception {
    public void OLDTEST() throws Exception {

        UserMultiID umk = createUMK();
        UserMultiID ruKey = new UserMultiID(umk.getRemoteUserName());

        User user = newUser(); // creates and registers this user.
        user.setUserMultiKey(ruKey);
        // First test: the getUser command here will not get the user by its unique ID, but by the 6+ parameters that
        // define it. At this point we have made a user and updated the ruKey. Getting the user should return a completely
        // new user.
        XMLMap map = getDBSClient().getUser(user);
        assert !user.getIdentifierString().equals(map.get(userKeys.identifier())) : "User ids match and should not";

        user.setIdentifier(BasicIdentifier.newID(map.get(userKeys.identifier()).toString()));
        user.setSerialIdentifier(user.getIdentifier());
        // The previous step got a new identifier. We should use that from now on or our methods to
        // check equality will always fail.

        // Now we are ready to actually check this user.
        String x = getRandomString();
        User referenceUser = user.clone(); // so we can check it did get archived right later
        user.setLastName("last-" + x);
        user.setFirstName("first-" + x);
        user.setEmail(user.getFirstName() + "." + user.getLastName() + "@foo-" + x + ".edu");
        user.setIDPName("idpname-" + x);
        map = getDBSClient().getUser(user);  // This will update this, archive the current version and update the serial string.
        User user3 = getUserStore().get(user.getIdentifier());
        checkUserAgainstMap(map, user3); // giggle test to make sure it really is right.
        XMLMap archiveMap = getDBSClient().getLastArchivedUser(user.getIdentifier());
        user.setSerialIdentifier(user3.getSerialIdentifier());
        checkUserAgainstMap(archiveMap, referenceUser); // so what's in the archive and previous user match
        checkUserAgainstMap(map, user); // Updated user and store match.
        // Last step that we have a new and old version of the user is to archive it here and show that when we get it
        // back from the DB service, it is the same user.
        getArchivedUserStore().archiveUser(user.getIdentifier());
        map = getDBSClient().getLastArchivedUser(user.getIdentifier());

        checkUserAgainstMap(map, user);
    }
}
