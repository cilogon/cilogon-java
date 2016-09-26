package org.cilogon.d2.storage.impl.postgres.provider;

import edu.uiuc.ncsa.myproxy.oa4mp.server.storage.MultiDSClientStoreProvider;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.impl.sql.CILSQLTransactionStore;
import org.cilogon.d2.storage.impl.sql.provider.CILSQLTransactionStoreProvider;
import org.cilogon.d2.storage.impl.sql.table.CILogonServiceTransactionTable;
import org.cilogon.d2.util.CILTransactionKeys;
import org.cilogon.d2.util.CILogonServiceTransaction;

import javax.inject.Provider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/9/12 at  1:46 PM
 */
public class PGTransactionStoreProvider extends CILSQLTransactionStoreProvider{
    public PGTransactionStoreProvider(ConfigurationNode config, ConnectionPoolProvider<? extends ConnectionPool> cpp, String type, Provider<TokenForge> tokenForgeProvider, Provider<? extends CILogonServiceTransaction> tp, MultiDSClientStoreProvider cs, MapConverter converter) {
        super(config, cpp, type, tokenForgeProvider, tp, cs, converter);
    }

    @Override
    public CILSQLTransactionStore get() {
       CILogonServiceTransactionTable t = new CILogonServiceTransactionTable((CILTransactionKeys) converter.keys, getSchema(), getPrefix(), getTablename());
        return newInstance(t);
    }
}
