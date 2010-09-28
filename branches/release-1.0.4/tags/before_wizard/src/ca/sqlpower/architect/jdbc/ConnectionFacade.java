/*
 * Created on Jun 8, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 * The ConnectionFacade wraps a JDBC Connection object and delegates all operations to it.
 * Some calls are intercepted for some types of database connections when the delegate connection
 * is not fully conformant to the JDBC standard.
 *
 * @author fuerth
 * @version $Id$
 */
public class ConnectionFacade implements Connection {
	private static Logger logger = Logger.getLogger(ConnectionFacade.class);

    /**
     * The object to which all JDBC operations are delegated. 
     */
	protected Connection connection;

    /**
     * Creates a new ConnectionFacade which delegates to the given connection.
     * Outside users can create a ConnectionFacade using the public {@link #createFacade(Connection)} method.
     * 
     * @param delegate The object to which all JDBC operations will be delegated.
     */
    protected ConnectionFacade(Connection delegate) {
        this.connection = delegate;
    }

    /**
     * Creates a new ConnectionFacade (or appropriate subclass) which delegates to the given connection.
     * Outside users can create a ConnectionFacade using the public {@link #createFacade(Connection)} method.
     * 
     * @param delegate The object to which all JDBC operations will be delegated.
     */
    public static ConnectionFacade createFacade(Connection delegate) throws SQLException {
		logger.debug("static createFacade, driver class is: " + delegate.getClass().getName());
		logger.debug("static createFacade, driver name is: " + delegate.getMetaData().getDriverName());
        if (delegate.getMetaData().getDriverName().equals("PostgreSQL Native Driver")) {
            return new PostgresConnectionFacade(delegate);
        } else {
            return new ConnectionFacade(delegate);
        }
    }
    
    /**
     * @throws java.sql.SQLException
     */
    public void clearWarnings() throws SQLException {
        connection.clearWarnings();
    }
    /**
     * @throws java.sql.SQLException
     */
    public void close() throws SQLException {
        connection.close();
    }
    /**
     * @throws java.sql.SQLException
     */
    public void commit() throws SQLException {
        connection.commit();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }
    /**
     * @param resultSetType
     * @param resultSetConcurrency
     * @return
     * @throws java.sql.SQLException
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return connection.createStatement(resultSetType, resultSetConcurrency);
    }
    /**
     * @param resultSetType
     * @param resultSetConcurrency
     * @param resultSetHoldability
     * @return
     * @throws java.sql.SQLException
     */
    public Statement createStatement(int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return connection.createStatement(resultSetType, resultSetConcurrency,
                resultSetHoldability);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return connection.equals(obj);
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public boolean getAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public String getCatalog() throws SQLException {
        return connection.getCatalog();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public int getHoldability() throws SQLException {
        return connection.getHoldability();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return connection.getMetaData();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public int getTransactionIsolation() throws SQLException {
        return connection.getTransactionIsolation();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public Map getTypeMap() throws SQLException {
        return connection.getTypeMap();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public SQLWarning getWarnings() throws SQLException {
        return connection.getWarnings();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return connection.hashCode();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public boolean isReadOnly() throws SQLException {
        return connection.isReadOnly();
    }
    /**
     * @param sql
     * @return
     * @throws java.sql.SQLException
     */
    public String nativeSQL(String sql) throws SQLException {
        return connection.nativeSQL(sql);
    }
    /**
     * @param sql
     * @return
     * @throws java.sql.SQLException
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        return connection.prepareCall(sql);
    }
    /**
     * @param sql
     * @param resultSetType
     * @param resultSetConcurrency
     * @return
     * @throws java.sql.SQLException
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    /**
     * @param sql
     * @param resultSetType
     * @param resultSetConcurrency
     * @param resultSetHoldability
     * @return
     * @throws java.sql.SQLException
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency,
                resultSetHoldability);
    }
    /**
     * @param sql
     * @return
     * @throws java.sql.SQLException
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }
    /**
     * @param sql
     * @param autoGeneratedKeys
     * @return
     * @throws java.sql.SQLException
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
        return connection.prepareStatement(sql, autoGeneratedKeys);
    }
    /**
     * @param sql
     * @param resultSetType
     * @param resultSetConcurrency
     * @return
     * @throws java.sql.SQLException
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        return connection.prepareStatement(sql, resultSetType,
                resultSetConcurrency);
    }
    /**
     * @param sql
     * @param resultSetType
     * @param resultSetConcurrency
     * @param resultSetHoldability
     * @return
     * @throws java.sql.SQLException
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return connection.prepareStatement(sql, resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }
    /**
     * @param sql
     * @param columnIndexes
     * @return
     * @throws java.sql.SQLException
     */
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws SQLException {
        return connection.prepareStatement(sql, columnIndexes);
    }
    /**
     * @param sql
     * @param columnNames
     * @return
     * @throws java.sql.SQLException
     */
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException {
        return connection.prepareStatement(sql, columnNames);
    }
    /**
     * @param savepoint
     * @throws java.sql.SQLException
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        connection.releaseSavepoint(savepoint);
    }
    /**
     * @throws java.sql.SQLException
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }
    /**
     * @param savepoint
     * @throws java.sql.SQLException
     */
    public void rollback(Savepoint savepoint) throws SQLException {
        connection.rollback(savepoint);
    }
    /**
     * @param autoCommit
     * @throws java.sql.SQLException
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }
    /**
     * @param catalog
     * @throws java.sql.SQLException
     */
    public void setCatalog(String catalog) throws SQLException {
        connection.setCatalog(catalog);
    }
    /**
     * @param holdability
     * @throws java.sql.SQLException
     */
    public void setHoldability(int holdability) throws SQLException {
        connection.setHoldability(holdability);
    }
    /**
     * @param readOnly
     * @throws java.sql.SQLException
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
        connection.setReadOnly(readOnly);
    }
    /**
     * @return
     * @throws java.sql.SQLException
     */
    public Savepoint setSavepoint() throws SQLException {
        return connection.setSavepoint();
    }
    /**
     * @param name
     * @return
     * @throws java.sql.SQLException
     */
    public Savepoint setSavepoint(String name) throws SQLException {
        return connection.setSavepoint(name);
    }
    /**
     * @param level
     * @throws java.sql.SQLException
     */
    public void setTransactionIsolation(int level) throws SQLException {
        connection.setTransactionIsolation(level);
    }
    /**
     * @param map
     * @throws java.sql.SQLException
     */
    public void setTypeMap(Map map) throws SQLException {
        connection.setTypeMap(map);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return connection.toString();
    }
}
