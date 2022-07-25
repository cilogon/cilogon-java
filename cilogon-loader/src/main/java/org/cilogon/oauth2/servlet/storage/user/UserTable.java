package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.cilogon.oauth2.servlet.util.UserKeys;

import java.sql.Types;

import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.TIMESTAMP;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Apr 13, 2010 at  2:07:14 PM
 */
public class UserTable extends Table {
    public static final String DEFAULT_TABLE_NAME = "user";

    public void createColumnDescriptors() {
        super.createColumnDescriptors();
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().remoteUser(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().idp(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().idpDisplayName(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().serialString(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().firstName(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().lastName(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().email(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().eppn(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().eptid(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().openID(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().oidc(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().subjectId(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().pairwiseId(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().affiliation(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().displayName(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().organizationalUnit(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().creationTimestamp(), TIMESTAMP));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().useUSinDN(), Types.BOOLEAN));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().attr_json(), LONGVARCHAR));
        getColumnDescriptor().add(new ColumnDescriptorEntry(userKeys().state(), LONGVARCHAR));
    }


    public UserTable(UserKeys keys, String schema, String tablenamePrefix, String tablename) {
        super(keys, schema, tablenamePrefix, tablename);
       if (this.tablename == null) {
            this.tablename = DEFAULT_TABLE_NAME;
        }
    }

    public UserKeys userKeys() {
        return (UserKeys) keys;
    }

    public String selectUserStatement() {
        return "SELECT * FROM " + getFQTablename() + " WHERE " +
                userKeys().remoteUser() + " = ? AND " + userKeys().idp() + " = ? ";
    }

    public String removeUserStatement() {
        return "DELETE FROM " + getFQTablename() + " WHERE " + userKeys().remoteUser() + " = ? AND " + userKeys().idp() + " = ?";
    }


    public String getUserIDStatement(String userKeyField) {
        return "SELECT " + userKeys().identifier() + " FROM " + getFQTablename() + " WHERE " + userKeyField +
                " = ? AND " + userKeys().idp() + " = ? ";

    }
}
