package org.cilogon.d2.impl;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BeanUtils;
import edu.uiuc.ncsa.security.util.TestBase;
import org.cilogon.d2.CILTestStoreProviderI2;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserMultiID;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.util.ArchivedUserStore;
import org.junit.Test;

import java.util.List;

import static org.cilogon.d2.RemoteDBServiceTest.createUMK;
import static org.cilogon.d2.ServiceTestUtils.checkTimestamp;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/9/12 at  12:11 PM
 */
public class ArchivedUserStoreTest extends TestBase {
    public void testAll() throws Exception {
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMemoryStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMySQLStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getPgStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getDerbyStoreProvider());

        //       doTests((CILTestStoreProviderI2) ServiceTestUtils.getFsStoreProvider());
    }

    public void doTests(CILTestStoreProviderI2 provider) throws Exception {
        testArchiveUserStore(provider);
        testLastArchivedUser(provider);
        testNewIDs(provider);
    }

    /**
     * Regression test that changing something in the user does not change an already archived user. This is unlikely to happen
     * in SQL stores, but could occur in memory or file store implementations.
     *
     * @param provider
     * @throws Exception
     */
    public void testArchiveUserStore(CILTestStoreProviderI2 provider) throws Exception {
        ArchivedUserStore archivedUserStore = provider.getArchivedUserStore();
        // what is in the user store *should* be indep. of the user instance. newUser() saves the object already.
        User user = provider.newUser();

        // Archive the user in the user store
        Identifier archivedUserUID = archivedUserStore.archiveUser(user.getIdentifier());
        assert archivedUserStore.containsKey(archivedUserUID) : "failed to archive user";
        ArchivedUser archivedUser = archivedUserStore.get(archivedUserUID);

        // Check everything is faithfully stored.
        compareUsers(archivedUser, user, true);

        // Now change some stuff and check that the archived user has not somehow changed. This is less important
        // for SQL stores, but could happen in memory or file stores if they are nto implemented correctly
        String oldFirstName = user.getFirstName();
        user.setUseUSinDN(true);
        user.setAttr_json("random_attribute_string_" + getRandomString());
        user.setFirstName("Roderick");
        user.setFirstName(oldFirstName);
        provider.getUserStore().save(user);
        archivedUser = archivedUserStore.get(archivedUserUID);
        User oldUser = archivedUser.getUser();
        assert oldUser.getFirstName().equals(oldFirstName) : "Changing user updates archived user";
        assert oldUser.isUseUSinDN() == false;
        assert !user.getAttr_json().equals(oldUser.getAttr_json());
        provider.getUserStore().remove(user); // cleanup
        provider.getArchivedUserStore().remove(archivedUser);
    }

    /**
     * Create a user and immediately archive it. These should match exactly (showing that saving it to the archive is faithful
     *
     * @param provider
     * @throws Exception
     */
    @Test
    public void testArchivingIntergrity(CILTestStoreProviderI2 provider) throws Exception {
        ArchivedUserStore archivedUserStore = provider.getArchivedUserStore();
        // what is in the user store *should* be indep. of the user instance. newUser() saves the object already.
        User user = provider.newUser();

        // Archive the user in the user store
        Identifier archivedUserUID = archivedUserStore.archiveUser(user.getIdentifier());
        assert archivedUserStore.containsKey(archivedUserUID) : "failed to archive user";
        ArchivedUser archivedUser = archivedUserStore.get(archivedUserUID);

        // Get the archived user and prove it is indeed the old user, not the new one.
        User oldUser = archivedUser.getUser();

        compareUsers(archivedUser, user, true);
        provider.getUserStore().remove(user); // cleanup
        provider.getArchivedUserStore().remove(archivedUser);
    }

    /**
     * This is to be used as follows.
     * (1) create and save a user (newUser() will make one and save it)
     * (2) archive it
     * (3) get the most recent one from the archive
     * (4) invoke this method after saving a user and archiving it.
     * This compares the supplied user with the user fields in the archived user.
     *
     * @param archivedUser
     * @param user
     * @throws Exception
     */
    protected void compareUsers(ArchivedUser archivedUser, User user, boolean doSerialIdsMatch) throws Exception {
        User oldUser = archivedUser.getUser();
        assert oldUser.getFirstName().equals(user.getFirstName()) : "User first names don't match";
        assert oldUser.getIdentifier().equals(user.getIdentifier()) : "User uid's don't match";
        assert oldUser.getLastName().equals(user.getLastName()) : "User last names don't match";
        assert oldUser.getEmail().equals(user.getEmail()) : "User email's don't match";
        assert oldUser.getIDPName().equals(user.getIDPName()) : "User idp names don't match";
        assert BeanUtils.checkNoNulls(oldUser.getePPN(), user.getePPN()) : "User eppns don't match. Expected " + oldUser.getUserMultiKey() + " and got " + user.getUserMultiKey();
        assert BeanUtils.checkNoNulls(oldUser.getePTID(), user.getePTID()) : "User eptids don't match. Expected " + oldUser.getePTID() + " and got " + user.getePTID();
        assert BeanUtils.checkNoNulls(oldUser.getOpenID(), user.getOpenID()) : "User openids don't match. Expected " + oldUser.getOpenID() + " and got " + user.getOpenID();
        assert BeanUtils.checkNoNulls(oldUser.getOpenIDConnect(), user.getOpenIDConnect()) : "User openid connect id's don't match. Expected " + oldUser.getOpenIDConnect() + " and got " + user.getOpenIDConnect();
        assert BeanUtils.checkNoNulls(oldUser.getRemoteUser(), user.getRemoteUser()) : "User remote users don't match. Expected " + oldUser.getRemoteUser() + " and got " + user.getRemoteUser();
        assert BeanUtils.checkNoNulls(oldUser.isUseUSinDN(), user.isUseUSinDN()) : "'User is in US' don't match. Expected " + oldUser.isUseUSinDN() + " and got " + user.isUseUSinDN();
        if (user.getAttr_json() == null) {
            if (oldUser.getAttr_json() == null || oldUser.getAttr_json().isEmpty()) {
                assert true;
            } else {
                assert false : "JSON attributes do not match. Expected \"" + oldUser.getAttr_json() + "\" and got \"" + user.getAttr_json() + "\"";
            }
        } else {
            if (oldUser.getAttr_json() == null || oldUser.getAttr_json().isEmpty()) {
                assert false : "JSON attributes do not match. Expected \"" + oldUser.getAttr_json() + "\" and got \"" + user.getAttr_json() + "\"";
            } else {
                assert oldUser.getAttr_json().equals(user.getAttr_json());
            }
        }

        if (oldUser.hasEPPN()) {
            assert BeanUtils.checkBasic(oldUser.getePPN(), user.getePPN()) : "User eppns don't match. Expected " + oldUser.getePPN() + " and got " + user.getePPN();
        } else {
            assert user.getePPN() == null : "User eppn should be null, got " + user.getePPN();
        }
        if (oldUser.hasEPTID()) {
            assert BeanUtils.checkBasic(oldUser.getePTID(), user.getePTID()) : "User eptids don't match. Expected " + oldUser.getePTID() + " and got " + user.getePTID();
        } else {
            assert user.getePTID() == null : "User eptid should be null, got " + user.getePTID();
        }
        if (oldUser.hasOpenID()) {
            assert BeanUtils.checkBasic(oldUser.getOpenID(), user.getOpenID()) : "User openids don't match. Expected " + oldUser.getOpenID() + " and got " + user.getOpenID();
        } else {
            assert user.getOpenID() == null : "User openID should be null, got " + user.getOpenID();
        }
        if (oldUser.hasOpenIDConnect()) {
            assert BeanUtils.checkBasic(oldUser.getOpenIDConnect(), user.getOpenIDConnect()) : "User oidc's don't match. Expected " + oldUser.getOpenID() + " and got " + user.getOpenID();
        } else {
            assert user.getOpenIDConnect() == null : "User oidc  should be null, got " + user.getOpenIDConnect();
        }
        if (oldUser.hasRemoteUser()) {
            assert BeanUtils.checkBasic(oldUser.getRemoteUser(), user.getRemoteUser()) : "User remote users don't match. Expected " + oldUser.getRemoteUser() + " and got " + user.getRemoteUser();

        } else {
            assert user.getRemoteUser() == null : "User's remote_user should be null, got " + user.getRemoteUser();
        }
        // The old serial identifier is set to be the same as the uid at creation time. Check
        if (doSerialIdsMatch) {
            assert oldUser.getSerialIdentifier().equals(user.getSerialIdentifier()) : "User's serial identifiers don't match. Expected " + oldUser.getSerialIdentifier() + " and got " + user.getSerialIdentifier();
        } else {
            assert !oldUser.getSerialIdentifier().equals(user.getSerialIdentifier()) : "User's serial identifiers match:" + user.getSerialIdentifier();
        }
        assert checkTimestamp(user, oldUser);

    }


    public void testLastArchivedUser(CILTestStoreProviderI2 provider) throws Exception {
        UserStore userStore = provider.getUserStore();
        ArchivedUserStore archivedUserStore = provider.getArchivedUserStore();
        User user = provider.newUser();
        userStore.save(user);

        // Shouldn't be one archived before we start.
        assert archivedUserStore.getLastArchivedUser(user.getIdentifier()) == null;
        // archive some users
        for (int i = 0; i < count; i++) {
            // This next sleep command is needed because if items update too fast, their timestamps in SQL
            // stores might not be distinct. Getting the last archived user item then is undefined. This is mostly
            // a MySQL issue, since unlike Postgres, MySQL does not store nanos as well.
            Thread.sleep(1000);
            user.setFirstName("Bob-" + i);
            // update does not archive. We have to do that
            archivedUserStore.archiveUser(user.getIdentifier());
            userStore.update(user);
        }
        List<ArchivedUser> x = archivedUserStore.getAllByUserId(user.getIdentifier());
        assert x.size() == count;
        User targetUser = archivedUserStore.getLastArchivedUser(user.getIdentifier()).getUser();
        // We're one behind the count in number (index origin zero) and one behind that in names, hence count-2 should do it.
        if (targetUser.getFirstName().equals("Bob-" + (count - 2))) {
            assert true;
        } else {
            System.out.println(getClass().getSimpleName() + ": archived user name = " + targetUser.getFirstName() + ", should be Bob-" + (count - 2));
            System.out.println(getClass().getSimpleName() + ": store =" + archivedUserStore.getClass().getName());
            System.out.println("Failed to get correct archived user? These are sorted by timestamp and in *tests* backed by an SQL store they might be indistinguishable.");
        }
    }

    public void testNewIDs(CILTestStoreProviderI2 provider) throws Exception {
        UserMultiID umk = createUMK();
        UserMultiID ruKey = new UserMultiID(umk.getRemoteUserName());
        UserMultiID eppnKey = new UserMultiID(umk.getEppn());
        UserMultiID eptidKey = new UserMultiID(umk.getEptid());
        UserMultiID openIdKey = new UserMultiID(umk.getOpenID());
        UserMultiID oidcKey = new UserMultiID(umk.getOpenIDConnect());

        User user = provider.newUser();

        checkArchivedKey(provider, ruKey, user, true);
        checkArchivedKey(provider, eppnKey, user, false);
        checkArchivedKey(provider, eptidKey, user, false);
        checkArchivedKey(provider, openIdKey, user, false);
        checkArchivedKey(provider, oidcKey, user, false);

        List<ArchivedUser> archivedUsers = provider.getArchivedUserStore().getAllByUserId(user.getIdentifier());
        assert archivedUsers.size() == 5 : "Expected 5 archived users and got " + archivedUsers.size() + " instead.";

    }

    private void checkArchivedKey(CILTestStoreProviderI2 provider, UserMultiID multiKey, User user, boolean doSerialIDsMatch) throws Exception {
        ArchivedUser lastAUser;
        user.setUserMultiKey(multiKey);
        provider.getUserStore().update(user);
        Identifier archivedUserID = provider.getArchivedUserStore().archiveUser(user.getIdentifier());
        lastAUser = provider.getArchivedUserStore().get(archivedUserID);
        compareUsers(lastAUser, user, true);
    }
}
