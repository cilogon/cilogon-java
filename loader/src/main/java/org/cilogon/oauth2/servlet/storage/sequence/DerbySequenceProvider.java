package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.util.Incrementable;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/17/21 at  6:15 AM
 */
public class DerbySequenceProvider extends IncrementableProvider implements OA4MPConfigTags {

    public DerbySequenceProvider(ConfigurationNode config, ConnectionPoolProvider<? extends ConnectionPool> connectionPoolProvider) {
           this(config, DERBY_STORE, connectionPoolProvider);
    }

    public DerbySequenceProvider(ConfigurationNode config, String type, ConnectionPoolProvider<? extends ConnectionPool> connectionPoolProvider) {
        super(config, type, connectionPoolProvider);
    }


    @Override
    public Incrementable get() {
        DerbySequenceTable t = new DerbySequenceTable(
                new SequenceTable.SequenceKeys(),
                getSchema(),
                getPrefix(),
                getTablename());
        return new DerbySequence(getConnectionPool(), t);
    }
}
