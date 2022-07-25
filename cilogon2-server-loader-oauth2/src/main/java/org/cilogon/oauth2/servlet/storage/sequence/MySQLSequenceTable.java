package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;

import java.sql.Types;

/**
 * In order to use an auto increment column in MySQL, you must use a regular table and
 * have an extra, dummy column. You insert a dummy value into that then read off the returned
 * auto increment value (this value should be the primary key)
 * as a side effect. while it is possible to have
 * a single auto increment column as the complete table, there is no way
 * to actually then update it. MySQL requires another column which is supplied
 * here.
 * <p>Created by Jeff Gaynor<br>
 * on 5/9/12 at  11:50 AM
 */
public class MySQLSequenceTable extends SequenceTable {
    public MySQLSequenceTable(SequenceKeys keys, String schema, String tablenamePrefix, String tablename) {
        super(keys, schema, tablenamePrefix, tablename);
    }

    @Override
    public void createColumnDescriptors() {
        super.createColumnDescriptors();
        getColumnDescriptor().add(new ColumnDescriptorEntry(dummyColumn, Types.VARCHAR, false, false));

    }

    String dummyColumn = "dummy";
    protected String dummyColumn(String ...x){
        if(0 < x.length) dummyColumn = x[0];
        return dummyColumn;
    }

    @Override
    public String createTableStatement(int startValue) {
        String createString ="create table uid_seq (\n" +
                getPrimaryKeyColumnName() + " INT NOT NULL AUTO_INCREMENT,\n" +
                dummyColumn() + " tinyint,\n" +
                "PRIMARY KEY (" + getPrimaryKeyColumnName() + ")\n" +
                "   ) AUTO_INCREMENT=" + startValue + ";";
        return createString;
    }

    @Override
    public String nextValueStatement() {
        String x = "INSERT INTO " + getFQTablename() + " (" + dummyColumn() + ")";
        x = x + " values (0);"; // the dummy value.
        return x;
    }

    @Override
    public String dropStatement() {
        // In Mysql a sequence is an auto-increment column in a table, so drop the table.
        return "DROP TABLE " + getFQTablename();
    }
}
