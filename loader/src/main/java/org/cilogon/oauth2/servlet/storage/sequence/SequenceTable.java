package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.storage.data.SerializationKeys;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/9/12 at  11:44 AM
 */
public abstract class SequenceTable extends Table {
    public SequenceTable(SequenceKeys keys, String schema, String tablenamePrefix, String tablename) {
        super(keys, schema, tablenamePrefix, tablename);
    }

    public static class SequenceKeys extends SerializationKeys{
        public SequenceKeys() {
            identifier("nextval");
        }
    }
    public abstract String createTableStatement(int startValue);

    public abstract String nextValueStatement();

    public abstract String dropStatement();
}
