package org.cilogon.d2.impl;

import org.cilogon.d2.CILStoreTest;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.twofactor.TwoFactorInfo;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.junit.Test;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  1:40 PM
 */
public abstract class TwoFactorStoreTest extends CILStoreTest {
    public TwoFactorStore get2FStore() throws Exception {
        return getCILStoreTestProvider().getTwoFactorStore();
    }

    public UserStore getUserStore() throws Exception {
        return getCILStoreTestProvider().getUserStore();
    }

    @Override
    public void checkStoreClass() throws Exception {
        testClassAsignability(get2FStore());
    }

    @Test
    public void testPutInfo() throws Exception {

        User user = getUserStore().create(true);
        user.setFirstName("Relth");
        user.setLastName("Gryzaackxs-" + getRandomString(8));
        getUserStore().save(user);

        TwoFactorInfo info = get2FStore().create();
        info.setIdentifier(user.getIdentifier());
        String infoString = getRandomString(256);
        info.setInfo(infoString);
        get2FStore().save(info);

        TwoFactorInfo info2 = get2FStore().get(info.getIdentifier());
        assert info2.getIdentifier().equals(info.getIdentifier()) : "Identifiers did not match";
        assert info2.getInfo().equals(info.getInfo()) : "info did not match";
    }
}
