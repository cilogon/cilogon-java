package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;
import org.cilogon.d2.storage.impl.sql.table.UserTable;
import org.cilogon.d2.util.UserKeys;

import java.sql.Types;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/13/15 at  8:55 AM
 */
public class OA2UserTable extends UserTable {
    public OA2UserTable(UserKeys keys, String schema, String tablenamePrefix, String tablename) {
        super(keys, schema, tablenamePrefix, tablename);
    }

    protected OA2UserKeys getUKeys() {
        return (OA2UserKeys) keys;
    }

    @Override
    public void createColumnDescriptors() {
        super.createColumnDescriptors();
        getColumnDescriptor().add(new ColumnDescriptorEntry(getUKeys().affiliation(), Types.LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(getUKeys().displayName(), Types.LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(getUKeys().organizationalUnit(), Types.LONGVARCHAR));


    }
}
