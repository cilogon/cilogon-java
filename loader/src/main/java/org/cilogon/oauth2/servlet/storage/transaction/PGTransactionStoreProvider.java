package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.oa4mp.delegation.common.token.TokenForge;
import org.oa4mp.server.api.storage.MultiDSClientStoreProvider;

import javax.inject.Provider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/9/12 at  1:46 PM
 */
public class PGTransactionStoreProvider extends CILSQLTransactionStoreProvider{
    public PGTransactionStoreProvider(CFNode config, ConnectionPoolProvider<? extends ConnectionPool> cpp, String type, Provider<TokenForge> tokenForgeProvider, Provider<? extends CILogonServiceTransaction> tp, MultiDSClientStoreProvider cs, MapConverter converter) {
        super(config, cpp, type, tokenForgeProvider, tp, cs, converter);
    }

    @Override
    public CILSQLTransactionStore get() {
       CILogonServiceTransactionTable t = new CILogonServiceTransactionTable((CILTransactionKeys) converter.keys, getSchema(), getPrefix(), getTablename());
        return newInstance(t);
    }
}
