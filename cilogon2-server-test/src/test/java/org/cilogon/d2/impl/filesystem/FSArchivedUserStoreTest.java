package org.cilogon.d2.impl.filesystem;

import edu.uiuc.ncsa.security.storage.FileStore;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.impl.ArchivedUserStoreTest;
import org.junit.Test;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  3:20 PM
 */
public class FSArchivedUserStoreTest extends ArchivedUserStoreTest {

    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getFsStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return FileStore.class;
    }

    @Test
    public void testStoreType() throws Exception{
        assert getTSProvider().getIDP() instanceof FileStore: "The IDP store is not a FileStore";
        assert getTSProvider().getUserStore() instanceof FileStore : "The user store is not a FileStore";
        assert getTSProvider().getClientApprovalStore() instanceof FileStore : "The client approval store is not a FileStore";
        assert getTSProvider().getTransactionStore() instanceof FileStore : "The transaction store is not a FileStore";
    }
}
