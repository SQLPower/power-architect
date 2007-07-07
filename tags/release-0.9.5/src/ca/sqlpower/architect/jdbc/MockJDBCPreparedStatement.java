/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.help.UnsupportedOperationException;

public class MockJDBCPreparedStatement implements PreparedStatement {

    private static class Parameter {
        Object value;
        boolean set;
    }
    
    List<Parameter> parameters;
    

    /**
     * create a new prepared statement
     * @param i the number of parameters for this statement >0
     */
    public MockJDBCPreparedStatement(int paramCount) {
        parameters = new ArrayList<Parameter>(paramCount);
        for (int i = 0; i < paramCount; i++) {
            parameters.add(new Parameter());
        }
    }

    /**
     * Throws an exception if there are any unset parameters in this prepared statement.
     * The exception message will list the unset parameter indices.
     */
    public void checkAllParametersSet() throws SQLException {
        checkParametersSet(1, parameters.size());
    }
    
    /**
     * Throws an exception if there are any unset parameters in the given range.
     * The exception message will list the unset parameter indices.
     * 
     * @param start the first index to check for settedness. It uses JDBC-style 1-based indexing.
     * @param length the number of indices (starting at start) to check
     * @throws SQLException If there are any unset parameters in this pstmt.
     */
    public void checkParametersSet(int start, int length) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start+length; i++) {
            if (parameters.get(i-1).set == false) sb.append(" "+i);
        }
        if (sb.length() > 0) {
            throw new SQLException("The following parameters are not set: "+sb);
        }
    }
    
    ////////// PreparedStatement interface is below this line /////////////
    
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
        Object[] result = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            result[i] = parameters.get(i).value;
        }
        return result;
    }

    public void setParameters(Object[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parami = this.parameters.get(i);
            parami.value = parameters[i];
            parami.set = true;
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        parameters.get(parameterIndex-1).value = x;
        parameters.get(parameterIndex-1).set = true;
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
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
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
   }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        parameters.get(parameterIndex-1).value = null;
        parameters.get(parameterIndex-1).set = true;
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        throw new UnsupportedOperationException("This isn't yet implemented in the mock object");
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
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
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
    }

    public void setString(int parameterIndex, String x) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
   }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
         parameters.get(parameterIndex-1).value = x;
         parameters.get(parameterIndex-1).set = true;
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
