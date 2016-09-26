package org.cilogon.d2.storage.impl.mysql.provider;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.Sequence;
import org.cilogon.d2.storage.impl.mysql.MySQLSequence;
import org.cilogon.d2.storage.impl.mysql.MySQLSequenceTable;
import org.cilogon.d2.storage.impl.sql.table.SequenceTable;
import org.cilogon.d2.storage.provider.IncrementableProvider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  10:06 AM
 */
public class MySQLSequenceProvider extends IncrementableProvider implements OA4MPConfigTags {
    ConnectionPoolProvider<? extends ConnectionPool> connectionPoolProvider;

    public MySQLSequenceProvider(ConfigurationNode cn, ConnectionPoolProvider<? extends ConnectionPool> cpp) {
        super(cn, MYSQL_STORE);
        connectionPoolProvider = cpp;
    }

      protected ConnectionPool getConnectionPool() {
        if (connectionPoolProvider.getConfig() == null) {
            connectionPoolProvider.setConfig(getTypeConfig());
        }
        return connectionPoolProvider.get();
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
