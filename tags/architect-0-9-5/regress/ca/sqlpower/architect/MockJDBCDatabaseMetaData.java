package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MockJDBCDatabaseMetaData implements DatabaseMetaData {

	private static final Logger logger = Logger.getLogger(MockJDBCDatabaseMetaData.class);

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
		if (getCatalogTerm() == null) {
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
		final String NO_CATALOG = "no_catalog";  // special string to indicate no catalogs are in the database
		
		// FIXME: this method should support restricting to connection's current catalog!
		
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

		if (logger.isDebugEnabled()) {
			logger.debug("getTables: Searching for:");
			logger.debug("    catalog '"+catalogNamePattern+"' (pattern "+catalogPattern+")");
			logger.debug("     schema '"+schemaNamePattern+"' (pattern "+schemaPattern+")");
			logger.debug("      table '"+tableNamePattern+"' (pattern "+tablePattern+")");
		}

		List<String> catalogs = new ArrayList<String>();
		if (getCatalogTerm() != null) {
			String catalogList = connection.getProperties().getProperty("catalogs");
			for (String cat : Arrays.asList(catalogList.split(","))) {
				if (catalogPattern.matcher(cat).matches()) {
					catalogs.add(cat);
					logger.debug("  Adding catalog "+cat);
				} else {
					logger.debug("Skipping catalog "+cat+" (doesn't match pattern)");
				}
			}
		} else {
			catalogs.add(NO_CATALOG);
		}
		
		// map from catalog name (might be null) to list of schema names in that catalog
		Map<String,List<String>> schemas = new TreeMap<String,List<String>>();
		if (getSchemaTerm() != null) {
			for (String cat : catalogs) {
				String schemaList;
				if (cat == NO_CATALOG) {
					schemaList = connection.getProperties().getProperty("schemas");
				} else {
					schemaList = connection.getProperties().getProperty("schemas."+cat);
				}
				
				List<String> schemasOfCat = new ArrayList<String>();
				if (schemaList != null) {
					for (String sch : Arrays.asList(schemaList.split(","))) {
						if (schemaPattern.matcher(sch).matches()) {
							schemasOfCat.add(sch);
							logger.debug("  Adding schema "+sch);
						} else {
							logger.debug("Skipping schema "+sch+" (doesn't match pattern)");
						}
					}
				}
				if (logger.isDebugEnabled()) logger.debug("Putting schemas "+schemasOfCat+" under map key '"+cat+"'");
				schemas.put(cat, schemasOfCat);
			}
		} else {
			// leave schemas map empty
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Found Catalogs: "+catalogs);
			logger.debug("Found Schemas: "+schemas);
		}

		for (String cat : catalogs) {
			if (cat.equals(NO_CATALOG)) {
				if (schemas.get(cat) == null) {
					// no catalogs, no schemas
					String tableList = connection.getProperties().getProperty("tables");
					if (tableList == null) throw new SQLException("Missing property: 'tables'");
					for (String table : Arrays.asList(tableList.split(","))) {
						if (tablePattern.matcher(table).matches()) {
							rs.addRow();
							rs.updateObject(3, table);
							rs.updateObject(4, "TABLE");
						} else {
							logger.debug("Skipping table "+table+" (doesn't match pattern)");
						}
					}
				} else {
					// schemas, but no catalogs
					List<String> schemasOfCat = schemas.get(cat);
					for (String sch : schemasOfCat) {
						String tableList = connection.getProperties().getProperty("tables."+sch);
						if (tableList == null) throw new SQLException("Missing property: 'tables."+sch+"'");
						for (String table : Arrays.asList(tableList.split(","))) {
							if (tablePattern.matcher(table).matches()) {
								rs.addRow();
								rs.updateObject(2, sch);
								rs.updateObject(3, table);
								rs.updateObject(4, "TABLE");
							} else {
								logger.debug("Skipping table "+table+" (doesn't match pattern)");
							}
						}
					}
				}
			} else {
				if (getSchemaTerm() == null) {
					// catalogs, but no schemas
					String tableList = connection.getProperties().getProperty("tables."+cat);
					if (tableList == null) throw new SQLException("Missing property: 'tables."+cat+"'");
					for (String table : Arrays.asList(tableList.split(","))) {
						if (tablePattern.matcher(table).matches()) {
							rs.addRow();
							rs.updateObject(1, cat);
							rs.updateObject(3, table);
							rs.updateObject(4, "TABLE");
						} else {
							logger.debug("Skipping table "+table+" (doesn't match pattern)");
						}
					}
				} else {
					// schemas and catalogs
					if (schemas.get(cat) != null) {
						// this catalog has schemas
						for (String sch : schemas.get(cat)) {
							String tableList = connection.getProperties().getProperty("tables."+cat+"."+sch);
							if (tableList == null) throw new SQLException("Missing property: 'tables."+cat+"."+sch+"'");
							for (String table : Arrays.asList(tableList.split(","))) {
								if (tablePattern.matcher(table).matches()) {
									rs.addRow();
									rs.updateObject(1, cat);
									rs.updateObject(2, sch);
									rs.updateObject(3, table);
									rs.updateObject(4, "TABLE");
								} else {
									logger.debug("Skipping table "+table+" (doesn't match pattern)");
								}
							}
						}
					}
				}
			}
		}
		
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

	/**
	 * Returns a sorted list of schemas in the current catalog, if there is one.
	 * Otherwise returns a sorted list of schemas in all catalogs.
	 */
	public ResultSet getSchemas() throws SQLException {
		String catalogList = connection.getProperties().getProperty("catalogs");
		
		// mapping of schema name to catalog name.  treemap keeps it ordered by schema
		Map<String,String> schemas = new TreeMap<String,String>();
		
		if (getSchemaTerm() == null) {
			logger.debug("getSchemas: schemaTerm==null; returning empty result set");
		} else if (getCatalogTerm() == null) {
			String schemaList = connection.getProperties().getProperty("schemas");
			logger.debug("getSchemas: catalogTerm==null; schemaList="+schemaList);
			if (schemaList == null) throw new SQLException("Missing property: 'schemas'");
			for (String schName : Arrays.asList(schemaList.split(","))) {
				schemas.put(schName, null);
				if (logger.isDebugEnabled()) logger.debug("getSchemas: put '"+schName+"'");
			}
		} else {
			logger.debug("getSchemas: database has catalogs and schemas!");
			String restrictToCatalog = connection.getCatalog();

			for (String catName : Arrays.asList(catalogList.split(","))) {
				if (restrictToCatalog != null &&
						!restrictToCatalog.equalsIgnoreCase(catName)) continue;
				
				String schemaList = connection.getProperties().getProperty("schemas."+catName);
				if (schemaList == null) throw new SQLException("Missing property: 'schemas."+catName+"'");
				for (String schName : Arrays.asList(schemaList.split(","))) {
					schemas.put(schName, catName);
					if (logger.isDebugEnabled()) logger.debug("getSchemas: put '"+catName+"'.'"+schName+"'");
				}
			}
		}

		// now populate the result set, ordered by schema name
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 2);
		rs.setColumnName(1, "TABLE_SCHEM");
		rs.setColumnName(2, "TABLE_CATALOG");
		for (Map.Entry<String,String> e : schemas.entrySet()) {
			rs.addRow();
			rs.updateObject(1, e.getKey());
			rs.updateObject(2, e.getValue());
		}
		rs.beforeFirst();
		return rs;
	}

	public ResultSet getCatalogs() throws SQLException {
		String catalogList = connection.getProperties().getProperty("catalogs");
		if (logger.isDebugEnabled()) logger.debug("getCatalogs: user-supplied catalog list is '"+catalogList+"'");
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 1);
		rs.setColumnName(1, "TABLE_CAT");
		if (getCatalogTerm() != null) {
			for (String catName : Arrays.asList(catalogList.split(","))) {
				rs.addRow();
				rs.updateObject(1, catName);
				if (logger.isDebugEnabled()) logger.debug("getCatalogs: added '"+catName+"'");
			}
		}
		rs.beforeFirst();
		return rs;
	}

	public ResultSet getTableTypes() throws SQLException {
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 1);
		rs.setColumnName(1, "TABLE_TYPE");
		rs.updateObject(1, "TABLE");
		rs.beforeFirst();
		return rs;
	}

	public ResultSet getColumns(String catalog, String schemaPattern,
			String tableNamePattern, String columnNamePattern)
			throws SQLException {
        logger.debug("getColumns: Searching for:");
        logger.debug("    catalog '"+catalog+"' (pattern )");
        logger.debug("     schema '"+schemaPattern+"' (pattern )");
        logger.debug("      table '"+tableNamePattern+"' (pattern )");
        logger.debug("     column '"+columnNamePattern+"' (pattern )");
        
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 22);
		rs.setColumnName(1, "TABLE_CAT");
		rs.setColumnName(2, "TABLE_SCHEM");
		rs.setColumnName(3, "TABLE_NAME");
		rs.setColumnName(4, "COLUMN_NAME");
		rs.setColumnName(5, "DATA_TYPE");
		rs.setColumnName(6, "TYPE_NAME");
		rs.setColumnName(7, "COLUMN_SIZE");
		rs.setColumnName(8, "BUFFER_LENGTH");
		rs.setColumnName(9, "DECIMAL_DIGITS");
		rs.setColumnName(10, "NUM_PREC_RADIX");
		rs.setColumnName(11, "NULLABLE");
		rs.setColumnName(12, "REMARKS");
		rs.setColumnName(13, "COLUMN_DEF");
		rs.setColumnName(14, "SQL_DATA_TYPE");
		rs.setColumnName(15, "SQL_DATETIME_SUB");
		rs.setColumnName(16, "CHAR_OCTET_LENGTH");
		rs.setColumnName(17, "ORDINAL_POSITION");
		rs.setColumnName(18, "IS_NULLABLE");
		rs.setColumnName(19, "SCOPE_CATLOG");
		rs.setColumnName(20, "SCOPE_SCHEMA");
		rs.setColumnName(21, "SCOPE_TABLE");
		rs.setColumnName(22, "SOURCE_DATA_TYPE");

        // FIXME: doesn't support null catalog, schema, or table patterns yet!
        
        StringBuffer colListPropName = new StringBuffer();
        colListPropName.append("columns.");
        if (getCatalogTerm() != null) {
            if (catalog == null) {
                // FIXME: this should be made optional, but it's a lot of work
                throw new SQLException("Catalog name is mandatory for this JDBC Driver.");
            }
            colListPropName.append(catalog).append(".");
        }
        if (getSchemaTerm() != null) {
            if (schemaPattern == null) {
                // FIXME: this should be made optional, but it's a lot of work
                throw new SQLException("Schema name is mandatory for this JDBC Driver.");
            }
            colListPropName.append(schemaPattern).append(".");
        }
        colListPropName.append(tableNamePattern);
        
        logger.debug("getColumns: property name for column list is '"+colListPropName+"'");
        String columnList = connection.getProperties().getProperty(colListPropName.toString());
        logger.debug("getColumns: user-supplied column list is '"+columnList+"'");
        if (columnList == null) {
            columnList = tableNamePattern+"_col_1,"
                        +tableNamePattern+"_col_2,"
                        +tableNamePattern+"_col_3,"
                        +tableNamePattern+"_col_4";
        }
        int colNo = 1;
        for (String colName : Arrays.asList(columnList.split(","))) {
            rs.addRow();
            rs.updateObject(1, catalog);
            rs.updateObject(2, schemaPattern);
            rs.updateObject(3, tableNamePattern);
            rs.updateObject(4, colName);
            rs.updateInt(5, Types.VARCHAR);
            rs.updateObject(6, "VARCHAR");
            rs.updateInt(7, 20);
            rs.updateObject(8, null);
            rs.updateInt(9, 0);
            rs.updateInt(10, 0);
            rs.updateInt(11, columnNoNulls);
            rs.updateObject(12, null);
            rs.updateObject(13, null);
            rs.updateInt(14, 0);
            rs.updateInt(15, 0);
            rs.updateInt(16, 20);
            rs.updateInt(17, colNo);
            rs.updateObject(18, "NO");
            rs.updateObject(19, null);
            rs.updateObject(20, null);
            rs.updateObject(21, null);
            rs.updateObject(22, null);
            colNo++;
        }

        rs.beforeFirst();
        return rs;
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
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 6);
		rs.setColumnName(1, "TABLE_CAT");
		rs.setColumnName(2, "TABLE_SCHEM");
		rs.setColumnName(3, "TABLE_NAME");
		rs.setColumnName(4, "COLUMN_NAME");
		rs.setColumnName(5, "KEY_SEQ");
		rs.setColumnName(6, "PK_NAME");
		
		// TODO: define primary keys
		
		return rs;
	}

	public ResultSet getImportedKeys(String catalog, String schema, String table)
			throws SQLException {
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 14);
		rs.setColumnName(1, "PKTABLE_CAT");
		rs.setColumnName(2, "PKTABLE_SCHEM");
		rs.setColumnName(3, "PKTABLE_NAME");
		rs.setColumnName(4, "PKCOLUMN_NAME");
		rs.setColumnName(5, "FKTABLE_CAT");
		rs.setColumnName(6, "FKTABLE_SCHEM");
		rs.setColumnName(7, "FKTABLE_NAME");
		rs.setColumnName(8, "FKCOLUMN_NAME");
		rs.setColumnName(9, "KEY_SEQ");
		rs.setColumnName(10, "UPDATE_RULE");
		rs.setColumnName(11, "DELETE_RULE");
		rs.setColumnName(12, "FK_NAME");
		rs.setColumnName(13, "PK_NAME");
		rs.setColumnName(14, "DEFERRABILITY");
		
		// TODO: define imported keys
		
		return rs;
	}
	
	public ResultSet getExportedKeys(String catalog, String schema, String table)
			throws SQLException {
		MockJDBCResultSet rs = new MockJDBCResultSet(null, 14);
		rs.setColumnName(1, "PKTABLE_CAT");
		rs.setColumnName(2, "PKTABLE_SCHEM");
		rs.setColumnName(3, "PKTABLE_NAME");
		rs.setColumnName(4, "PKCOLUMN_NAME");
		rs.setColumnName(5, "FKTABLE_CAT");
		rs.setColumnName(6, "FKTABLE_SCHEM");
		rs.setColumnName(7, "FKTABLE_NAME");
		rs.setColumnName(8, "FKCOLUMN_NAME");
		rs.setColumnName(9, "KEY_SEQ");
		rs.setColumnName(10, "UPDATE_RULE");
		rs.setColumnName(11, "DELETE_RULE");
		rs.setColumnName(12, "FK_NAME");
		rs.setColumnName(13, "PK_NAME");
		rs.setColumnName(14, "DEFERRABILITY");
		
		// TODO: define imported keys

		return rs;
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
        MockJDBCResultSet rs = new MockJDBCResultSet(null, 13);
        rs.setColumnName(1, "TABLE_CAT");
        rs.setColumnName(2, "TABLE_SCHEM");
        rs.setColumnName(3, "TABLE_NAME");
        rs.setColumnName(4, "NON_UNIQUE");
        rs.setColumnName(5, "INDEX_QUALIFIER");
        rs.setColumnName(6, "INDEX_NAME");
        rs.setColumnName(7, "TYPE");
        rs.setColumnName(8, "ORDINAL_POSITION");
        rs.setColumnName(9, "COLUMN_NAME");
        rs.setColumnName(10, "ASC_OR_DESC");
        rs.setColumnName(11, "CARDINALITY");
        rs.setColumnName(12, "PAGES");
        rs.setColumnName(13, "FILTER_CONDITION");
        
        // TODO: define indices
        
        return rs;
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
