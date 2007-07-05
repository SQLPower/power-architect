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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

/**
 * A fake connection object that we can use for unit testing the architect.
 * 
 * @author fuerth
 * @version $Id$
 */
public class MockJDBCConnection implements Connection {

	private boolean autoCommit;
	private boolean readOnly;
	private String currentCatalog;
	private int transactionIsolation;
	private String url;
	private Properties properties;
	private MockJDBCDatabaseMetaData metaData;
	
	public MockJDBCConnection(String url, Properties properties) {
		this.url = url;
		this.properties = properties;
		this.metaData = new MockJDBCDatabaseMetaData(this);
	}
	
	String getURL() {
		return url;
	}
	
	Properties getProperties() {
		return properties;
	}
	
	// ========= java.sql.Connection interface is below this line ========
	
	public Statement createStatement() throws SQLException {
		return new MockJDBCStatement(this);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String nativeSQL(String sql) throws SQLException {
		return sql;
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		this.autoCommit = autoCommit;
	}

	public boolean getAutoCommit() throws SQLException {
		return autoCommit;
	}

	public void commit() throws SQLException {
		// do nothing
	}

	public void rollback() throws SQLException {
		// do nothing
	}

	public void close() throws SQLException {
		// do nothing
	}

	public boolean isClosed() throws SQLException {
		return false;
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return metaData;
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() throws SQLException {
		return readOnly;
	}

	public void setCatalog(String catalog) throws SQLException {
		if (getMetaData().getCatalogTerm() != null) {
			currentCatalog = catalog;
		} else {
			throw new SQLException("This Mock Database doesn't have catalogs");
		}
	}

	public String getCatalog() throws SQLException {
		return currentCatalog;
	}

	public void setTransactionIsolation(int level) throws SQLException {
		transactionIsolation = level;
	}

	public int getTransactionIsolation() throws SQLException {
		return transactionIsolation;
	}

	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	public void clearWarnings() throws SQLException {
		// do nothing
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return new MockJDBCStatement(this);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void setHoldability(int holdability) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getHoldability() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Savepoint setSavepoint() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		// do nothing
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return new MockJDBCStatement(this);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

}
