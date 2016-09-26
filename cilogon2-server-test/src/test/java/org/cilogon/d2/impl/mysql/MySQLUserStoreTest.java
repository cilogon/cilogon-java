package org.cilogon.d2.impl.mysql;

import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.UserStoreTest;
import org.cilogon.d2.storage.impl.sql.CILSQLUserStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/15/12 at  12:09 PM
 */
public class MySQLUserStoreTest extends UserStoreTest {
    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getMySQLStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return CILSQLUserStore.class;
    }
}
