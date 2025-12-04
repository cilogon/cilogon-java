package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.oa4mp.delegation.common.token.TokenForge;
import org.oa4mp.server.api.storage.MultiDSClientStoreProvider;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2SQLTStore;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2SQLTransactionStoreProvider;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2ServiceTransaction;

import javax.inject.Provider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/13/15 at  10:03 AM
 */
public class CILOA2TransactionstoreProvider<T extends OA2SQLTStore> extends OA2SQLTransactionStoreProvider<T> {
    public CILOA2TransactionstoreProvider(CFNode config,
                                          ConnectionPoolProvider<? extends ConnectionPool> cpp,
                                          String type,
                                          MultiDSClientStoreProvider clientStoreProvider,
                                          Provider<? extends OA2ServiceTransaction> tp,
                                          Provider<TokenForge> tfp,
                                          MapConverter converter) {
        super(config, cpp, type, clientStoreProvider, tp, tfp, converter);
    }

    @Override
    public T get() {
        return newInstance(new CILOA2TransactionTable((CILOA2TransactionKeys) converter.keys, getSchema(), getPrefix(), getTablename()));
    }

    @Override
    public T newInstance(Table table) {
        return (T) new OA2SQLTStore(tokenForgeProvider.get(),
                getConnectionPool(),
                table,
                transactionProvider, converter);
    }
}
