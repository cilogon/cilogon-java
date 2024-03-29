package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.storage.sequence.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  10:06 AM
 */
public class PGSequenceProvider extends IncrementableProvider implements OA4MPConfigTags {

    public PGSequenceProvider(ConfigurationNode cn, ConnectionPoolProvider<? extends ConnectionPool> cpp) {
        super(cn, POSTGRESQL_STORE, cpp);
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
