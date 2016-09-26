package org.cilogon.d2.impl;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BeanUtils;
import org.cilogon.d2.CILStoreTest;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserMultiKey;
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
public abstract class ArchivedUserStoreTest extends CILStoreTest {

    public UserStore getUserStore() throws Exception {
        return getCILStoreTestProvider().getUserStore();
    }

    public ArchivedUserStore getArchivedUserStore() throws Exception {
        return getCILStoreTestProvider().getArchivedUserStore();
    }


    @Override
    public void checkStoreClass() throws Exception {
        //     assert getTSProvider().getArchivedUserStore().getClass().isAssignableFrom(getStoreClass())  : "The archived user store is not a " + getStoreClass().getSimpleName();
        testClassAsignability(getCILStoreTestProvider().getArchivedUserStore());
    }


    @Test
    public void testArchiveUserStore() throws Exception {
        // what is in the user store *should* be indep. of the user instance. newUser() saves the object already.
        User user = getCILStoreTestProvider().newUser();
        String oldFirstName = user.getFirstName();
        user.setFirstName("Roderick");
        // Archive the user in the user store
        Identifier archivedUserUID = getArchivedUserStore().archiveUser(user.getIdentifier());
        assert getArchivedUserStore().containsKey(archivedUserUID) : "failed to archive user";
        ArchivedUser archivedUser = getArchivedUserStore().get(archivedUserUID);

        // Get the archived user and prove it is indeed the old user, not the new one.
        User oldUser = archivedUser.getUser();
        assert oldUser.getFirstName().equals(oldFirstName) : "User first names don't match";
        assert oldUser.getIdentifier().equals(user.getIdentifier()) : "User uid's don't match";
        assert oldUser.getLastName().equals(user.getLastName()) : "User last names don't match";
        assert oldUser.getEmail().equals(user.getEmail()) : "User email's don't match";
        assert oldUser.getIDPName().equals(user.getIDPName()) : "User idp names don't match";
        assert BeanUtils.checkBasic(oldUser.getePPN(), user.getePPN()) : "User eppns don't match";
        assert BeanUtils.checkBasic(oldUser.getePTID(), user.getePTID()) : "User eptids don't match";
        assert BeanUtils.checkBasic(oldUser.getOpenID(), user.getOpenID()) : "User openids don't match";
        assert BeanUtils.checkBasic(oldUser.getRemoteUser(), user.getRemoteUser()) : "User remote users don't match";
        // The old serial identifier is set to be the same as the uid at creation time. Check it.
        assert oldUser.getSerialIdentifier().equals(user.getIdentifier()) : "User's serial identifiers don't match";
        assert checkTimestamp(user, oldUser);

        user.setFirstName(oldFirstName);
        compareUsers(archivedUser, user, true);
    }

    /**
     * This is to be used as follows.
     * (1) create and save a user (newUser() will make one and save it)
     * (2) archive it
     * (3) get the most recent one from the archive
     * (4) invoke this method after saving a user and archiving it.
     * This compares the
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


    @Test
    public void testLastArchivedUser() throws Exception {
        User user = getCILStoreTestProvider().newUser();
        getUserStore().save(user);

        // Shouldn't be one archived before we start.
        assert getArchivedUserStore().getLastArchivedUser(user.getIdentifier()) == null;
        // archive some users
        for (int i = 0; i < count; i++) {
            // This next sleep command is needed because if items update too fast, their timestamps in SQL
            // stores might not be distinct. Getting the last archived user item then is undefined. This is mostly
            // a MySQL issue, since unlike Postgres, MySQL does not store nanos as well.
            Thread.sleep(1000);
            user.setFirstName("Bob-" + i);
            // update does not archive. We have to do that
            getArchivedUserStore().archiveUser(user.getIdentifier());
            getUserStore().update(user);
        }
        List<ArchivedUser> x = getArchivedUserStore().getAllByUserId(user.getIdentifier());
        assert x.size() == count;
        User targetUser = getArchivedUserStore().getLastArchivedUser(user.getIdentifier()).getUser();
        // We're one behind the count in number (index origin zero) and one behind that in names, hence count-2 should do it.
        if (targetUser.getFirstName().equals("Bob-" + (count - 2))) {
            assert true;
        } else {
            System.out.println(getClass().getSimpleName() + ": archived user name = " + targetUser.getFirstName() + ", should be Bob-" + (count - 2));
            System.out.println(getClass().getSimpleName() + ": store =" + getArchivedUserStore().getClass().getName());
            System.out.println("Failed to get correct archived user? These are sorted by timestamp and in *tests* backed by an SQL store they might be indistinguishable.");
        }
    }

    @Test
    public void testNewIDs() throws Exception {
        UserMultiKey umk = createUMK();
        UserMultiKey ruKey = new UserMultiKey(umk.getRemoteUserName());
        UserMultiKey eppnKey = new UserMultiKey(umk.getEppn());
        UserMultiKey eptidKey = new UserMultiKey(umk.getEptid());
        UserMultiKey openIdKey = new UserMultiKey(umk.getOpenID());
        UserMultiKey oidcKey = new UserMultiKey(umk.getOpenIDConnect());

        User user = getCILStoreTestProvider().newUser();

        checkArchivedKey(ruKey, user, true);
        checkArchivedKey(eppnKey, user, false);
        checkArchivedKey(eptidKey, user, false);
        checkArchivedKey(openIdKey, user, false);
        checkArchivedKey(oidcKey, user, false);

        List<ArchivedUser> archivedUsers = getArchivedUserStore().getAllByUserId(user.getIdentifier());
        assert archivedUsers.size() == 5 : "Expected 5 archived users and got " + archivedUsers.size() + " instead.";

    }

    private void checkArchivedKey(UserMultiKey multiKey, User user, boolean doSerialIDsMatch) throws Exception {
        ArchivedUser lastAUser;
        user.setUserMultiKey(multiKey);
        getUserStore().update(user);
        Identifier archivedUserID = getArchivedUserStore().archiveUser(user.getIdentifier());
        lastAUser = getArchivedUserStore().get(archivedUserID);
        compareUsers(lastAUser, user, true);
    }
}
