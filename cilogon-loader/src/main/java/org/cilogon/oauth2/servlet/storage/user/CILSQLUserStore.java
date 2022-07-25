package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.cache.SimpleEntryImpl;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionRecord;
import edu.uiuc.ncsa.security.storage.sql.SQLStore;
import edu.uiuc.ncsa.security.storage.sql.internals.ColumnMap;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.cilogon.oauth2.servlet.util.CILogonException;
import org.cilogon.oauth2.servlet.util.Incrementable;
import org.cilogon.oauth2.servlet.util.UserKeys;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Mar 12, 2010 at  3:41:37 PM
 */
public class CILSQLUserStore extends SQLStore<User> implements UserStore {
    Incrementable incrementable;

    @Override
    public Incrementable getIncrementable() {
        return incrementable;
    }

    @Override
    public User createAndRegisterUser(UserMultiID userMultiKey,
                                      String idP,
                                      String idPDisplayName,
                                      String firstName,
                                      String lastName,
                                      String email,
                                      String affiliation,
                                      String displayName,
                                      String organizationalUnit) {
        Identifier uid = null;
        User user = create(true);
        ServletDebugUtil.trace(this, "created new user" + user);

        uid = user.getIdentifier();
      //  System.err.println(this.getClass().getSimpleName() + ": start user uid = " + uid + " ss = " + user.getSerialIdentifier());
        user.setCreationTime(new Date());
        user.setIdP(idP);
        user.setSerialIdentifier(uid); // for a new user these are identical. This might change though over time.
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIDPName(idPDisplayName);
        user.setUserMultiKey(userMultiKey);
        user.setAffiliation(affiliation);
        user.setDisplayName(displayName);
        user.setOrganizationalUnit(organizationalUnit);
        ServletDebugUtil.trace(this, "registering new user" + user);

        register(user);
        ServletDebugUtil.trace(this, "AFTER registering new user" + user);
     //   System.err.println(this.getClass().getSimpleName() + ": AFTER user uid = " + user.getIdentifier() + " ss = " + user.getSerialIdentifier());

        return user;
    }

    /**
     * This will find the name from the PersonName and, if there is one, return an SQL snippet of the form
     * <code>key=?</code> to be added to the select statement.
     *
     * @param personName
     * @param key
     * @return
     */
    protected String selectSnippet(PersonName personName, String key) {
        if (personName == null) return null;
        if (personName.getName() == null || personName.getName().length() == 0) return null;
        String out = key + "=?";
        ServletDebugUtil.trace(this, "in selectSnippet, snippet = \"" + out + "\"");
        return out;
    }

