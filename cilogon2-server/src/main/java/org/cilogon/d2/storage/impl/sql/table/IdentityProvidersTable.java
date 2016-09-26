package org.cilogon.d2.storage.impl.sql.table;

import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.cilogon.d2.util.IDPKeys;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Apr 13, 2010 at  2:07:37 PM
 */
public class IdentityProvidersTable extends Table {
    public IdentityProvidersTable(IDPKeys keys, String schema, String prefix, String name) {
        super(keys, schema, prefix, name);
        tablename = "identity_provider";
    }

    public String hasIdpStatement() {
        return "select * from " + getFQTablename() + " where " +
                keys.identifier() + " = ?";
    }
}
