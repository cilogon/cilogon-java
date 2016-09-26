package org.cilogon.d2.impl.memory;

import edu.uiuc.ncsa.security.storage.MemoryStore;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.impl.ServiceTransactionStoreTest;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/17/12 at  12:09 PM
 */
public class MemoryTransactionStoreTest extends ServiceTransactionStoreTest {
    public CILTestStoreProvider getTSProvider(){
        return (CILTestStoreProvider) ServiceTestUtils.getMemoryStoreProvider();
    }
    @Override
    protected Class getStoreClass() {
        return MemoryStore.class;
    }

}
