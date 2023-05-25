package test.cilogon.impl;

import edu.uiuc.ncsa.security.util.TestBase;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorStore;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.junit.Test;
import test.cilogon.CILTestStoreProviderI2;
import test.cilogon.ServiceTestUtils;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  1:40 PM
 */
public  class TwoFactorStoreTest extends TestBase {
    public void testAll() throws Exception {
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMemoryStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getFsStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getMySQLStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getPgStoreProvider());
        doTests((CILTestStoreProviderI2) ServiceTestUtils.getDerbyStoreProvider());
    }

    public void doTests(CILTestStoreProviderI2 provider) throws Exception {
           testPutInfo(provider.getUserStore(),provider.getTwoFactorStore());
    }

    @Test
    public void testPutInfo(UserStore userStore, TwoFactorStore twoFactorStore) throws Exception {

        User user = userStore.create(true);
        user.setFirstName("Relth");
        user.setLastName("Gryzaackxs-" + getRandomString(8));
        userStore.save(user);

        TwoFactorInfo info = twoFactorStore.create();
        info.setIdentifier(user.getIdentifier());
        String infoString = getRandomString(256);
        info.setInfo(infoString);
        twoFactorStore.save(info);

        TwoFactorInfo info2 = twoFactorStore.get(info.getIdentifier());
        assert info2.getIdentifier().equals(info.getIdentifier()) : "Identifiers did not match";
        assert info2.getInfo().equals(info.getInfo()) : "info did not match";
        // clean up
        userStore.remove(user.getIdentifier());
        twoFactorStore.remove(info.getIdentifier());
        twoFactorStore.remove(info2.getIdentifier());
    }
}
