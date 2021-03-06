package org.cilogon.d2.storage.impl.sql.provider;

import edu.uiuc.ncsa.myproxy.oa4mp.server.storage.MultiDSClientStoreProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.DSSQLTransactionStoreProvider;

import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.impl.sql.CILSQLTransactionStore;
import org.cilogon.d2.storage.impl.sql.table.CILogonServiceTransactionTable;
import org.cilogon.d2.storage.provider.CILTransactionProvider;
import org.cilogon.d2.util.CILTransactionKeys;
import org.cilogon.d2.util.CILogonServiceTransaction;

import javax.inject.Provider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  8:06 PM
 */
public class CILSQLTransactionStoreProvider extends DSSQLTransactionStoreProvider<CILSQLTransactionStore> {
    public CILSQLTransactionStoreProvider(ConfigurationNode config,
                                          ConnectionPoolProvider<? extends ConnectionPool> cpp,
                                          String type,
                                          Provider<TokenForge> tokenForgeProvider,
                                          Provider<? extends CILogonServiceTransaction> tp,
                                          MultiDSClientStoreProvider cs,
                                          MapConverter converter) {
        super(config, cpp, type, cs, tp, tokenForgeProvider, converter);
    }

    @Override
    public CILSQLTransactionStore newInstance(Table table) {
        return new CILSQLTransactionStore(tokenForgeProvider.get(),
                getConnectionPool(),
                table,
                new CILTransactionProvider(),
                converter);
    }

    @Override
    public CILSQLTransactionStore get() {
        CILogonServiceTransactionTable t = new CILogonServiceTransactionTable((CILTransactionKeys)converter.keys, getSchema(), getPrefix(), getTablename());
        return newInstance(t);
    }
}

