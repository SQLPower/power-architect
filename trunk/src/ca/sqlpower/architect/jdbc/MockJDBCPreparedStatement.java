package ca.sqlpower.architect.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.help.UnsupportedOperationException;

public class MockJDBCPreparedStatement implements PreparedStatement {

    Object[] parameters;

    /**
     * create a new prepared statement
     * @param i the number of parameters for this statement >0
     */
    public MockJDBCPreparedStatement(int i) {
        parameters = new Object[i];
    }

    public void addBatch() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void clearParameters() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public boolean execute() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public ResultSet executeQuery() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int executeUpdate() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setArray(int i, Array x) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setBlob(int i, Blob x) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        parameters[parameterIndex] = x;
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setClob(int i, Clob x) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        parameters[parameterIndex] = null;
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setString(int parameterIndex, String x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
         parameters[parameterIndex] = x;
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void addBatch(String sql) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void cancel() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void clearBatch() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void close() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public boolean execute(String sql) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int[] executeBatch() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int executeUpdate(String sql) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getMaxFieldSize() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getMaxRows() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public boolean getMoreResults() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getQueryTimeout() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public ResultSet getResultSet() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getResultSetConcurrency() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getResultSetHoldability() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getResultSetType() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public int getUpdateCount() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setCursorName(String name) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setFetchSize(int rows) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setMaxFieldSize(int max) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setMaxRows(int max) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

}
