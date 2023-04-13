package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.SQLStoreProvider;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.apache.commons.configuration.tree.ConfigurationNode;

import static org.cilogon.oauth2.servlet.storage.idp.CILSQLIdentityProviderStore.DEFAULT_TABLENAME;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  8:04 PM
 */
public class CILSQLIDPStoreProvider extends SQLStoreProvider<IdentityProviderStore> implements OA4MPConfigTags {
    public CILSQLIDPStoreProvider(ConfigurationNode config,
                                  ConnectionPoolProvider<? extends ConnectionPool> cpp,
                                  String type,
                                  IDPConverter c) {
        super(config, cpp, type, IDENTITY_PROVIDERS, DEFAULT_TABLENAME, c);
    }

    @Override
    public IdentityProviderStore newInstance(Table table) {
        return new CILSQLIdentityProviderStore(getConnectionPool(),
                table,
                new IDPProvider(), (IDPConverter) converter);
    }

    @Override
    public IdentityProviderStore get() {
        IdentityProvidersTable t = new IdentityProvidersTable(new IDPKeys(),
                getSchema(),
                getPrefix(),
                getTablename());
        return newInstance(t);
    }
}
