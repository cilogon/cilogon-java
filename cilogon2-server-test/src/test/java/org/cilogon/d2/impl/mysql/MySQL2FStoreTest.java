package org.cilogon.d2.impl.mysql;

import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.TwoFactorStoreTest;
import org.cilogon.d2.twofactor.TwoFactorStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  2:12 PM
 */
public class MySQL2FStoreTest extends TwoFactorStoreTest {
    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getMySQLStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return TwoFactorStore.class;
    }
}
