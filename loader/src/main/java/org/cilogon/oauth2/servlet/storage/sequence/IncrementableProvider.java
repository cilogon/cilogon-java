package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.core.configuration.provider.CfgEvent;
import edu.uiuc.ncsa.security.core.configuration.provider.TypedProvider;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.cilogon.oauth2.servlet.util.Incrementable;

import static edu.uiuc.ncsa.security.storage.sql.SQLStoreProvider.*;
import static org.oa4mp.server.api.OA4MPConfigTags.SEQUENCE;


/**
 * Supplies a single incrementable, e.g., subclasses exist for file, sql-backed, etc.
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  9:52 AM
 */
public abstract class IncrementableProvider extends TypedProvider<Incrementable> {
    ConnectionPoolProvider<? extends ConnectionPool> connectionPoolProvider;

    protected IncrementableProvider(CFNode config, String type) {
        super(config, type, SEQUENCE);
    }

    public IncrementableProvider(CFNode config,
                                  String type,
                                  ConnectionPoolProvider<? extends ConnectionPool> connectionPoolProvider) {
         this(config, type);
         this.connectionPoolProvider = connectionPoolProvider;
     }

     protected ConnectionPool getConnectionPool() {
         if (connectionPoolProvider.getCFNode() == null) {
             connectionPoolProvider.setCFNode(getCFNode().getParent());
         }
         return connectionPoolProvider.get();
     }

    @Override
    public Object componentFound(CfgEvent configurationEvent) {
        if (checkEvent(configurationEvent)) {
            return get();
        }
        return null;
    }

    public String getSchema() {
        return getTypeAttribute(SCHEMA);
    }

    public String getPrefix() {
        return getTypeAttribute(PREFIX);
    }

    String tablename = "uid_seq";
    /**
     * Return the configured tablename if there is one, otherwise return the default.
     * @return
     */
    public String getTablename() {
        return getAttribute(TABLENAME, tablename);
    }
}
