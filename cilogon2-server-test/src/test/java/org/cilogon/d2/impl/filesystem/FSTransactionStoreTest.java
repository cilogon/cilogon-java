package org.cilogon.d2.impl.filesystem;

import edu.uiuc.ncsa.security.storage.FileStore;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.impl.ServiceTransactionStoreTest;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/17/12 at  12:24 PM
 */
public class FSTransactionStoreTest extends ServiceTransactionStoreTest {
    @Override
    protected Class getStoreClass() {
        return FileStore.class;
    }

    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getFsStoreProvider();
    }
}
