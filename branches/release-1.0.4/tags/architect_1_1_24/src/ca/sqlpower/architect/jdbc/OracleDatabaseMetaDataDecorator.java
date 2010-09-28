package ca.sqlpower.architect.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Decorate an Oracle Connection to handle the evil error "ORA-1722" on getTypeMap() when
 * the user is using an Oracle 10 driver on Oracle 8i. 
 */
public class OracleDatabaseMetaDataDecorator extends DatabaseMetaDataDecorator {

	public OracleDatabaseMetaDataDecorator(DatabaseMetaData delegate) {
		super(delegate);
	}
	
	private static final int DREADED_ORACLE_ERROR_CODE = 1722;
	
	private static final String ORACLE_1722_MESSAGE =
		"That caught ORA-1722; in this context it normally means that you are using the " +
		"Oracle 10 driver with Oracle 8. Please check your driver settings";

	@Override
	public ResultSet getTypeInfo() throws SQLException {
		try {
			return super.getTypeInfo();
		} catch (SQLException e) {
			if (e.getErrorCode() == DREADED_ORACLE_ERROR_CODE) {
				SQLException newE = new SQLException(ORACLE_1722_MESSAGE);
				newE.setNextException(e);
				throw newE;
			} else {
				throw e;
			}
		}
	}
}