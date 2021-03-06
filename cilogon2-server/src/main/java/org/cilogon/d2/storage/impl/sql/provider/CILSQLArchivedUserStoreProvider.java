package org.cilogon.d2.storage.impl.sql.provider;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.SQLStoreProvider;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.storage.ArchivedUserKeys;
import org.cilogon.d2.storage.impl.sql.CILSQLArchivedUserStore;
import org.cilogon.d2.storage.impl.sql.table.ArchivedUsersTable;
import org.cilogon.d2.storage.impl.sql.table.UserTable;
import org.cilogon.d2.util.ArchivedUserConverter;
import org.cilogon.d2.util.ArchivedUserStore;
import org.cilogon.d2.util.UserKeys;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  7:52 PM
 */
public class CILSQLArchivedUserStoreProvider extends SQLStoreProvider<ArchivedUserStore> implements OA4MPConfigTags {
    public CILSQLArchivedUserStoreProvider(ConfigurationNode config, ConnectionPoolProvider<? extends ConnectionPool> cpp,
                                           String type,
                                           IdentifiableProviderImpl<ArchivedUser> archivedUserProvider,
                                           ArchivedUserConverter c
    ) {
        super(config, cpp, type, ARCHIVED_USERS, ArchivedUsersTable.DEFAULT_TABLENAME, c);
        this.archivedUserProvider = archivedUserProvider;
    }

    IdentifiableProviderImpl<ArchivedUser> archivedUserProvider;


    @Override
    public ArchivedUserStore newInstance(Table table) {
        return new CILSQLArchivedUserStore(getConnectionPool(),
                table,
                archivedUserProvider, converter);
    }

    @Override
    public ArchivedUserStore get() {
        ArchivedUsersTable aut = new ArchivedUsersTable((ArchivedUserKeys)converter.keys,
                getSchema(),
                getPrefix(),
                getTablename(),
                new UserTable(new UserKeys(),getSchema(), getPrefix(), UserTable.DEFAULT_TABLE_NAME));
        return newInstance(aut);
    }
}
