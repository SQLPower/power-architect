package regress.ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockJDBCDatabaseMetaData implements DatabaseMetaData {

	private String schemaTerm;
	private String catalogTerm;
	private MockJDBCConnection connection;

	MockJDBCDatabaseMetaData(MockJDBCConnection connection) {
		this.connection = connection;
	}
	
	public boolean allProceduresAreCallable() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean allTablesAreSelectable() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getURL() throws SQLException {
		return connection.getURL();
	}

	public String getUserName() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean isReadOnly() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean nullsAreSortedHigh() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean nullsAreSortedLow() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean nullsAreSortedAtStart() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean nullsAreSortedAtEnd() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getDatabaseProductName() throws SQLException {
		return "SQL Power Mock JDBC Database";
	}

	public String getDatabaseProductVersion() throws SQLException {
		return "0.0";
	}

	public String getDriverName() throws SQLException {
		return "SQL Power Mock JDBC Database Driver";
	}

	public String getDriverVersion() throws SQLException {
		return "0.0";
	}

	public int getDriverMajorVersion() {
		return 0;
	}

	public int getDriverMinorVersion() {
		return 0;
	}

	public boolean usesLocalFiles() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean usesLocalFilePerTable() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean storesUpperCaseIdentifiers() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean storesLowerCaseIdentifiers() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean storesMixedCaseIdentifiers() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getIdentifierQuoteString() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getSQLKeywords() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getNumericFunctions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getStringFunctions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getSystemFunctions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getTimeDateFunctions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getSearchStringEscape() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getExtraNameCharacters() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsAlterTableWithAddColumn() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsAlterTableWithDropColumn() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsColumnAliasing() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean nullPlusNonNullIsNull() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsConvert() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsConvert(int fromType, int toType)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsTableCorrelationNames() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsExpressionsInOrderBy() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsOrderByUnrelated() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsGroupBy() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsGroupByUnrelated() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsGroupByBeyondSelect() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsLikeEscapeClause() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsMultipleResultSets() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsMultipleTransactions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsNonNullableColumns() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsMinimumSQLGrammar() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsCoreSQLGrammar() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsExtendedSQLGrammar() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsANSI92FullSQL() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsIntegrityEnhancementFacility() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsOuterJoins() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsFullOuterJoins() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsLimitedOuterJoins() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getSchemaTerm() throws SQLException {
		return connection.getProperties().getProperty("dbmd.schemaTerm");
	}

	public String getProcedureTerm() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getCatalogTerm() throws SQLException {
		return connection.getProperties().getProperty("dbmd.catalogTerm");
	}

	public boolean isCatalogAtStart() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getCatalogSeparator() throws SQLException {
		if (catalogTerm == null) {
			throw new SQLException("Catalogs are not supported");
		} else {
			return ".";
		}
	}

	public boolean supportsSchemasInDataManipulation() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsPositionedDelete() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsPositionedUpdate() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSelectForUpdate() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsStoredProcedures() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSubqueriesInComparisons() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSubqueriesInExists() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSubqueriesInIns() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsSubqueriesInQuantifieds() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsCorrelatedSubqueries() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsUnion() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsUnionAll() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxBinaryLiteralLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxCharLiteralLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxColumnNameLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxColumnsInGroupBy() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxColumnsInIndex() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxColumnsInOrderBy() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxColumnsInSelect() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxColumnsInTable() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxConnections() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxCursorNameLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxIndexLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxSchemaNameLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxProcedureNameLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxCatalogNameLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxRowSize() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxStatementLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxStatements() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxTableNameLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxTablesInSelect() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxUserNameLength() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getDefaultTransactionIsolation() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsTransactions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsTransactionIsolationLevel(int level)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsDataDefinitionAndDataManipulationTransactions()
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsDataManipulationTransactionsOnly()
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getProcedures(String catalog, String schemaPattern,
			String procedureNamePattern) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getProcedureColumns(String catalog, String schemaPattern,
			String procedureNamePattern, String columnNamePattern)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getTables(String catalogNamePattern, String schemaNamePattern,
			String tableNamePattern, String[] types) throws SQLException {
		
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 10);
		rs.setColumnName(1, "TABLE_CAT");
		rs.setColumnName(2, "TABLE_SCHEM");
		rs.setColumnName(3, "TABLE_NAME");
		rs.setColumnName(4, "TABLE_TYPE");
		rs.setColumnName(5, "REMARKS");
		rs.setColumnName(6, "TYPE_CAT");
		rs.setColumnName(7, "TYPE_SCHEM");
		rs.setColumnName(8, "TYPE_NAME");
		rs.setColumnName(9, "SELF_REFERENCING_COL_NAME");
		rs.setColumnName(10, "REF_GENERATION");

		Pattern catalogPattern = createPatternFromSQLWildcard(catalogNamePattern);
		Pattern schemaPattern = createPatternFromSQLWildcard(schemaNamePattern);
		Pattern tablePattern = createPatternFromSQLWildcard(tableNamePattern);

		String catalogList = connection.getProperties().getProperty("catalogs");
		String schemaList = connection.getProperties().getProperty("schemas");
		String tableList = connection.getProperties().getProperty("tables");
		
		// TODO: create the result set rows here
		
		rs.beforeFirst();
		return rs;
	}

	private Pattern createPatternFromSQLWildcard(String sqlWildcard) {
		if (sqlWildcard == null) {
			return Pattern.compile(".*");
		} else {
			// remove existing regex metachars
			sqlWildcard = sqlWildcard.replace(".", "\\.");
			sqlWildcard = sqlWildcard.replace("[", "\\[");
			sqlWildcard = sqlWildcard.replace("\\", "\\\\");
			
			// translate SQL metachars into regex
			sqlWildcard = sqlWildcard.replace("%", ".*");
			sqlWildcard = sqlWildcard.replace("_", ".");
			
			return Pattern.compile(sqlWildcard);
		}
	}

	public ResultSet getSchemas() throws SQLException {
		String catalogList = connection.getProperties().getProperty("catalogs");
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 2);
		rs.setColumnName(1, "TABLE_SCHEM");
		rs.setColumnName(2, "TABLE_CATALOG");
		
		if (getSchemaTerm() == null) {
			// no schemas. return empty rs
		} else if (getCatalogTerm() == null) {
			// database has schemas but not catalogs
			String schemaList = connection.getProperties().getProperty("schemas");
			for (String schName : Arrays.asList(schemaList.split(","))) {
				rs.addRow();
				rs.updateObject(1, null);
				rs.updateObject(2, schName);
			}
		} else {
			// database has catalogs and schemas
			for (String catName : Arrays.asList(catalogList.split(","))) {
				String schemaList = connection.getProperties().getProperty("schemas."+catName);
				for (String schName : Arrays.asList(schemaList.split(","))) {
					rs.addRow();
					rs.updateObject(1, catName);
					rs.updateObject(2, schName);
				}
			}
		}
		rs.beforeFirst();
		return rs;
	}

	public ResultSet getCatalogs() throws SQLException {
		String catalogList = connection.getProperties().getProperty("catalogs");
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 1);
		rs.setColumnName(1, "TABLE_CAT");
		if (getCatalogTerm() != null) {
			for (String catName : Arrays.asList(catalogList.split(","))) {
				rs.addRow();
				rs.updateObject(1, catName);
			}
		}
		rs.beforeFirst();
		return rs;
	}

	public ResultSet getTableTypes() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getColumns(String catalog, String schemaPattern,
			String tableNamePattern, String columnNamePattern)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getColumnPrivileges(String catalog, String schema,
			String table, String columnNamePattern) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getTablePrivileges(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getBestRowIdentifier(String catalog, String schema,
			String table, int scope, boolean nullable) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getVersionColumns(String catalog, String schema,
			String table) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getPrimaryKeys(String catalog, String schema, String table)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getImportedKeys(String catalog, String schema, String table)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getExportedKeys(String catalog, String schema, String table)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getCrossReference(String primaryCatalog,
			String primarySchema, String primaryTable, String foreignCatalog,
			String foreignSchema, String foreignTable) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getTypeInfo() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getIndexInfo(String catalog, String schema, String table,
			boolean unique, boolean approximate) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsResultSetType(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsResultSetConcurrency(int type, int concurrency)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean ownUpdatesAreVisible(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean ownDeletesAreVisible(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean ownInsertsAreVisible(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean othersUpdatesAreVisible(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean othersDeletesAreVisible(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean othersInsertsAreVisible(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean updatesAreDetected(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean deletesAreDetected(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean insertsAreDetected(int type) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsBatchUpdates() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getUDTs(String catalog, String schemaPattern,
			String typeNamePattern, int[] types) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Connection getConnection() throws SQLException {
		return connection;
	}

	public boolean supportsSavepoints() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsNamedParameters() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsMultipleOpenResults() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsGetGeneratedKeys() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getSuperTypes(String catalog, String schemaPattern,
			String typeNamePattern) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getSuperTables(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSet getAttributes(String catalog, String schemaPattern,
			String typeNamePattern, String attributeNamePattern)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsResultSetHoldability(int holdability)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getResultSetHoldability() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getDatabaseMajorVersion() throws SQLException {
		return 0;
	}

	public int getDatabaseMinorVersion() throws SQLException {
		return 0;
	}

	public int getJDBCMajorVersion() throws SQLException {
		return 3;
	}

	public int getJDBCMinorVersion() throws SQLException {
		return 0;
	}

	public int getSQLStateType() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean locatorsUpdateCopy() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean supportsStatementPooling() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

}
