package org.cilogon.oauth2.servlet.storage.twofactor;

import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.SQLStoreProvider;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.oa4mp.server.api.OA4MPConfigTags;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  1:12 PM
 */
public class CILSQL2FStoreProvider extends SQLStoreProvider<TwoFactorSQLStore> implements OA4MPConfigTags {
    TwoFactorInfoProvider twoFactorInfoProvider;

    public CILSQL2FStoreProvider(ConfigurationNode config,
                                 ConnectionPoolProvider<? extends ConnectionPool> cpp,
                                 String type,
                                 MapConverter converter,
                                 TwoFactorInfoProvider twoFactorInfoProvider) {
        super(config, cpp, type, TWO_FACTOR, converter);
        this.twoFactorInfoProvider = twoFactorInfoProvider;

    }

    @Override
    public TwoFactorSQLStore newInstance(Table table) {
        return new TwoFactorSQLStore(getConnectionPool(), table, twoFactorInfoProvider, converter);
    }

    @Override
    public TwoFactorSQLStore get() {
        TwoFactorTable table = new TwoFactorTable(new TwoFactorSerializationKeys(),
                getSchema(),
                getPrefix(),
                getTablename());
        return newInstance(table);
    }
}
