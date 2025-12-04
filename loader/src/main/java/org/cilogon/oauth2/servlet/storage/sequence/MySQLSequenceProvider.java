package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.oa4mp.server.api.OA4MPConfigTags;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  10:06 AM
 */
public class MySQLSequenceProvider extends IncrementableProvider implements OA4MPConfigTags {

    public MySQLSequenceProvider(CFNode cn, ConnectionPoolProvider<? extends ConnectionPool> cpp) {
        this(cn, MYSQL_STORE, cpp);
    }

    public MySQLSequenceProvider(CFNode config,
                                 String type,
                                 ConnectionPoolProvider<? extends ConnectionPool> connectionPoolProvider) {
        super(config, type, connectionPoolProvider);
    }

    @Override
    public Sequence get() {
          MySQLSequenceTable t = new MySQLSequenceTable(
                new SequenceTable.SequenceKeys(),
                getSchema(),
                getPrefix(),
                getTablename());
        return new MySQLSequence(getConnectionPool(), t);

    }
}
