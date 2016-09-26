package org.cilogon.d2.impl.filesystem;

import edu.uiuc.ncsa.security.storage.FileStore;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.UserStoreTest;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  3:17 PM
 */
public class FSUserStoreTest extends UserStoreTest {
    @Override
    protected Class getStoreClass() {
        return FileStore.class;
    }

    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getFsStoreProvider();
    }
}
