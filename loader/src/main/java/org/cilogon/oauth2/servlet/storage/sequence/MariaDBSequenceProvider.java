package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 9/23/14 at  10:21 AM
 */
public class MariaDBSequenceProvider extends MySQLSequenceProvider {
    public MariaDBSequenceProvider(CFNode cn, ConnectionPoolProvider<? extends ConnectionPool> cpp) {
        super(cn, MARIADB_STORE, cpp);
    }
}
