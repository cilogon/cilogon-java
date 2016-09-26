package org.cilogon.d2.impl.postgres;

import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.TwoFactorStoreTest;
import org.cilogon.d2.twofactor.TwoFactorStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  3:11 PM
 */
public class PG2FStoreTest extends TwoFactorStoreTest {
    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getPgStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return TwoFactorStore.class;
    }
}
