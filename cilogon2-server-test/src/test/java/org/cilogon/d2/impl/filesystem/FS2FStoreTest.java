package org.cilogon.d2.impl.filesystem;

import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.TwoFactorStoreTest;
import org.cilogon.d2.twofactor.TwoFactorStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  1:48 PM
 */
public class FS2FStoreTest extends TwoFactorStoreTest {

      @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getFsStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return TwoFactorStore.class;
    }
}
