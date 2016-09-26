package org.cilogon.d2.storage.impl.postgres.provider;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.Sequence;
import org.cilogon.d2.storage.impl.postgres.PGSequenceTable;
import org.cilogon.d2.storage.impl.postgres.PostgresSequence;
import org.cilogon.d2.storage.impl.sql.table.SequenceTable;
import org.cilogon.d2.storage.provider.IncrementableProvider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  10:06 AM
 */
public class PGSequenceProvider extends IncrementableProvider implements OA4MPConfigTags {
    ConnectionPoolProvider<? extends ConnectionPool> connectionPoolProvider;

    public PGSequenceProvider(ConfigurationNode cn, ConnectionPoolProvider<? extends ConnectionPool> cpp) {
        super(cn, POSTGRESQL_STORE);
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
        PGSequenceTable t = new PGSequenceTable(
                new SequenceTable.SequenceKeys(),
                getSchema(),
                getPrefix(),
                getTablename());
        return new PostgresSequence(getConnectionPool(), t);

    }
}
