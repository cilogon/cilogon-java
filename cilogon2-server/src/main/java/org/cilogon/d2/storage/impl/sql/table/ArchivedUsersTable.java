package org.cilogon.d2.storage.impl.sql.table;

import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.cilogon.d2.storage.ArchivedUserKeys;

import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.TIMESTAMP;

/**
 * The table information for archived users. This needs a reference to the {@link UserTable}
 * because it must pull off a user and archive all of its information.
 * <p>Created by Jeff Gaynor<br>
 * on Apr 13, 2010 at  2:07:56 PM
 */
public class ArchivedUsersTable extends Table {
    public ArchivedUsersTable(ArchivedUserKeys keys, String schema, String tablenamePrefix, String tablename, UserTable usersTable) {
        super(keys, schema, tablenamePrefix, tablename);
        this.usersTable = usersTable;
        keys = new ArchivedUserKeys();
        tablename = DEFAULT_TABLENAME;
    }

    public static String DEFAULT_TABLENAME= "old_user";

    public UserTable getUsersTable() {
        return usersTable;
    }

    UserTable usersTable;

    ArchivedUserKeys auKeys(){return (ArchivedUserKeys) keys;}

    @Override
    public void createColumnDescriptors() {
         super.createColumnDescriptors();
        getUsersTable().createColumnDescriptors();
        getColumnDescriptor().addAll(getUsersTable().getColumnDescriptor());
        // There is *no* primary key for this table.
        getColumnDescriptor().add(new ColumnDescriptorEntry(auKeys().archivedUserIDColumn(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(auKeys().archivedTimestampColumn(), TIMESTAMP));
        getColumnDescriptor().get(getUsersTable().userKeys().identifier()).setPrimaryKey(false);
    }

    public String addArchiveUser() {
        return "insert into " + getFQTablename() + " " +
                "(" +
                auKeys().archivedUserIDColumn() + "," +
                getUsersTable().userKeys().remoteUser() + "," +
                getUsersTable().userKeys().eppn() + "," +
                getUsersTable().userKeys().eptid() + "," +
                getUsersTable().userKeys().openID() + "," +
                getUsersTable().userKeys().oidc() + "," +
                getUsersTable().userKeys().idp() + "," +
                getUsersTable().userKeys().idpDisplayName() + "," +
                getUsersTable().userKeys().identifier() + "," +
                getUsersTable().userKeys().serialString() + "," +
                getUsersTable().userKeys().firstName() + "," +
                getUsersTable().userKeys().lastName() + "," +
                getUsersTable().userKeys().email() + "," +
                getUsersTable().userKeys().affiliation() + "," +
                getUsersTable().userKeys().displayName() + "," +
                getUsersTable().userKeys().organizationalUnit() + "," +
                getUsersTable().userKeys().creationTimestamp() + "," +
                getUsersTable().userKeys().useUSinDN() + "," +
                getUsersTable().userKeys().attr_json() + "," +
                auKeys().archivedTimestampColumn() + ")" +
                "select ?," +
                getUsersTable().userKeys().remoteUser() + "," +
                getUsersTable().userKeys().eppn() + "," +
                getUsersTable().userKeys().eptid() + "," +
                getUsersTable().userKeys().openID() + "," +
                getUsersTable().userKeys().oidc() + "," +
                getUsersTable().userKeys().idp() + "," +
                getUsersTable().userKeys().idpDisplayName() + "," +
                getUsersTable().userKeys().identifier() + "," +
                getUsersTable().userKeys().serialString() + "," +
                getUsersTable().userKeys().firstName() + "," +
                getUsersTable().userKeys().lastName() + "," +
                getUsersTable().userKeys().email() + "," +
                getUsersTable().userKeys().affiliation() + "," +
                getUsersTable().userKeys().displayName() + "," +
                getUsersTable().userKeys().organizationalUnit() + "," +
                getUsersTable().userKeys().creationTimestamp() + "," +
                getUsersTable().userKeys().useUSinDN() + "," +
                getUsersTable().userKeys().attr_json() + "," +
                getUsersTable().userKeys().state() + "," +
                "CURRENT_TIMESTAMP FROM " + getUsersTable().getFQTablename() + " where " + getUsersTable().userKeys().identifier() + " = ?";
    }

    public String getArchivedUser() {
        return "SELECT * FROM " + getFQTablename() + " WHERE " +
                getUsersTable().userKeys().identifier() + " = ? ORDER BY " + auKeys().archivedTimestampColumn() + " ASC";
        // note that the SQL spec says that the default for order by is ASC, but in practice some versions of
        // MySQL do not do this. Therefore, it has been explicitly added.
    }
}
