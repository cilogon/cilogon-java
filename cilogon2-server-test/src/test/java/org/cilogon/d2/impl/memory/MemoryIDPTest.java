package org.cilogon.d2.impl.memory;

import edu.uiuc.ncsa.security.storage.MemoryStore;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.IdentityProviderTest;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/14/12 at  11:04 AM
 */
public class MemoryIDPTest extends IdentityProviderTest {
   @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getMemoryStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return MemoryStore.class;
    }
}
