package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.DSTransactionTable;
import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;
import org.cilogon.oauth2.servlet.util.CILTransactionKeys;

import static java.sql.Types.BOOLEAN;
import static java.sql.Types.LONGVARCHAR;

/**
 * <p>Created by Jeff Gaynor<br>
 * on May 18, 2011 at  4:18:00 PM
 */

/*
  KLUDGE: In order to keep CILogon 1 running at the same time as CILogon 2, we have to re-use the database tables.
  We CANNOT remove the information from it since the original service must continue to run without change.
  We also must track which portal does what, therefore, we have to add a column, portal_id, to the transaction table.
  In practice, the initial request will be processed and all the information will be added to the table since
  CILogon 1 still needs it (including a human readable name, portal_name and the success as well as error urls.)

  Later, remove error, success and portal name columns. Just have new dbservice do a lookup of these from the
  portal_id column.
 */
public class CILogonServiceTransactionTable extends DSTransactionTable {
    public CILogonServiceTransactionTable(CILTransactionKeys keys, String schema, String tablenamePrefix, String tablename) {
       super(keys, schema, tablenamePrefix, tablename);
    }

    @Override
    public void createColumnDescriptors() {
        super.createColumnDescriptors();
        CILTransactionKeys x = (CILTransactionKeys) keys;
        getColumnDescriptor().add(new ColumnDescriptorEntry(x.tempCredSS(), LONGVARCHAR, true, false));
        getColumnDescriptor().add(new ColumnDescriptorEntry(x.accessTokenSS(), LONGVARCHAR, true, false));
        getColumnDescriptor().add(new ColumnDescriptorEntry(x.complete(), BOOLEAN, true, false));
        getColumnDescriptor().add(new ColumnDescriptorEntry(x.LOA(), LONGVARCHAR, true, false));
        getColumnDescriptor().add(new ColumnDescriptorEntry(x.affiliation(), LONGVARCHAR, true, false));
        getColumnDescriptor().add(new ColumnDescriptorEntry(x.organizationalUnit(), LONGVARCHAR, true, false));
        getColumnDescriptor().add(new ColumnDescriptorEntry(x.displayName(), LONGVARCHAR, true, false));
    }
}
