package org.cilogon.oauth2.servlet.storage.archiveUser;

import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.SQLStoreProvider;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.storage.user.UserKeys;
import org.cilogon.oauth2.servlet.storage.user.UserTable;
import org.oa4mp.server.api.OA4MPConfigTags;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  7:52 PM
 */
public class CILSQLArchivedUserStoreProvider extends SQLStoreProvider<ArchivedUserStore> implements OA4MPConfigTags {
    public CILSQLArchivedUserStoreProvider(ConfigurationNode config, ConnectionPoolProvider<? extends ConnectionPool> cpp,
                                           String type,
                                           IdentifiableProviderImpl<ArchivedUser> archivedUserProvider,
                                           ArchivedUserConverter c, UserTable userTable
    ) {
        super(config, cpp, type, ARCHIVED_USERS, ArchivedUsersTable.DEFAULT_TABLENAME, c);
        this.archivedUserProvider = archivedUserProvider;
        this.userTable = userTable;
    }

    IdentifiableProviderImpl<ArchivedUser> archivedUserProvider;
     UserTable userTable = null;

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
                new UserTable(new UserKeys(),getSchema(), getPrefix(), userTable.getTablename()));
        return newInstance(aut);
    }
}
