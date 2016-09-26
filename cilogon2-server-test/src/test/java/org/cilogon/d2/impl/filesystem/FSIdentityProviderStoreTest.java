package org.cilogon.d2.impl.filesystem;

import edu.uiuc.ncsa.security.storage.FileStore;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.IdentityProviderTest;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  4:41 PM
 */
public class FSIdentityProviderStoreTest extends IdentityProviderTest {
    @Override
    protected Class getStoreClass() {
        return FileStore.class;
    }

    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getFsStoreProvider();
    }
}
