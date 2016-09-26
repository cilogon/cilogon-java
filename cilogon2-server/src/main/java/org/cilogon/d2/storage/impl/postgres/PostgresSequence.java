package org.cilogon.d2.storage.impl.postgres;


import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import org.cilogon.d2.storage.Sequence;
import org.cilogon.d2.util.CILogonException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Jun 24, 2010 at  8:19:52 AM
 */
public class PostgresSequence extends Sequence {
    public PostgresSequence(ConnectionPool connectionPool,
                            PGSequenceTable sequenceTable) {
        super(connectionPool, sequenceTable);
    }

    @Override
    public long nextValue() throws CILogonException {
          long value = -1;
          Connection c = null;
          Statement stmt = null;
          try {
              c = getConnection();
              stmt = c.createStatement();
              stmt.execute(getSequenceTable().nextValueStatement());
              ResultSet rs = stmt.getResultSet();
              rs.next(); // trick to get to the value
              value = rs.getInt(1);
              rs.close();
              stmt.close();
          } catch (SQLException e) {
              destroyConnection(c);
              throw new CILogonException("Error getting next value in sequence", e);
          } finally {
              releaseConnection(c);
          }
          return value;
      }



}
