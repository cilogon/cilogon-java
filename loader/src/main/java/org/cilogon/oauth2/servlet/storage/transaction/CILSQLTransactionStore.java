package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.oa4mp.delegation.common.token.TokenForge;
import org.oa4mp.server.api.admin.transactions.DSSQLTransactionStore;

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
