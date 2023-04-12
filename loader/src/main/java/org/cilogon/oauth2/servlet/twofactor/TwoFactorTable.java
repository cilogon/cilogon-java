package org.cilogon.oauth2.servlet.twofactor;

import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;

import static java.sql.Types.LONGVARCHAR;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  10:22 AM
 */
public class TwoFactorTable extends Table {

    public static final String DEFAULT_TABLENAME = "two_factor";
    public TwoFactorTable(TwoFactorSerializationKeys keys, String schema, String tablenamePrefix, String tablename) {
        super(keys, schema, tablenamePrefix, tablename);
        if (this.tablename == null) {
            this.tablename = DEFAULT_TABLENAME; // set the default
        }
    }

    protected TwoFactorSerializationKeys tfk() {
        return (TwoFactorSerializationKeys) keys;
    }

    @Override
    public void createColumnDescriptors() {
        super.createColumnDescriptors();
        getColumnDescriptor().add(new ColumnDescriptorEntry(tfk().info, LONGVARCHAR));
    }
}
