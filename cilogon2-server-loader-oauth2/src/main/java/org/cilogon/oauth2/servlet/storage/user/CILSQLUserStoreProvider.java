package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.SQLStoreProvider;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.util.Incrementable;
import org.cilogon.oauth2.servlet.util.UserKeys;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  7:41 PM
 */
public class CILSQLUserStoreProvider extends SQLStoreProvider<CILSQLUserStore> implements OA4MPConfigTags {
    public CILSQLUserStoreProvider(
            ConfigurationNode config,
            ConnectionPoolProvider<? extends ConnectionPool> cpp,
            String type,
            IdentifiableProviderImpl<User> userProvider,
            MapConverter converter,
            Incrementable incrementable) {
        super(config, cpp, type, USERS, converter);
        this.userProvider = userProvider;
        this.incrementable = incrementable;
    }

                                       Incrementable incrementable;

    IdentifiableProviderImpl<User> userProvider;
    @Override
    public CILSQLUserStore newInstance(Table table) {
        return new CILSQLUserStore(getConnectionPool(),table,userProvider, converter, incrementable);
    }

    @Override
    public CILSQLUserStore get() {
        UserTable ut = new UserTable(new UserKeys(), getSchema(), getPrefix(), getTablename());
        return newInstance(ut);
    }
}
