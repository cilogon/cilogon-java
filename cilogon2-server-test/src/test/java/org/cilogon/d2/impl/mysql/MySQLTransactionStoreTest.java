package org.cilogon.d2.impl.mysql;

import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.ServiceTransactionStoreTest;
import org.cilogon.d2.storage.impl.sql.CILSQLTransactionStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/17/12 at  12:40 PM
 */
public class MySQLTransactionStoreTest extends ServiceTransactionStoreTest {
    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getMySQLStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return CILSQLTransactionStore.class;
    }
}
