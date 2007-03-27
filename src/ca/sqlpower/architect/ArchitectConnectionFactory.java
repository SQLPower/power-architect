package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.log4j.Logger;

/**
 * A simple wrapper around ArchitectDataSource that lets it implement
 * the Apache DBCP ConnectionFactory interface.  We didn't want to pollute
 * ArchitectDataSource itself with the Apache class dependency.
 */
public class ArchitectConnectionFactory implements ConnectionFactory {
	private static final Logger logger = Logger.getLogger(ArchitectConnectionFactory.class);
	
	private ArchitectDataSource dataSource;
	
	public ArchitectConnectionFactory(ArchitectDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public Connection createConnection() throws SQLException {
	    return dataSource.createConnection();
	}
}
