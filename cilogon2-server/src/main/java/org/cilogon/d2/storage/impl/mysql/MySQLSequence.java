package org.cilogon.d2.storage.impl.mysql;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import org.cilogon.d2.storage.Sequence;
import org.cilogon.d2.util.CILogonException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/9/12 at  11:58 AM
 */
public class MySQLSequence extends Sequence {
    public MySQLSequence(ConnectionPool connectionPool, MySQLSequenceTable sequenceTable) {
        super(connectionPool, sequenceTable);
    }

    protected MySQLSequenceTable getMT(){
        return (MySQLSequenceTable) getSequenceTable();
    }
    @Override
    public long nextValue() {
        long value = -1;
          Connection c = null;
          Statement stmt = null;
          try {
              c = getConnection();
              stmt = c.createStatement();
              stmt.executeUpdate(getSequenceTable().nextValueStatement(), RETURN_GENERATED_KEYS);
              ResultSet rs = stmt.getGeneratedKeys();
              if(rs.next()){
                  value = rs.getInt(1);
              }else{
                  throw new GeneralException("Error: Could not retrieve the next value. There was some issue with MySQL.");
              }
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
