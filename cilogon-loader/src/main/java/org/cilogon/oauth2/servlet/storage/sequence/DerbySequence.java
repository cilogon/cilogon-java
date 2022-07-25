package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionRecord;
import org.cilogon.oauth2.servlet.util.CILogonException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/17/21 at  6:12 AM
 */
public class DerbySequence extends Sequence {
    public DerbySequence(ConnectionPool connectionPool, DerbySequenceTable sequenceTable) {
        super(connectionPool, sequenceTable);
    }

    @Override
    public long nextValue() {
        long value = -1;
            ConnectionRecord cr = getConnection();
            Connection c = cr.connection;
            Statement stmt = null;
            try {

                stmt = c.createStatement();
                stmt.executeUpdate(getSequenceTable().nextValueStatement(), RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    value = rs.getInt(1);
                } else {
                    rs.close();
                    stmt.close();
                    releaseConnection(cr);
                    throw new GeneralException("Error: Could not retrieve the next value. There was some issue with MySQL.");
                }
                rs.close();
                stmt.close();
                releaseConnection(cr);
            } catch (SQLException e) {
                destroyConnection(cr);
                throw new CILogonException("Error getting next value in sequence", e);
            }

            return value;
    }
}
