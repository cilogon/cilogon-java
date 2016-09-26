package org.cilogon.d2.storage;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.SQLDatabase;
import org.cilogon.d2.storage.impl.sql.table.SequenceTable;
import org.cilogon.d2.util.Incrementable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Jun 24, 2010 at  8:26:44 AM
 */
abstract public class Sequence extends SQLDatabase implements Incrementable {
    protected Sequence(ConnectionPool connectionPool, SequenceTable sequenceTable) {
        super(connectionPool);
        this.sequenceTable = sequenceTable;

    }


    public SequenceTable getSequenceTable() {
        return sequenceTable;
    }

    SequenceTable sequenceTable;

    @Override
    public boolean createNew(long initialValue) {
        String x = sequenceTable.createTableStatement((int) initialValue);
        Connection c = null;
        Statement stmt = null;
        try {
            c = getConnection();
            stmt = c.createStatement();
            stmt.executeUpdate(x);
            stmt.close();
        } catch (SQLException e) {
            destroyConnection(c);
            throw new GeneralException("Error creating a new incrementable.", e);
        } finally {
            releaseConnection(c);
        }
        return true;
    }

    @Override
    public boolean destroy() {
        Connection c = null;
        Statement stmt = null;
        try {
            c = getConnection();
            stmt = c.createStatement();
            // No "drop sequence if exists" until we upgrade from postgres 8.x to 9.x...
            String dropIt = getSequenceTable().dropStatement();
            stmt.executeUpdate(dropIt);
            stmt.close();
        } catch (SQLException e) {
            destroyConnection(c);
            throw new GeneralException("Error destroying the sequence", e);
        } finally {
            releaseConnection(c);
        }
        return true;
    }

    @Override
    public boolean init() {
        destroy();
        return createNew();
    }

    @Override
    public boolean createNew() {
        return createNew(42L);
    }

    @Override
    public boolean isCreated() {
        return false;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