    @Override
    public Collection<User> get(UserMultiID userMultiKey, String idP) {
        ServletDebugUtil.trace(this, "getting user for multi-key=" + userMultiKey + ", and idp=" + idP);
        if (userMultiKey.isTrivial()) throw new UserNotFoundException("Error: no user for trivial identifier");
        // so one of these is ok.
        UserKeys userKeys = (UserKeys) converter.keys;
        ArrayList<String> foundIds = new ArrayList<>();
        String selectStmt = "select * from " + getTable().getFQTablename() + " where";
        String snippet = selectSnippet(userMultiKey.getRemoteUserName(), userKeys.remoteUser());
        String zzz = "";
        boolean gotOne = false;
        if (snippet != null) {
            foundIds.add(userMultiKey.getRemoteUserName().getName());
            zzz = zzz + " " + snippet;
            gotOne = true;
        }
        snippet = selectSnippet(userMultiKey.getEppn(), userKeys.eppn());
        if (snippet != null) {
            foundIds.add(userMultiKey.getEppn().getName());
            zzz = zzz + (gotOne ? " OR " : " ") + snippet;
            gotOne = true;
        }
        snippet = selectSnippet(userMultiKey.getEptid(), userKeys.eptid());
        if (snippet != null) {
            foundIds.add(userMultiKey.getEptid().getName());
            zzz = zzz + (gotOne ? " OR " : " ") + snippet;
            gotOne = true;
        }
        snippet = selectSnippet(userMultiKey.getPairwiseID(), userKeys.pairwiseId());
        if (snippet != null) {
            foundIds.add(userMultiKey.getPairwiseID().getName());
            zzz = zzz + (gotOne ? " OR " : " ") + snippet;
            gotOne = true;
        }
        snippet = selectSnippet(userMultiKey.getSubjectID(), userKeys.subjectId());
        if (snippet != null) {
            foundIds.add(userMultiKey.getSubjectID().getName());
            zzz = zzz + (gotOne ? " OR " : " ") + snippet;
            gotOne = true;
        }
        snippet = selectSnippet(userMultiKey.getOpenID(), userKeys.openID());
        if (snippet != null) {
            foundIds.add(userMultiKey.getOpenID().getName());
            zzz = zzz + (gotOne ? " OR " : " ") + snippet;
            gotOne = true;
        }


        snippet = selectSnippet(userMultiKey.getOpenIDConnect(), userKeys.oidc());
        if (snippet != null) {
            foundIds.add(userMultiKey.getOpenIDConnect().getName());
            zzz = zzz + (gotOne ? " OR " : " ") + snippet;
            gotOne = true;
        }

        if (0 < zzz.length()) {
            selectStmt = selectStmt + " (" + zzz + ") ";
        }


        ServletDebugUtil.trace(this, "user select statement=\"" + selectStmt + "\"");
        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;
        selectStmt = selectStmt + " AND " + userKeys.idp() + " = ?";
        User user = null;
        ArrayList<User> users
                = new ArrayList<>();

        try {
            PreparedStatement stmt = c.prepareStatement(selectStmt);
            int i = 1; // SQL statements start with index 1.
            for (String x : foundIds) {
                stmt.setString(i++, x);

            }
            stmt.setString(i++, idP);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                user = create(false);
                ColumnMap map = rsToMap(rs);
                populate(map, user);
                ServletDebugUtil.trace(this, "Adding user = " + user);
                users.add(user);
            }
            rs.close();
            stmt.close();
            releaseConnection(cr);

        } catch (SQLException e) {
            destroyConnection(cr);
            DebugUtil.error(this, "Error getting user with ids =" +  userMultiKey, e);
            throw new CILogonException("Error getting user with ids = \"" + userMultiKey + "\" and identity provider =\"" + idP + "\"", e);
        }
        if (users.isEmpty()) throw new UserNotFoundException();
        return users;
    }

    @Override
    public Identifier getUserID(UserMultiID userMultiKey, String idP) {
        Collection<User> users = get(userMultiKey, idP);
        if (users.size() == 1) {
            return users.iterator().next().getIdentifier();
        }
        throw new UserNotFoundException("Error: multiple users found");
    }

    public CILSQLUserStore(ConnectionPool connectionPool,
                           Table table,
                           IdentifiableProviderImpl<User> idp,
                           MapConverter converter,
                           Incrementable incrementable) {
        super(connectionPool, table, idp, converter);
        this.incrementable = incrementable;
    }

    protected UserProvider getUserProvider() {
        return (UserProvider) identifiableProvider;
    }

    /**
     * API-specific. No user means user not found exception must be thrown.
     *
     * @param o
     * @return
     */
    @Override
    public User get(Object o) {
        User u = super.get(o);
        if (u == null) throw new UserNotFoundException("No user found for id \"" + o + "\"");
        return u;
    }

    @Override
    public Set<Entry<Identifier, User>> entrySet() {
        HashSet<Entry<Identifier, User>> entries = new HashSet<Entry<Identifier, User>>();
        for (Identifier key : keySet()) {
            entries.add(new SimpleEntryImpl<Identifier, User>(key, get(key)));
        }
        return entries;
    }


    public UserTable getUserTable() {
        return (UserTable) getTable();
    }

    @Override
    public User create(boolean newSerialString) {
        User user = getUserProvider().get(newSerialString); // create with an identifier. decide about the serial string later
        // check if this in use in the store.
        if (newSerialString && containsKey(user.getIdentifier())) {
            throw new InvalidUserIdException("Error: The id \"" + user.getIdentifierString() + "\" is already in use.");
        }
        return user;
    }

    @Override
    public User create() {
        return create(false);
    }

    /**
     * API-specific get by remoteUser and IDP.
     *
     * @param remoteUser
     * @param idP
     * @return
     */
    public User get(String remoteUser, String idP) {

        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;

        User user = null;
        try {
            PreparedStatement stmt = c.prepareStatement(getUserTable().selectUserStatement());
            stmt.setString(1, remoteUser);
            stmt.setString(2, idP);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if (rs.next()) {
                user = create(false);
                ColumnMap map = rsToMap(rs);
                populate(map, user);
            } else {
                rs.close();
                stmt.close();
                releaseConnection(cr);
                throw new UserNotFoundException("Error: no user found for remoteUser=" + remoteUser + ", and idp=" + idP);
            }
            rs.close();
            stmt.close();
            releaseConnection(cr);
        } catch (SQLException e) {
            destroyConnection(cr);
            throw new CILogonException("Error getting user with remote user name = \"" + remoteUser + "\" and identity provider =\"" + idP + "\"", e);
        }
        return user;

    }


    public boolean remove(String remoteUser, String idP) {
        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;

        try {
            PreparedStatement stmt = c.prepareStatement(getUserTable().removeUserStatement());
            stmt.setString(1, remoteUser);
            stmt.setString(2, idP);
            stmt.executeUpdate();
            stmt.close();
            releaseConnection(cr);
        } catch (SQLException e) {
            destroyConnection(cr);
            return false;
        }
        // note that if there is no entry for this, then there is no SQL error and we return true in any case.
        return true;
    }


    // Assumes that the user already exists and this is just an update.
    // This is a primitive call in that no checking is done. It is assumed that all checks are completed
    // by this point and this just carries out the update. In particular, that the user exists and that
    // there is already an entry (note that this does not change the remote_user or idP columns).
    // This also generates a new serial string and resets it

    /**
     * Update the user. <B>NOTE:</b> it is up to the programmer to archive the user prior to making any updates,
     * if that is warranted.
     * <b><it>This updates the serial string!!</it></b> If you do not want the serial string updated, you should use
     * {@link #update(User, boolean)} with the second argument of <code>true</code>.
     *
     * @param user
     * @
     */

    public void update(User user) {
        update(user, false);
    }

    public void update(User user, boolean keepSerialID) {
        ServletDebugUtil.trace(this,"user id = " + user.getIdentifierString() + ", serial string=" + user.getSerialString() + ", keepSerialID=" + keepSerialID);
        // Fix for CIL-69: Any changes to the user (IDP, first name, last name, email) must change the serial identifier too.
        if (!keepSerialID) {
            Identifier serialString = getUserProvider().newIdentifier();
            user.setSerialIdentifier(serialString); // or subsequent calls have wrong serial string!
            ServletDebugUtil.trace(this,"setting serial string for user id = " + user.getIdentifierString() + ", serial string=" + user.getSerialString());

        }
        super.update(user);
    }

    public Identifier getUserID(String userKey, String personName, String idP) {
        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;
        Identifier rc = null;
        try {
            PreparedStatement stmt = c.prepareStatement(getUserTable().getUserIDStatement(userKey));
            stmt.setString(1, personName);
            stmt.setString(2, idP);
            stmt.execute();// just execute() since executeQuery(x) would throw an exception regardless of content of x as per JDBC spec.
            ResultSet rs = stmt.getResultSet();
            if (rs.next()) {
                rc = BasicIdentifier.newID(rs.getString(getUserTable().userKeys().identifier()));
            } // Finish up using the database, then decide what to do. Catch any real DB exceptions that happen.
            rs.close();
            stmt.close();
            releaseConnection(cr);

        } catch (Exception e) {
            destroyConnection(cr);
            throw new CILogonException("Error getting uid for remote user = " + personName + ", and IdP = " + idP, e);
        }
        if (rc == null) {
            throw new UserNotFoundException("No user found for remoteUser=" + personName + ", idp=" + idP);
        }
        return rc;
    }

    public Identifier getUserID(RemoteUserName remoteUser, String idP) {
        return getUserID(((UserKeys) converter.keys).remoteUser(), remoteUser.getName(), idP);
    }

    public Identifier getUserID(EduPersonPrincipleName eppn, String idP) {
        return getUserID(((UserKeys) converter.keys).eppn(), eppn.getName(), idP);
    }

    public Identifier getUserID(EduPersonTargetedID eptid, String idP) {
        return getUserID(((UserKeys) converter.keys).eptid(), eptid.getName(), idP);
    }

    public Identifier getUserID(OpenID openID, String idP) {
        return getUserID(((UserKeys) converter.keys).openID(), openID.getName(), idP);
    }

    @Override
    public MapConverter getMapConverter() {
        return converter;
    }

    @Override
    public String getCreationTSField() {
        return ((UserKeys) getMapConverter().getKeys()).creationTimestamp();
    }
}
