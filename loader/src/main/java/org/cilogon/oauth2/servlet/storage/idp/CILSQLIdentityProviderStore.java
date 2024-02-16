package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.oa4mp.delegation.common.storage.monitored.MonitoredSQLStore;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionRecord;
import edu.uiuc.ncsa.security.storage.sql.internals.ColumnDescriptorEntry;
import edu.uiuc.ncsa.security.storage.sql.internals.ColumnMap;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;
import org.cilogon.oauth2.servlet.util.CILogonException;

import javax.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Apr 10, 2010 at  10:04:49 AM
 */
public class CILSQLIdentityProviderStore extends MonitoredSQLStore<IdentityProvider> implements IdentityProviderStore {
    public static final String DEFAULT_TABLENAME = "identity_provider";

    public CILSQLIdentityProviderStore(ConnectionPool connectionPool, Table table,
                                       Provider<IdentityProvider> idp,
                                       IDPConverter converter) {
        super(connectionPool, table, idp, converter);
    }

    /**
     * Completely replace every idp
     *
     * @param idps
     */
    public void replaceAll(Collection<IdentityProvider> idps) {
        clear();
        add(idps);
    }

    @Override
    public IdentityProvider get(Object key) {
        if (hasIdp(key.toString())) {
            return new IdentityProvider(newID(key.toString()));
        }
        return null;
    }


    public IdentityProvidersTable getIdpTable() {
        return (IdentityProvidersTable) getTable();
    }


    public boolean hasIdp(String idp) {
        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;

        try {
            PreparedStatement stmt = c.prepareStatement(getIdpTable().hasIdpStatement());
            stmt.setString(1, idp);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            boolean rc = rs.next();
            rs.close();
            stmt.close();
            releaseConnection(cr);
            return rc;
        } catch (SQLException e) {
            destroyConnection(cr);
            throw new CILogonException("Error getting identity providers", e);
        }
    }

    @Override
    public void putAll(Map<? extends Identifier, ? extends IdentityProvider> m) {
        Collection<? extends IdentityProvider> idps = m.values();
        if (!idps.isEmpty()) {
            add(idps);
        }
    }

    public void add(IdentityProvider idp) {
        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;
        try {
            PreparedStatement stmt = c.prepareStatement(getIdpTable().createInsertStatement());
            ColumnMap map = depopulate(idp);
            int i = 1;
            for (ColumnDescriptorEntry cde : getTable().getColumnDescriptor()) {
                stmt.setObject(i++, map.get(cde.getName()));
            }

            //stmt.setString(1, idp.getDescription());
            //stmt.setString(2, idp.getIdentifierString());
            stmt.execute();// just execute() since executeQuery(x) would throw an exception regardless of content of x as per JDBC spec.
            stmt.close();
            releaseConnection(cr);
        } catch (SQLException e) {
            destroyConnection(cr);
            throw new CILogonException("Error adding identity provider = \"" + idp + "\"", e);
        }
    }


    public void add(Collection<? extends IdentityProvider> idps) throws CILogonException {
        if (idps.isEmpty()) {
            return; // don't even bother setting up a connection.
        }
        Collection<IdentityProvider> currentValues = values();

        for (IdentityProvider idp : currentValues) {
            idps.remove(idp);
        }
        // This whittles it down to something manageable.
        ConnectionRecord cr = getConnection();
        Connection c = cr.connection;
        try {
            c.setAutoCommit(false);
            PreparedStatement stmt = c.prepareStatement(getIdpTable().createInsertStatement());
            for (IdentityProvider idp : idps) {
                try {
                    for (int i = 1; i <= getIdpTable().getColumnDescriptor().size(); i++) {
                        stmt.setString(i, idp.getIdentifierString()); // Set everything to the IDP id by default.
                    }
                    stmt.execute();
                } catch (SQLException e) {

                    // The major database vendors we support, MySQL and PostgreSQL have the word "duplicate" in their
                    // messages. Trapping the specific exceptions (which inherit from SQLException) would only work
                    // If we include them in the project which in turn means that serveers would be required to deploy
                    // support for all databases, regardless of what store they are using. This is intolerable.
                    if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                        // then this is benign
                        e.printStackTrace();
                    }
                    // Basically we are trying to catch failures if the key is in use. These should be benign.
                    // This mechanism is not perfect though, since other exceptions can cause this to fail.
                    e.printStackTrace();

                }
            }
            stmt.close();
            c.commit();
            c.setAutoCommit(true);
            releaseConnection(cr);
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            destroyConnection(cr);
            throw new CILogonException("Error adding identity provider list", e);
        }
    }

    @Override
    public List<IdentityProvider> getMostRecent(int n, List<String> attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCreationTSField() {
        throw new UnsupportedOperationException();
    }
}
