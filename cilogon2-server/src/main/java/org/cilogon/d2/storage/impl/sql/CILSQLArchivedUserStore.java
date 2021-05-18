package org.cilogon.d2.storage.impl.sql;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionRecord;
import edu.uiuc.ncsa.security.storage.sql.SQLStore;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.storage.impl.sql.table.ArchivedUsersTable;
import org.cilogon.d2.storage.provider.ArchivedUserProvider;
import org.cilogon.d2.util.ArchivedUserStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Nov 2, 2010 at  9:48:35 AM
 */
public class CILSQLArchivedUserStore extends SQLStore<ArchivedUser> implements ArchivedUserStore {

    public CILSQLArchivedUserStore(ConnectionPool connectionPool,
                                   Table table,
                                   IdentifiableProviderImpl<ArchivedUser> aup,
                                   MapConverter  converter) {
        super(connectionPool, table, aup, converter);
    }

    public ArchivedUsersTable getArchivedUserTable() {
        return (ArchivedUsersTable) getTable();
    }


    protected ArchivedUserProvider getAUP() {
        return (ArchivedUserProvider) identifiableProvider;
    }

    public Identifier archiveUser(Identifier userID) {
        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;

        Identifier archivedUserID = getAUP().newId();
        try {
            PreparedStatement stmt = c.prepareStatement(getArchivedUserTable().addArchiveUser());
            DebugUtil.trace(this, "stmt = " + stmt);
            stmt.setString(1, archivedUserID.toString());  // this sets the id of the archived user entry
            stmt.setString(2, userID.toString()); // this sets the id to get the user from the user table
            stmt.execute();// just execute() since executeQuery(x) would throw an exception regardless of content of x as per JDBC spec.
            stmt.close();
            releaseConnection(cr);
        } catch (SQLException e) {
            destroyConnection(cr);
            throw new GeneralException("Error: Could not archive user.", e);
        }
        return archivedUserID;
    }


    public List<ArchivedUser> getAllByUserId(Identifier userId) {
        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;

        List<ArchivedUser> aUsers = new LinkedList<ArchivedUser>();
        try {
            PreparedStatement stmt = c.prepareStatement(getArchivedUserTable().getArchivedUser());
            stmt.setString(1, userId.toString());
            stmt.execute();// just execute() since executeQuery(x) would throw an exception regardless of content per JDBC spec.

            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                ArchivedUser au = create();
                populate(rsToMap(rs), au);
                aUsers.add(au);
            }
            rs.close();
            stmt.close();
            releaseConnection(cr);
        } catch (SQLException e) {
            destroyConnection(cr);
            throw new GeneralException("Error: Could not get all users by id", e);
        }
        return aUsers;
    }

    public ArchivedUser getLastArchivedUser(Identifier userid) {
        List<ArchivedUser> users = getAllByUserId(userid);
        if (users.isEmpty()) return null;
        // uses that this list is always sorted by archive date.
        return users.get(users.size() - 1);
    }
}
