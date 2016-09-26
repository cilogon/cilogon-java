package org.cilogon.d2.storage.impl.sql;

import edu.uiuc.ncsa.myproxy.oa4mp.server.storage.sql.DSSQLTransactionStore;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.cilogon.d2.util.CILogonServiceTransaction;

/**
 * <p>Created by Jeff Gaynor<br>
 * on May 18, 2011 at  4:31:29 PM
 */
public class CILSQLTransactionStore extends DSSQLTransactionStore<CILogonServiceTransaction> {


    public CILSQLTransactionStore(TokenForge tokenForge,
                                  ConnectionPool connectionPool,
                                  Table table,
                                  IdentifiableProviderImpl<CILogonServiceTransaction> idp,
                                  MapConverter converter) {
        super(tokenForge,  connectionPool, table, idp, converter);
    }

}
