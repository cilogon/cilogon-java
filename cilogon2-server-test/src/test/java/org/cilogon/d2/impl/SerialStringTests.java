package org.cilogon.d2.impl;

import edu.uiuc.ncsa.security.storage.XMLMap;
import org.cilogon.d2.CILTestStoreProviderI2;
import org.cilogon.d2.RemoteDBServiceTest;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.storage.*;
import org.cilogon.d2.util.UserConverter;
import org.cilogon.d2.util.UserKeys;
import org.junit.Test;

import static org.cilogon.d2.servlet.AbstractDBService.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/24/20 at  2:12 PM
 */
public class SerialStringTests extends RemoteDBServiceTest {
    @Test
    public void testAll() throws Exception {
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMemoryStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getFsStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMySQLStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getPgStoreProvider());
    }


    public void doTests(CILTestStoreProviderI2 provider) throws Exception {
        updateNoNewSSTest(provider.getUserStore());
    }

    public void updateNoNewSSTest(UserStore userStore) throws Exception {
        String r = getRandomString();
        EduPersonTargetedID eptid = new EduPersonTargetedID("https://idp.bigstate.edu/idp/shibboleth!https://cilogon.org/shibboleth!625895E904172B7E4096063C3C447A91B0F14");
        EduPersonPrincipleName eppn = new EduPersonPrincipleName("bob" + r + "@bigstate.edu");
        UserMultiID umk = new UserMultiID(null, eppn, eptid, null, null);
        XMLMap userMap = new XMLMap();

        UserKeys userKeys = new UserKeys();
        userMap.put(userKeys.eptid, "https://idp.bigstate.edu/idp/shibboleth!" + r);
        userMap.put(userKeys.idp, "https://idp.bigstate.edu/idp/shibboleth");
        UserConverter userConverter = new UserConverter(userKeys, null);
        // Here's how ti works: Update various fields that do not allow the user to get a new serial string
        // and double check that the response is "new user" first time and "ok" after that.


        XMLMap map = getDBSClient().doGet(GET_USER, userMap);
        String oldSS = map.getString(userKeys.serialString());
        assert getStatusKey(map) == STATUS_NEW_USER;
        userMap.put(userKeys.email(), "bob" + r + "@bigstate.edu");
        map = getDBSClient().doGet(GET_USER, userMap);
        User user = new User(null,null);
        userConverter.fromMap(map, user);
        assert !user.canGetCert();
        assert getStatusKey(map) == STATUS_OK;
        assert map.getString(userKeys.serialString()).equals(oldSS);
        userMap.put(userKeys.email(), "bob2" + r + "@bigstate.edu");
        map = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(map) == STATUS_OK; // no change
        assert map.getString(userKeys.serialString()).equals(oldSS);

        userMap.put(userKeys.lastName(), "Chang");
        map = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(map) == STATUS_OK; // no change
        assert map.getString(userKeys.serialString()).equals(oldSS);

        // Now we add a first name and the status should change to updated but the serial string should not increment.

        userMap.put(userKeys.firstName(), "Bob");
        map = getDBSClient().doGet(GET_USER, userMap);
        userConverter.fromMap(map, user);
        assert !user.canGetCert();
        assert getStatusKey(map) == STATUS_OK; // user updated = serial string
        assert map.getString(userKeys.serialString()).equals(oldSS);

        userMap.put(userKeys.idpDisplayName(), "Big State University");
        map = getDBSClient().doGet(GET_USER, userMap);
        userConverter.fromMap(map, user);
        assert user.canGetCert();
        assert getStatusKey(map) == STATUS_OK; // user updated = serial string
        assert map.getString(userKeys.serialString()).equals(oldSS);

        userMap.put(userKeys.firstName(), "Robert");
        map = getDBSClient().doGet(GET_USER, userMap);
        userConverter.fromMap(map, user);
        assert user.canGetCert();
        assert getStatusKey(map) == STATUS_USER_UPDATED; // user updated = serial string
        System.out.println("new serial string = " + map.getString(userKeys.serialString()) + ", old = " + oldSS);
        assert !map.getString(userKeys.serialString()).equals(oldSS);

    }
}
