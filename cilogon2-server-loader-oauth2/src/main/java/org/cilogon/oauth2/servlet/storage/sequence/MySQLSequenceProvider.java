package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  10:06 AM
 */
public class MySQLSequenceProvider extends IncrementableProvider implements OA4MPConfigTags {

    public MySQLSequenceProvider(ConfigurationNode cn, ConnectionPoolProvider<? extends ConnectionPool> cpp) {
        this(cn, MYSQL_STORE, cpp);
    }

    public MySQLSequenceProvider(ConfigurationNode config,
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
