package org.cilogon.d2.storage.impl.postgres;

import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;
import org.cilogon.d2.storage.impl.sql.table.SequenceTable;

import java.sql.Types;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Apr 13, 2010 at  3:28:34 PM
 */
public class PGSequenceTable extends SequenceTable {
    public PGSequenceTable(SequenceKeys keys, String schema, String prefix, String name) {
        super(keys, schema, prefix, name);
        tablename = "uid_seq";
    }

    @Override
    public void createColumnDescriptors() {
        getColumnDescriptor().add((new ColumnDescriptorEntry("nextval", Types.BIGINT, false, true)));
    }

    @Override
    public String getPrimaryKeyColumnName() {
        return "nextval";
    }


    public String createTableStatement() {
        return createTableStatement(42);
    }

    @Override
    public String createTableStatement(int startValue) {
        return "CREATE SEQUENCE " + getFQTablename() + "\n" +
                "  INCREMENT 1\n" +
                "  MINVALUE 1\n" +
                "  MAXVALUE 9223372036854775807\n" +
                "  START " + Integer.toString(startValue) + "\n" +
                "  CACHE 1;";
    }

    @Override
    public String nextValueStatement() {
        return "select nextval('" + getFQTablename() + "')";
    }

    @Override
    public String dropStatement() {
        // In Postgres a sequence is a specific entity that is managed separately from tables.
        return "Drop SEQUENCE " + getFQTablename();

    }
}
