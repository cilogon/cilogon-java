package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.transactions.OA2TransactionTable;
import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;

import java.sql.Types;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/12/15 at  3:33 PM
 */
public class CILOA2TransactionTable extends OA2TransactionTable {
    public CILOA2TransactionTable(CILOA2TransactionKeys keys, String schema, String tablenamePrefix, String tablename) {
        super(keys, schema, tablenamePrefix, tablename);
    }

    protected CILOA2TransactionKeys getCKeys(){
        return (CILOA2TransactionKeys) keys;
    }
    @Override
      public void createColumnDescriptors() {
          super.createColumnDescriptors();
          getColumnDescriptor().add(new ColumnDescriptorEntry(getCKeys().LOA(), Types.LONGVARCHAR));
          getColumnDescriptor().add(new ColumnDescriptorEntry(getCKeys().affiliation(), Types.LONGVARCHAR));
          getColumnDescriptor().add(new ColumnDescriptorEntry(getCKeys().organizationalUnit(), Types.LONGVARCHAR));
          getColumnDescriptor().add(new ColumnDescriptorEntry(getCKeys().displayName(), Types.LONGVARCHAR));
          getColumnDescriptor().add(new ColumnDescriptorEntry(getCKeys().userUID(), Types.LONGVARCHAR));
      }
}
