package org.cilogon.d2.impl.mysql;

import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.ArchivedUserStoreTest;
import org.cilogon.d2.storage.impl.sql.CILSQLArchivedUserStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/15/12 at  12:12 PM
 */
public class MySQLArchivedUserStoreTest extends ArchivedUserStoreTest {
        @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getMySQLStoreProvider();
    }
    @Override
    protected Class getStoreClass() {
        return CILSQLArchivedUserStore.class;
    }

}
