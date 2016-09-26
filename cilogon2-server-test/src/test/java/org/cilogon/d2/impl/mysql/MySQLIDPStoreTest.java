package org.cilogon.d2.impl.mysql;

import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.IdentityProviderTest;
import org.cilogon.d2.storage.impl.sql.CILSQLIdentityProviderStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/15/12 at  12:13 PM
 */
public class MySQLIDPStoreTest extends IdentityProviderTest {
        @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getMySQLStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return CILSQLIdentityProviderStore.class;
    }

}
