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
package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class MockJDBCStatement implements Statement {
	
	private MockJDBCConnection connection;
	private int maxFieldSize;
	private int maxRows;
	private int queryTimeout;

	MockJDBCStatement(MockJDBCConnection connection) {
		this.connection = connection;
	}
	
	public ResultSet executeQuery(String sql) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int executeUpdate(String sql) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void close() throws SQLException {
		// do nothing
	}

	public int getMaxFieldSize() throws SQLException {
		return maxFieldSize;
	}

	public void setMaxFieldSize(int max) throws SQLException {
		maxFieldSize = max;
	}

	public int getMaxRows() throws SQLException {
		return maxRows;
	}

	public void setMaxRows(int max) throws SQLException {
		maxRows = max;
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getQueryTimeout() throws SQLException {
		return queryTimeout;
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		queryTimeout = seconds;
	}

	public void cancel() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void setCursorName(String name) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean execute(String sql) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

    /**
     * Creates a new empty result set.
     */
	public ResultSet getResultSet() throws SQLException {
		return new MockJDBCResultSet(this,0);
	}

	public int getUpdateCount() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean getMoreResults() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void setFetchSize(int rows) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getFetchSize() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getResultSetConcurrency() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getResultSetType() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void addBatch(String sql) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void clearBatch() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int[] executeBatch() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Connection getConnection() throws SQLException {
		return connection;
	}

	public boolean getMoreResults(int current) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getResultSetHoldability() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

}
