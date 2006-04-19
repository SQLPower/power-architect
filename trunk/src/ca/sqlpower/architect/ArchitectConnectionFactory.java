package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.jdbc.ConnectionDecorator;

public class ArchitectConnectionFactory implements ConnectionFactory {
	private static final Logger logger = Logger.getLogger(ArchitectConnectionFactory.class);
	
	private ArchitectDataSource dataSource;
	
	
	public ArchitectConnectionFactory(ArchitectDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	
	public Connection createConnection() throws SQLException {
		try {
			
			if (dataSource.getDriverClass() == null
					|| dataSource.getDriverClass().trim().length() == 0) {
				throw new SQLException("Connection \""+dataSource.getName()+"\" has no JDBC Driver class specified.");
			}
			
			if (dataSource.getUrl() == null
					|| dataSource.getUrl().trim().length() == 0) {
				throw new SQLException("Connection \""+dataSource.getName()+"\" has no JDBC URL.");
			}
			
			if (dataSource.getUser() == null
					|| dataSource.getUser().trim().length() == 0) {
				throw new SQLException("Connection \""+dataSource.getName()+"\" has no JDBC username.");
			}
			
			ArchitectSession session = ArchitectSession.getInstance();
			if (session == null) {
				throw new SQLException
				("Can't connect to database \""+dataSource.getName()+
				"\" because ArchitectSession.getInstance() returned null");
			}
			if (logger.isDebugEnabled()) {
				ClassLoader cl = this.getClass().getClassLoader();
				StringBuffer loaders = new StringBuffer();
				loaders.append("Local Classloader chain: ");
				while (cl != null) {
					loaders.append(cl).append(", ");
					cl = cl.getParent();
				}
				logger.debug(loaders);
			}
			Driver driver = (Driver) Class.forName(dataSource.getDriverClass(), true, session.getJDBCClassLoader()).newInstance();
			logger.info("Driver Class "+dataSource.getDriverClass()+" loaded without exception");
			if (!driver.acceptsURL(dataSource.getUrl())) {
				throw new SQLException("Couldn't connect to database:\n"
						+"JDBC Driver "+dataSource.getDriverClass()+"\n"
						+"does not accept the URL "+dataSource.getUrl());
			}
			Properties connectionProps = new Properties();
			connectionProps.setProperty("user", dataSource.getUser());
			connectionProps.setProperty("password", dataSource.getPass());
			Connection realConnection = driver.connect(dataSource.getUrl(), connectionProps);
			if (realConnection == null) {
				throw new SQLException("JDBC Driver returned a null connection!");
			}
			Connection connection = ConnectionDecorator.createFacade(realConnection);
			logger.debug("Connection class is: " + connection.getClass().getName());
			return connection;
		} catch (ClassNotFoundException e) {
			logger.warn("Driver Class not found", e);
			throw new SQLException("JDBC Driver \""+dataSource.getDriverClass()
					+"\" not found.");
		} catch (InstantiationException e) {
			logger.error("Creating SQL Exception to conform to interface.  Real exception is: ", e);
			throw new SQLException("Couldn't create an instance of the " +
					"JDBC driver '"+dataSource.getDriverClass()+"'. "+e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error("Creating SQL Exception to conform to interface.  Real exception is: ", e);
			throw new SQLException("Couldn't connect to database because the " +
					"JDBC driver has no public constructor (this is bad). "+e.getMessage());
		}
	}
}
