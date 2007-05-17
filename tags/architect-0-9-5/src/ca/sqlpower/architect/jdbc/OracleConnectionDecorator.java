package ca.sqlpower.architect.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Allow for workarounds for dealing with Oracle Driver issues.
*/
public class OracleConnectionDecorator extends ConnectionDecorator {
	
	public DatabaseMetaData getMetaData() throws SQLException {
        return new OracleDatabaseMetaDataDecorator(super.getMetaData());
    }

	public OracleConnectionDecorator(Connection conn) {
		super(conn);
	}

}
