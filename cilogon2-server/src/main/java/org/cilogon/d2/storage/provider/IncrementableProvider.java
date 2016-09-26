package org.cilogon.d2.storage.provider;

import edu.uiuc.ncsa.security.core.configuration.provider.CfgEvent;
import edu.uiuc.ncsa.security.core.configuration.provider.TypedProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.util.Incrementable;

import static edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags.SEQUENCE;
import static edu.uiuc.ncsa.security.storage.sql.SQLStoreProvider.*;


/**
 * Supplies a single incrementable, e.g., subclasses exist for file, sql-backed, etc.
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  9:52 AM
 */
public abstract class IncrementableProvider extends TypedProvider<Incrementable> {

    protected IncrementableProvider(ConfigurationNode config, String type) {
        super(config, type, SEQUENCE);
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
