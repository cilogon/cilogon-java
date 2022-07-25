package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;

import java.sql.Types;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/16/21 at  10:53 PM
 */
public class DerbySequenceTable extends SequenceTable {
    String dummyColumn = "dummy";

    public DerbySequenceTable(SequenceKeys keys, String schema, String tablenamePrefix, String tablename) {
        super(keys, schema, tablenamePrefix, tablename);
    }

    @Override
      public void createColumnDescriptors() {
          super.createColumnDescriptors();
          getColumnDescriptor().add(new ColumnDescriptorEntry(dummyColumn, Types.INTEGER, false, false));

      }
      /*
CREATE TABLE ciloa2.uid_seq
(
    nextval INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    dummy varchar(16)
);

       */
    @Override
    public String createTableStatement(int startValue) {
        return " CREATE TABLE " + getFQTablename() +
                "(" + getPrimaryKeyColumnName() + "  INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH " + startValue + ", INCREMENT BY 1)," +
                 dummyColumn + "  varchar(16));";
    }

    @Override
    public String nextValueStatement() {
        String x= "insert into " + getFQTablename() + " (" + dummyColumn + ") values (0)";
        return x;
    }

    @Override
    public String dropStatement() {
        return "drop table " + getFQTablename();
    }
}
