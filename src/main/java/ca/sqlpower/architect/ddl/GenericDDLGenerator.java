/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.sqlpower.architect.ddl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.DepthFirstSearch;
import ca.sqlpower.architect.ddl.DDLStatement.StatementType;
import ca.sqlpower.architect.profile.ProfileFunctionDescriptor;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.object.SPResolverRegistry;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLCheckConstraint;
import ca.sqlpower.sqlobject.SQLCheckConstraintVariableResolver;
import ca.sqlpower.sqlobject.SQLCheckConstraintVariableResolver.SQLCheckConstraintVariable;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLEnumeration;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.sqlobject.SQLTypePhysicalProperties.SQLTypeConstraint;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider.PropertyType;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.util.SQLPowerUtils;

public class GenericDDLGenerator implements DDLGenerator {

	public static final String GENERATOR_VERSION = "$Revision$";

	private static final Logger logger = Logger.getLogger(GenericDDLGenerator.class);

	public String getName() {
	    return "Generic SQL-92";
	}

     /**
     * Check to see if the word word is on the list of reserved words for this database
     * @return
     */
    public boolean isReservedWord(String word){
        return false;
    }
	/**
	 * This property says whether or not the user will allow us to
	 * connect to the target system in order to determine database
	 * meta-data.  This generic base class will fail if
	 * allowConnection == false.
	 */
	protected boolean allowConnection;

	/**
	 * This is where each DDL statement gets accumulated while it is
	 * being generated.
	 */
	private StringBuffer ddl;

	/**
	 * Complete DDL statements (of type DDLStatement) are accumulated in this list.
	 */
	private List<DDLStatement> ddlStatements;

	/**
	 * This is initialized to the System line.separator property.
	 */
	protected static final String EOL = System.getProperty("line.separator");

	/**
	 * A mapping from JDBC type code (Integer values) to
	 * GenericTypeDescriptor objects which describe that data type.
	 */
	protected Map<Integer, GenericTypeDescriptor> typeMap;

	/**
	 * This variable will be a live, non-null connection to the target
	 * database (set up by writeDDL) if allowConnection is true.
	 */
	protected Connection con;

	/**
	 * As table and relationship creation statements are generated,
	 * their SQL identifiers are stored in this map (key is name,
	 * value is object having that name).  Warnings are created when
	 * multiple objects in this top-level scope use the same name.
     * XXX Consider changing this to a Set as it appears that the values
     * stored in the Map are never used.
	 */
	protected Map<String, SQLObject> topLevelNames;

	/**
	 * The name of the catalog in the target database that the
	 * generated DDL statements should create the objects in.  Not all
	 * databases have catalogs; subclasses of GenericDDLGenerator which
	 * target catalogless platforms should set this value to
	 * <code>null</code> as well as override {@link #getCatalogTerm()}
	 * to return <code>null</code>.
	 */
	protected String targetCatalog;

	/**
	 * The name of the schema in the target database that the
	 * generated DDL statements should create the objects in.  Not all
	 * databases have schemas; subclasses of GenericDDLGenerator which
	 * target schemaless platforms should set this value to
	 * <code>null</code> as well as override {@link #getSchemaTerm()}
	 * to return <code>null</code>.
	 */
	protected String targetSchema;

    /**
     * A mapping from JDBC type code (Integer values) to
     * appliable profile functions (min,max,avg,sum,etc...)
     * XXX This doesn't seem to be used at all
     */
    protected Map<String, ProfileFunctionDescriptor> profileFunctionMap;

    /**
     * used to quote the physical name.
     * default value is 'false'. set to 'true' only when comparing Data Model for Postgres Database.
     */
    protected boolean isComparingDMForPostgres = false;
    
    private ArchitectSwingSession session;

    private JDBCDataSourceType dsType = null;

    protected String identifierQuoteChar = "";
    protected String identifierQuoteCharRight = "";

    public boolean isIdentifierQuoted() {
      return !identifierQuoteChar.isBlank();
    }

    public String getQuoteLeft() {
      return identifierQuoteChar;
    }

    public String getQuoteRight() {
      return identifierQuoteCharRight;
    }
    
    public GenericDDLGenerator(boolean allowConnection) throws SQLException {
        this.allowConnection = allowConnection;
        ddlStatements = new ArrayList<DDLStatement>();
        ddl = new StringBuffer(500);
        println("");
        topLevelNames = new CaseInsensitiveHashMap();  // for tracking dup table/relationship names
        createTypeMap();
    }

    /**
     * Creates a new generic DDL generator that's allowed to connect to the target database.
     */
	public GenericDDLGenerator() throws SQLException {
	    this(true);
	}

    public String generateDDLScript(ArchitectSwingSession architectSwingSession, Collection<SQLTable> tables) throws SQLException, SQLObjectException {
        session = architectSwingSession;
        if (session.getProjectSettings().isQuoteIdentifiers()) {
          if (this instanceof SQLServerDDLGenerator) {
            identifierQuoteChar="[";
            identifierQuoteCharRight="]";
          } else {
            identifierQuoteChar = "\"";
            identifierQuoteCharRight = "\"";
          }
        } else {
          identifierQuoteChar = "";
          identifierQuoteCharRight = "";
        }
        List<DDLStatement> statements = generateDDLStatements(tables);

		ddl = new StringBuffer(4000);
		writeHeader();
		writeDDLTransactionBegin();

		Iterator<DDLStatement> it = statements.iterator();
		while (it.hasNext()) {
			DDLStatement ddlStmt = (DDLStatement) it.next();
			ddl.append(ddlStmt.getSQLText());
			println(getStatementTerminator());
		}

		writeDDLTransactionEnd();
		return ddl.toString();
	}


	/**
     * Creates a series of SQL DDL statements which will create the given list of
     * tables in a target database.  The script will include commands for defining
     * the tables, their primary keys, other indices, and the foreign key relationships
     * between them.
     *
     * @param tables the tables the generated script should create.
     * @return the list of DDL statements in the order they should be executed
	 * @see ca.sqlpower.architect.ddl.DDLGenerator#generateDDLStatements(Collection)
	 */
	public final List<DDLStatement> generateDDLStatements(Collection<SQLTable> tables) throws SQLException, SQLObjectException {
		ddlStatements = new ArrayList<DDLStatement>();
		ddl = new StringBuffer(500);
        topLevelNames = new CaseInsensitiveHashMap();

        /*
         * topological sort ensures parent tables are created before their
         * children. This is not strictly necessary, but apparently all the
         * cool tools do it this way. :)
         */
        List<SQLTable> tableList = new ArrayList<SQLTable>(tables);
        DepthFirstSearch dfs = new DepthFirstSearch(tableList);
        tableList = dfs.getFinishOrder();
        SQLDatabase parentDb = SQLPowerUtils.getAncestor(tableList.get(0), SQLDatabase.class);
        dsType = parentDb.getDataSource().getParentType();
		try {
			if (allowConnection && tableList.size() > 0) {
                if (parentDb.isPlayPenDatabase()) {
                    con = null;
                } else {
                    con = parentDb.getConnection();
                }
			} else {
				con = null;
			}

			createTypeMap();

			for (SQLTable t : tableList) {

				addTable(t);

                for (SQLIndex index : t.getIndices()) {
                   if (index.isPrimaryKeyIndex()) continue;
                    addIndex(index);
                }
			}

            for (SQLTable t : tableList) {
				writeExportedRelationships(t);
			}

		} finally {
			try {
				if (con != null) con.close();
			} catch (SQLException ex) {
				logger.error("Couldn't close connection", ex);
			}
		}
		return ddlStatements;
	}

	/**
	 * Stores all the ddl since the last call to endStatement as a SQL
	 * statement. You have to call this at the end of each statement.
	 *
	 * @param type the type of statement
	 * @param sqlObject the object to which the statement pertains
	 */
	public final void endStatement(StatementType type, SQLObject sqlObject) {
		if (logger.isInfoEnabled()) {
			logger.info("endStatement: " + ddl.toString());
		}

		ddlStatements.add(new DDLStatement(sqlObject, type, ddl.toString(), getStatementTerminator(), getTargetCatalog(), getTargetSchema()));
		ddl = new StringBuffer(500);
		println("");
	}

	public void writeHeader() {
		println("-- Created by SQLPower Generic DDL Generator "+GENERATOR_VERSION+" --");
	}

	/**
	 * Prints a single semicolon character (no newline).  If your
	 * database needs something else, override this method.
	 */
	public String getStatementTerminator() {
		return ";";
	}

	/**
	 * Does nothing.  If your target system supports transactional
	 * DDL, override this method and print the appropriate statement.
	 */
	public void writeDDLTransactionBegin() {
        // not supported in generic case
	}

	/**
	 * Does nothing.  If your target system supports transactional
	 * DDL, override this method and print the appropriate statement.
	 */
	public void writeDDLTransactionEnd() {
        // not supported in generic case
	}

	public void writeCreateDB(SQLDatabase db) {
		println("-- Would Create Database "+db.getName()+" here. --");
	}

	public void renameRelationship(SQLRelationship oldFK, SQLRelationship newFK) {
        dropRelationship(oldFK);
		addRelationship(newFK);
	}

	public void dropRelationship(SQLRelationship r) {

		print("\nALTER TABLE ");

		print( toQualifiedName(r.getFkTable()) );
		print(" DROP CONSTRAINT ");
		print(createPhysicalName(topLevelNames, r));
		endStatement(StatementType.DROP, r);
	}

    /**
     * Adds a statement for creating the given foreign key relationship in
     * the target database.  Depends on the {@link #getDeferrabilityClause(SQLRelationship)}
     * method for the target database's way of describing the deferrability policy.
     */
	public void addRelationship(SQLRelationship r) {
	    StringBuilder sql = new StringBuilder();
	    StringBuilder errorMsg = new StringBuilder();
	    boolean skipStatement = false;

	    StringBuilder typesMismatchMsg = new StringBuilder();

	    sql.append("\nALTER TABLE ");
		sql.append( toQualifiedName(r.getFkTable()) );
		sql.append(" ADD CONSTRAINT ");
		sql.append(createPhysicalName(topLevelNames, r));
		sql.append("\nFOREIGN KEY (");
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject> ();
		boolean firstColumn = true;

		for (ColumnMapping cm : r.getChildren(ColumnMapping.class)) {
			SQLColumn c = cm.getFkColumn();
			// make sure this is unique
			if (colNameMap.get(c.getName()) == null) {
				if (firstColumn) {
					firstColumn = false;
					sql.append(createPhysicalName(colNameMap, c));
				} else {
					sql.append(", " + createPhysicalName(colNameMap, c));
				}
				colNameMap.put(c.getName(), c);
			}
		}
		sql.append(")");

        sql.append("\nREFERENCES ");
		sql.append(toQualifiedName(r.getPkTable()));
		sql.append(" (");
		colNameMap.clear();
		firstColumn = true;

		if (r.getChildren().isEmpty()) {
		    errorMsg.append("Warning: Relationship has no columns to map:\n");
		}

		for (ColumnMapping cm : r.getChildren(ColumnMapping.class)) {
			SQLColumn c = cm.getPkColumn();
			SQLColumn fkCol = cm.getFkColumn();

			// checks the fk column and pk column are the same type,
			// generates DDLWarning if not the same.
			if (ArchitectUtils.columnTypesDiffer(c.getType(), fkCol.getType())) {
			    typesMismatchMsg.append("        " + c + " -- " + fkCol + "\n");
			}
			// make sure this is unique
			if (colNameMap.get(c.getName()) == null) {
				if (firstColumn) {
					firstColumn = false;
					sql.append(createPhysicalName(colNameMap, c));
				} else {
					sql.append(", " + createPhysicalName(colNameMap, c));
				}
				colNameMap.put(c.getName(), c);
			}
		}

		sql.append(")");

		// adds to error msg if there were types mismatch
		if (typesMismatchMsg.length() != 0) {
		    errorMsg.append("Warning: Column types mismatch in the following column mapping(s):\n");
		    errorMsg.append(typesMismatchMsg.toString());
		}

		if (supportsDeleteAction(r)) {
		    String deleteActionClause = getDeleteActionClause(r);

		    // avoid useless newline for empty clause
		    if (deleteActionClause.length() > 0) {
		        sql.append("\n").append(deleteActionClause);
		    }
		} else {
		    errorMsg.append("Warning: " + getName() + " does not support this relationship's " +
		            "delete action (" + r.getDeleteRule() + ").\n");
		}

		if (supportsUpdateAction(r)) {
            String updateActionClause = getUpdateActionClause(r);

            // avoid useless newline for empty clause
            if (updateActionClause.length() > 0) {
                sql.append("\n").append(updateActionClause);
            }
		} else {
            errorMsg.append("Warning: " + getName() + " does not support this relationship's " +
                    "update action (" + r.getUpdateRule() + ").\n");
		}

		// adds to error msg if the deferrability was not a supported feature,
		// add the deferrability clause otherwise.
		if (supportsDeferrabilityPolicy(r)) {
		    String deferrabilityClause = getDeferrabilityClause(r);

		    // avoid useless newline when clause is empty
		    if (deferrabilityClause.length() > 0) {
		        sql.append("\n").append(deferrabilityClause);
		    }
		} else {
		    errorMsg.append("Warning: " + getName() + " does not support this relationship's " +
		            "deferrability policy (" + r.getDeferrability() + ").\n");
		}

		// properly comment the relationship create statement,
		// i.e. entire statement or just the error message.
		if (errorMsg.length() != 0) {
		    if (skipStatement) {
		        sql.append("*/");
		    } else {
		        errorMsg.append("*/");
		    }
		    sql.insert(0, "\n/*\n" + errorMsg.toString());
		}

        print(sql.toString());

		endStatement(StatementType.CREATE, r);

	}

    /**
     * Returns true if this DDL generator supports the given relationship's
     * delete action. The generic DDL generator claims to support all delete
     * actions, so specific platforms that don't support all delete actions
     * should override this method.
     */
    public boolean supportsDeleteAction(SQLRelationship r) {
        return true;
    }

    /**
     * Returns the ON DELETE clause for the given relationship, with no
     * extra whitespace or newline characters around it.
     * <p>
     * If you are overriding this method for a platform-specific DDL generator
     * and you need this clause to be empty, return the empty string--not null.
     *
     * @param r The relationship whose delete action clause to generate
     * @return The delete action clause
     */
    public String getDeleteActionClause(SQLRelationship r) {
        return "ON DELETE " + getUpdateDeleteRule(r.getDeleteRule());
    }

    /**
     * Returns the words for the given update or delete action.
     */
    private String getUpdateDeleteRule(UpdateDeleteRule rule) {
        String action;
        if (rule == UpdateDeleteRule.CASCADE) {
            action = "CASCADE";
        } else if (rule == UpdateDeleteRule.NO_ACTION) {
            action = "NO ACTION";
        } else if (rule == UpdateDeleteRule.RESTRICT) {
            action = "RESTRICT";
        } else if (rule == UpdateDeleteRule.SET_DEFAULT) {
            action = "SET DEFAULT";
        } else if (rule == UpdateDeleteRule.SET_NULL) {
            action = "SET NULL";
        } else {
            throw new IllegalArgumentException("Unknown enum value: " + rule);
        }
        return action;
    }

    /**
     * Returns true if this DDL generator supports the given relationship's
     * update action. The generic DDL generator claims to support all update
     * actions, so specific platforms that don't support all update actions
     * should override this method.
     */
    public boolean supportsUpdateAction(SQLRelationship r) {
        return true;
    }

    /**
     * Returns the ON UPDATE clause for the given relationship, with no
     * extra whitespace or newline characters around it.
     * <p>
     * If you are overriding this method for a platform-specific DDL generator
     * and you need this clause to be empty, return the empty string--not null.
     *
     * @param r The relationship whose update action clause to generate
     * @return The update action clause
     */
    public String getUpdateActionClause(SQLRelationship r) {
        return "ON UPDATE " + getUpdateDeleteRule(r.getUpdateRule());
    }

    /**
     * Returns the correct syntax for setting the deferrability of a foreign
     * key relationship on this DDL Generator's target platform. Throws
     * an {@link UnsupportedOperationException} if the platform does not
     * support the given relationship's deferrability policy.
     *
     * @param r The relationship the deferrability clause is for
     * @return The SQL clause for declaring the deferrability policy
     * in r.
     */
    public String getDeferrabilityClause(SQLRelationship r) {
        if (supportsDeferrabilityPolicy(r)){
            if (r.getDeferrability() == Deferrability.NOT_DEFERRABLE) {
                return "NOT DEFERRABLE";
            } else if (r.getDeferrability() == Deferrability.INITIALLY_DEFERRED) {
                return "DEFERRABLE INITIALLY DEFERRED";
            } else if (r.getDeferrability() == Deferrability.INITIALLY_IMMEDIATE) {
                return "DEFERRABLE INITIALLY IMMEDIATE";
            } else {
                throw new IllegalArgumentException("Unknown deferrability policy: " + r.getDeferrability());
            }
        } else {
            throw new UnsupportedOperationException(getName() + " does not support " +
                    r.getName() + "'s deferrability policy (" + r.getDeferrability() + ").");
        }
    }

    /**
     * Returns true if the platform supports the deferrability policy of
     * the given relationship, false otherwise. This generic method assumes
     * support for all deferrability policies.
     * @return Whether the chosen platform supports the deferrability.
     */
    public boolean supportsDeferrabilityPolicy(SQLRelationship r) {
        if (!Arrays.asList(Deferrability.values()).contains(r.getDeferrability())) {
            throw new IllegalArgumentException("Unknown deferrability policy: " + r.getDeferrability());
        } else {
            return true;
        }
    }

	/**
	 * Generate the SQL to rename a table.
	 * <br>
	 * The default implementation works for PostgreSQL, Oracle, HSQLDB, H2
	 * 
	 * @param oldTable
	 * @param newTable
	 */
    public void renameTable(SQLTable oldTable, SQLTable newTable) {
        println("ALTER TABLE " + oldTable.getPhysicalName() + " RENAME TO " + newTable.getPhysicalName());
        endStatement(StatementType.ALTER, newTable);
    }

    public void addComment(SQLObject o) {
        // TODO: move remarks storage to SQLObject in order to support comments for all object types
    }

    public void modifyComment(SQLObject o) {
        if (o instanceof SQLTable) {
            addComment((SQLTable)o, false);
        } else if (o instanceof SQLColumn) {
            addComment((SQLColumn)o);
        }
    }

    public void addComment(SQLColumn c) {
        if (c.getRemarks() == null || c.getRemarks().trim().length() == 0) return;

        print("COMMENT ON COLUMN ");
        print(toQualifiedName(c.getParent()));
        print(".");
        print(getPhysicalName(c));
        print(" IS '");
        print(c.getRemarks().replaceAll("'", "''"));
        print("'");
        endStatement(StatementType.COMMENT, c);
    }

    /**
     * Add the SQL to define the comment for the table.
     *
     * TODO: [thomas kellerer] currently this is only called from within addTable() as there
     * is not modifyTable() I don't currently know how to include updated comments
     * in the SQL when forward engineering a diff between two models.
     * 
     * @param t the table for which to add the comment
     * @param includeColumns if true, column comments will be added for all columns
     *        by calling addComment(SQLColumn) for each column
     * @see #addComment(ca.sqlpower.sqlobject.SQLColumn)
     */
	public void addComment(SQLTable t, boolean includeColumns) {
		if (t.getRemarks() != null && t.getRemarks().trim().length() > 0) {
			print("COMMENT ON TABLE ");
			print(toQualifiedName(t));
			print(" IS '");
			print(t.getRemarks().replaceAll("'", "''"));
			print("'");
			endStatement(StatementType.COMMENT, t);
		}

		if (includeColumns) {
			addColumnComments(t);
		}
	}

	protected void addColumnComments(SQLTable t) {
		try {
			for (SQLColumn col : t.getColumns()) {
				addComment(col);
			}
		} catch (SQLObjectException ex) {
			logger.error("Could not add column remarks for table " + t, ex);
		}
		print("\n");
	}

	/**
	 * Generate the SQL to rename a column.
	 * <br>
	 * The default implementation works for PostgreSQL, Oracle
	 * @param oldCol
	 * @param newCol
	 */
	public void renameColumn(SQLColumn oldCol, SQLColumn newCol) {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
		print("\nALTER TABLE ");
		print(toQualifiedName(oldCol.getParent()));
		print(" RENAME COLUMN ");
		print(createPhysicalName(colNameMap, oldCol));
        print(" TO ");
		print(createPhysicalName(colNameMap, newCol));
		endStatement(StatementType.ALTER, oldCol);
    }

	public void addColumn(SQLColumn c) {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
		print("\nALTER TABLE ");
		print(toQualifiedName(c.getParent()));
		print(" ADD COLUMN ");
		print(columnDefinition(c,colNameMap));
		endStatement(StatementType.CREATE, c);

	}

	public void dropColumn(SQLColumn c) {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
		print("\nALTER TABLE ");
		print(toQualifiedName(c.getParent()));
		print(" DROP COLUMN ");
		print(createPhysicalName(colNameMap,c));
		endStatement(StatementType.DROP, c);

	}

	public void modifyColumn(SQLColumn c, DiffChunk<SQLObject> diffChunk) {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
		SQLTable t = c.getParent();
		print("\nALTER TABLE ");
		print(toQualifiedName(t));
		print(" ALTER COLUMN ");
		print(columnDefinition(c,colNameMap));
		endStatement(StatementType.MODIFY, c);
        addComment(c);
	}
	
	public void dropTable(SQLTable t) {
        print(makeDropTableSQL(t.getName()));
        endStatement(StatementType.DROP, t);
    }

	/**
	 * Creates a SQL DDL snippet which consists of the column name, data type,
	 * default value, and nullability clauses.
	 *
	 * @param c The column to generate the DDL snippet for.
	 * @param colNameMap Dirty hack for coming up with unique physical names.
	 * The final physical name generated in the SQL snippet will be stored
	 * in this map. If you don't care about producing unique column names, just
	 * pass in a freshly-created map. See {@link #createPhysicalName(Map, SQLObject)}
	 * for more information.
	 * @return The SQL snippet that describes the given column. The returned string
	 * is not delimited at the beginning or end: you're responsible for properly putting
	 * it in the context of a valid SQL statement.
	 */
	protected String columnDefinition(SQLColumn c, Map<String, SQLObject> colNameMap) {
	    return columnDefinition(c, colNameMap, true);
	}

    /**
     * Creates a SQL DDL snippet which consists of the column name, data type,
     * default value, and nullability clauses.
     * 
     * @param c
     *            The column to generate the DDL snippet for.
     * @param colNameMap
     *            Dirty hack for coming up with unique physical names. The final
     *            physical name generated in the SQL snippet will be stored in
     *            this map. If you don't care about producing unique column
     *            names, just pass in a freshly-created map. See
     *            {@link #createPhysicalName(Map, SQLObject)} for more
     *            information.
     * @param alterNullability
     *            If true the nullability of the column will be changed. If
     *            false it will be skipped. For Oracle if a column is currently
     *            null you cannot alter it to be null again.
     * @return The SQL snippet that describes the given column. The returned
     *         string is not delimited at the beginning or end: you're
     *         responsible for properly putting it in the context of a valid SQL
     *         statement.
     */
	protected String columnDefinition(SQLColumn c, Map<String, SQLObject> colNameMap, boolean alterNullability) {
        StringBuffer def = new StringBuffer();

        // Column name
        def.append(createPhysicalName(colNameMap, c));

        def.append(" ");
        def.append(columnType(c));

        UserDefinedSQLType type = c.getUserDefinedSQLType();
        String defaultValue = type.getDefaultValue(getPlatformName());
        if ( defaultValue != null && !defaultValue.equals("")) {
            def.append(" ");
            def.append("DEFAULT ");
            def.append(defaultValue);
        }

        if (alterNullability) {
            def.append(columnNullability(c));
        }
        
        List<SQLCheckConstraint> checkConstraints;
        List<SQLEnumeration> enumerations;
        SQLTypeConstraint constraintType = type.getConstraintType(getPlatformName());
        if (constraintType == null) {
            constraintType = type.getDefaultPhysicalProperties().getConstraintType();
            checkConstraints = type.getDefaultPhysicalProperties().getChildrenWithoutPopulating(SQLCheckConstraint.class);
            enumerations = type.getDefaultPhysicalProperties().getChildrenWithoutPopulating(SQLEnumeration.class);
        } else {
            checkConstraints = type.getCheckConstraints(getPlatformName());
            enumerations = type.getEnumerations(getPlatformName());
        }
        
        // Add check constraint.
        if (constraintType == SQLTypeConstraint.CHECK) {
            String columnCheckConstraint = columnCheckConstraint(c, checkConstraints);
            if (columnCheckConstraint != null && columnCheckConstraint.length() > 0) {
                def.append(" " + columnCheckConstraint);
            }
            
        // Add enumeration.
        } else if (constraintType == SQLTypeConstraint.ENUM) {
            String columnEnumeration = columnEnumeration(c, enumerations);
            if (columnEnumeration != null && columnEnumeration.length() > 0) {
                def.append(" " + columnEnumeration);
            }
        }

        logger.debug("column definition "+ def.toString());
        return def.toString();
    }

    protected String columnNullability(SQLColumn c) {
        GenericTypeDescriptor td = failsafeGetTypeDescriptor(c);
        if (c.isDefinitelyNullable()) {
			if (! td.isNullable()) {
				throw new UnsupportedOperationException
					("The data type "+td.getName()+" is not nullable on the target database platform.");
			}
			return "";
		} else {
			return " NOT NULL";
		}
    }

    /**
     * Creates a SQL DDL snippet for defining column check constraint. This
     * method follows the SQL-92 standard for check constraints. However, some
     * platforms may use check constraints differently. Thus, extending classes
     * should override this method if it does not follow the SQL-92 check
     * constraint standard.
     * 
     * XXX Note that many platforms require unique check constraint names not
     * only across all columns, but also all tables. It would be desirable to
     * not allow the user to enter duplicate check constraint names in the UI.
     * 
     * @param c
     *            The {@link SQLColumn} the check constraint applies to.
     * @param checkConstraints
     *            The {@link String} of check constraints that may use variables
     *            defined by the {@link SQLCheckConstraintVariable} enum and can
     *            be resolved by the {@link SQLCheckConstraintVariableResolver}.
     * @return The generated SQL DDL snippet for defining column check
     *         constraints.
     */
    protected String columnCheckConstraint(SQLColumn c, List<SQLCheckConstraint> checkConstraints) {
        if (!supportsCheckConstraint() || 
                c == null || 
                checkConstraints == null || 
                checkConstraints.isEmpty()) {
            return "";
        }
        
        SPVariableResolver resolver = c.getVariableResolver();
        SPVariableHelper helper = new SPVariableHelper(c);
        SPResolverRegistry.register(c, resolver);
        
        StringBuilder sb = new StringBuilder();
        for (SQLCheckConstraint constraint : checkConstraints) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(String.format("CONSTRAINT %s CHECK (%s)", 
                    constraint.getName(),
                    helper.substitute(constraint.getConstraint())));
        }
        
        SPResolverRegistry.deregister(c, resolver);
        
        return sb.toString();
    }

    /**
     * Creates a SQL DDL snippet for defining column enumerations. SQL-92 does
     * not define enumerations. Since this is mostly platform specifically, all
     * extending {@link DDLGenerator}s that do support enumerations should
     * override this method. Some platforms do not support enumerations, so they
     * must be implemented using check constraints instead.
     * 
     * @param c
     *            The {@link SQLColumn} the enumeration applies to.
     * @param enumerations
     *            The {@link List} of enumerated values.
     * @return The generated SQL DDL snippet for defining column enumerations.
     */
    protected String columnEnumeration(SQLColumn c, List<SQLEnumeration> enumerations) {
        if (supportsCheckConstraint()) {
            return columnEnumToCheckConstraint(c, enumerations);
        } else {
            return "";
        }
    }

    /**
     * Creates a SQL DDL snippet for defining column enumerations on a platform
     * that does not support enumerations, by defining them as check constraints
     * instead. Platforms that do not support this syntax style: CHECK (COLUMN =
     * 'ENUM1' OR COLUMN = 'ENUM2') should have their respective
     * {@link DDLGenerator} override this method. If this method is overridden,
     * the {@link #columnCheckConstraint(SQLColumn, List)} method likely needs
     * to be overridden as well.
     * 
     * @param c
     *            The {@link SQLColumn} the enumeration applies to.
     * @param enumerations
     *            The {@link String} array of enumerated types
     * @return The generated SQL DDL snippet for using check constraints to
     *         define column enumerations.
     */
    protected String columnEnumToCheckConstraint(SQLColumn c, List<SQLEnumeration> enumerations) {
        if (c == null || enumerations == null || enumerations.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (SQLEnumeration enumeration : enumerations) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("'" + enumeration.getName() + "'");
        }
        
        return "CHECK (" + getPhysicalName(c) + " IN (" + 
                sb.toString() + "))";
    }

    protected String getPlatformName() {
        return SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM;
    }
    
	/** Column type */
    public String columnType(SQLColumn c) {
        StringBuffer def = new StringBuffer();
        UserDefinedSQLType columnType = c.getUserDefinedSQLType();
        if (columnType.getUpstreamType() != null) {
            def.append(columnType.getUpstreamType().getPhysicalName(getPlatformName()));
        } else {
            def.append(columnType.getPhysicalName(getPlatformName()));
        }
        
        int precision = columnType.getPrecision(getPlatformName());
        int scale = columnType.getScale(getPlatformName());
        PropertyType precisionType = columnType.getPrecisionType(getPlatformName());
        PropertyType scaleType = columnType.getScaleType(getPlatformName());
        
		if (precisionType != PropertyType.NOT_APPLICABLE && 
		        scaleType != PropertyType.NOT_APPLICABLE && 
		        precision > 0 && scale > 0) {
			def.append("("+columnType.getPrecision(getPlatformName()));
			def.append(","+columnType.getScale(getPlatformName())+")");
		} else if (precisionType != PropertyType.NOT_APPLICABLE && precision > 0) {
		    def.append("("+columnType.getPrecision(getPlatformName())+")");
		} else if (scaleType != PropertyType.NOT_APPLICABLE && scale > 0) {
		    def.append("("+columnType.getScale(getPlatformName())+")");
		}
        return def.toString();
    }

    /** Column type */
    public String getColumnDataTypeName(SQLColumn c) {
        StringBuffer def = new StringBuffer();
        GenericTypeDescriptor td = failsafeGetTypeDescriptor(c);
        def.append(td.getName());
        if (td.getHasPrecision()) {
            def.append("("+c.getPrecision());
            if (td.getHasScale()) {
                def.append(","+c.getScale());
            }
            def.append(")");
        }
        return def.toString();
    }

    /**
     * Returns the type descriptor for the given column's type if that exists in this generator's typemap,
     * else returns the default type.
     */
    protected GenericTypeDescriptor failsafeGetTypeDescriptor(SQLColumn c) {
        GenericTypeDescriptor td = (GenericTypeDescriptor) typeMap.get(Integer.valueOf(c.getType()));
		if (td == null) {
		    td = (GenericTypeDescriptor) typeMap.get(getDefaultType());
		    if (td == null) {
		        throw new NullPointerException("Current type map does not have entry for default datatype!");
		    }
        }
        return td;
    }

	public void addTable(SQLTable t) throws SQLException, SQLObjectException {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();  // for detecting duplicate column names
		// generate a new physical name if necessary
		createPhysicalName(topLevelNames,t); // also adds generated physical name to the map
		print("\nCREATE TABLE ");
		print( toQualifiedName(t) );
		println(" (");
		boolean firstCol = true;
		Iterator<SQLColumn> it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();

			if (!firstCol) println(",");
			print("                ");

			print(columnDefinition(c,colNameMap));

			firstCol = false;
		}

		SQLIndex pk = t.getPrimaryKeyIndex();
		if (pk.getChildCount() > 0) {
		    print(",\n");
            print("                ");
		    writePKConstraintClause(pk);
		}

		print("\n)");
		endStatement(StatementType.CREATE, t);
        addComment(t, true);
	}

	/**
	 * Returns the default data type for this platform.  Normally, this can be VARCHAR,
	 * but if your platform doesn't have a varchar, override this method.
	 */
	protected Object getDefaultType() {
		return Types.VARCHAR;
	}

    /**
     * Writes out the primary key constraint clause for the given primary key
     * index. The clause begins with the word "CONSTRAINT" and has no leading
     * space, so you must write a newline, space, and/or indentation before
     * calling this method.
     * <p>
     * Side effect: the PK object's name will be initialized if necessary.
     *
     * @param pk
     *            The primary key index
     * @throws SQLObjectException
     *             If the pk object wasn't already populated and the populate
     *             attempt fails.
     */
	protected void writePKConstraintClause(SQLIndex pk) throws SQLObjectException {
	    if (!pk.isPrimaryKeyIndex()) {
	        throw new IllegalArgumentException("The given index is not a primary key");
	    }
        createPhysicalName(topLevelNames, pk);
	    print("CONSTRAINT ");
	    print(getPhysicalName(pk));
	    print(" PRIMARY KEY (");

	    boolean firstCol = true;
	    for (SQLIndex.Column col : pk.getChildren(SQLIndex.Column.class)) {
	        if (!firstCol) print(", ");
	        if (col.getColumn() == null) {
	            throw new IllegalStateException("Index column is not associated with the real column in the table.");
	        } else {
	            print(getPhysicalName(col.getColumn()));
	        }
	        firstCol = false;
	    }
	    print(")");
	}

	protected void writePrimaryKey(SQLTable t) throws SQLObjectException {
	    println("");
	    print("ALTER TABLE ");
	    print( toQualifiedName(t) );
	    print(" ADD ");

	    writePKConstraintClause(t.getPrimaryKeyIndex());

	    endStatement(StatementType.ADD_PK, t);
	}

    /**
     * Adds statements for creating every exported key in the given table.
     */
	protected void writeExportedRelationships(SQLTable t) throws SQLObjectException {
		Iterator<SQLRelationship> it = t.getExportedKeys().iterator();
		while (it.hasNext()) {
			SQLRelationship rel = (SQLRelationship) it.next();
			addRelationship(rel);
		}
	}

	/**
	 * Creates and populates <code>typeMap</code> using
	 * DatabaseMetaData.  Subclasses for specific DB platforms will be
	 * able to override this implementation with one that uses a
	 * static, pre-defined type map.
	 */
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap<Integer, GenericTypeDescriptor>();
		if (con == null || !allowConnection) {
			// Add generic type map
			typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 2000, "0x", null, DatabaseMetaData.columnNullable, true, false));
            typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, false, false));
            typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("BOOLEAN", Types.BOOLEAN, 1, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
			typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 10, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("LONGVARBINARY", Types.LONGVARBINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("LONGVARCHAR", Types.LONGVARCHAR, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
			typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 3, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 8000, null, null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(SQLType.NVARCHAR), new GenericTypeDescriptor("NVARCHAR", SQLType.NVARCHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(SQLType.NCHAR), new GenericTypeDescriptor("NCHAR", SQLType.NCHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(SQLType.NCLOB), new GenericTypeDescriptor("NCLOB", SQLType.NCLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, false, false));
		}
		else
		{
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet rs = dbmd.getTypeInfo();
			while (rs.next()) {
				GenericTypeDescriptor td = new GenericTypeDescriptor(rs);
				typeMap.put(Integer.valueOf(td.getDataType()), td);
			}
			rs.close();
		}
	}

	protected void println(String text) {
		ddl.append(text).append(EOL);
	}

	protected void print(String text) {
		ddl.append(text);
	}

	/**
	 * Converts space to underscore in <code>name</code> and returns
	 * the possibly-modified string.  This will not be completely
	 * sufficient because it leaves ".", "%", and lots of other
	 * non-alphanumeric characters alone. Subclasses might choose to
	 * quote and leave everything alone, or whatever.
	 */
	public String toIdentifier(String name) {
        if (name == null) return null;
		else return name.replace(' ', '_');
	}

	/**
     * Creates a fully-qualified table name from the given table's phyiscal name
     * and this DDL Generator's current target schema and catalog.
     *
     * @param t The table whose name to qualify.  The parents of this table are
     * disregarded; only the DDL Generator's target schema and catalog matter.
     * @return A string of the form <tt>[catalog.][schema.]table</tt> (catalog and
     * schema are omitted if null).
	 */
	public String toQualifiedName(SQLTable t) {
        return DDLUtils.toQualifiedName(
                t.getCatalogName(),
                t.getSchemaName(),
                t.getPhysicalName(),
                identifierQuoteChar,
                identifierQuoteCharRight);
	}

	/**
	 * Creates a qualified name from the physical name of the SQLIndex
	 */
	public String toQualifiedName(SQLIndex i) {
        return toQualifiedName(i.getPhysicalName());
    }

    /**
     * Creates a fully-qualified table name from the given string (which
     * is the non-qualified table name) and this DDL Generator's current
     * target schema and catalog.
     *
     * @param tname The table name to qualify. Must not contain the name separator
     * character (usually '.').
     * @return A string of the form <tt>[catalog.][schema.]table</tt> (catalog and
     * schema are omitted if null).
     */
    public String toQualifiedName(String tname) {
        String catalog = getTargetCatalog();
        String schema = getTargetSchema();
        return DDLUtils.toQualifiedName(catalog, schema, tname, identifierQuoteChar, identifierQuoteCharRight);
    }

	// ---------------------- accessors and mutators ----------------------

	/**
	 * Gets the value of allowConnection
	 *
	 * @return the value of allowConnection
	 */
	public boolean getAllowConnection()  {
		return this.allowConnection;
	}

	/**
	 * Sets the value of allowConnection
	 *
	 * @param argAllowConnection Value to assign to this.allowConnection
	 */
	public void setAllowConnection(boolean argAllowConnection) {
		this.allowConnection = argAllowConnection;
	}

	/**
	 * Gets the value of typeMap
	 *
	 * @return the value of typeMap
	 */
	public Map<Integer, GenericTypeDescriptor> getTypeMap()  {
		return this.typeMap;
	}

	/**
	 * Sets the value of typeMap
	 *
	 * @param argTypeMap Value to assign to this.typeMap
	 */
	public void setTypeMap(Map<Integer, GenericTypeDescriptor> argTypeMap) {
		this.typeMap = argTypeMap;
	}

    public Map<String, ProfileFunctionDescriptor> getProfileFunctionMap() {
        return this.profileFunctionMap;
    }
    public void setProfileFunctionMap(Map<String, ProfileFunctionDescriptor> profileFunctionMap) {
        this.profileFunctionMap = profileFunctionMap;
    }

	/**
	 * Gets the value of con
	 *
	 * @return the value of con
	 */
	public Connection getCon()  {
		return this.con;
	}

	/**
	 * Sets the value of con
	 *
	 * @param argCon Value to assign to this.con
	 */
	public void setCon(Connection argCon) {
		this.con = argCon;
	}

	/**
	 * See {@link #targetCatalog}.
	 *
	 * @return the value of targetCatalog
	 */
	public String getTargetCatalog()  {
	    return getQuotedPhysicalName(this.targetCatalog);
	}

	/**
	 * See {@link #targetCatalog}.
	 *
	 * @param argTargetCatalog Value to assign to this.targetCatalog
	 */
	public void setTargetCatalog(String argTargetCatalog) {
		this.targetCatalog = argTargetCatalog;
	}

	/**
	 * See {@link #targetSchema}.
	 *
	 * @return the value of targetSchema
	 */
	public String getTargetSchema()  {
	    return getQuotedPhysicalName(this.targetSchema);
	}

	/**
	 * See {@link #targetSchema}.
	 *
	 * @param argTargetSchema Value to assign to this.targetSchema
	 */
	public void setTargetSchema(String argTargetSchema) {
		this.targetSchema = argTargetSchema;
	}

	/**
	 * The name that the target database gives to the JDBC idea of
	 * "catalog."  For Oracle, this would be null (no catalogs) and
	 * for SQL Server it would be "Database".
	 */
	public String getCatalogTerm() {
		return null;
	}

	/**
	 * The name that the target database gives to the JDBC idea of
	 * "schema."  For Oracle, this would be "Schema" and for SQL
	 * Server it would be "Owner".
	 */
	public String getSchemaTerm() {
		return null;
	}

	/**  
	 * Generate, set, and return a valid identifier for this SQLObject.
	 * @param dupCheck
	 * @param so
	 * @return
	 */
	protected String createPhysicalName(Map<String, SQLObject> dupCheck, SQLObject so) {
        logger.debug("transform identifier source: " + so.getPhysicalName());
        if ((so instanceof SQLTable || so instanceof SQLColumn) &&
                (so.getPhysicalName() != null && !so.getPhysicalName().trim().equals(""))) {
		    String physicalName = so.getPhysicalName();
		    logger.debug("The physical name for this SQLObject is: " + physicalName);
		} else {
		    so.setPhysicalName(toIdentifier(so.getName()));
		}
        logger.debug("The logical name field now is: " + so.getName());

		return getPhysicalName(so);
	}

    /**
     * Generates a standard <code>DROP TABLE $tablename</code> command.  Should work on most platforms.
     */
    public String makeDropTableSQL(String table) {
        return "\nDROP TABLE "+toQualifiedName(table);
    }

    /**
     * Generates a command for dropping a foreign key which works on some platforms.
     * The statement looks like <code>ALTER TABLE $fktable DROP FOREIGN KEY $fkname</code>.
     */
    public String makeDropForeignKeySQL(String fkTable, String fkName) {
        return "\nALTER TABLE "
            +toQualifiedName(fkTable)
            +" DROP FOREIGN KEY "
            +fkName;
    }

	public List<DDLStatement> getDdlStatements() {
		return ddlStatements;
	}

	public void dropPrimaryKey(SQLTable t) throws SQLObjectException {
	    SQLIndex pk = t.getPrimaryKeyIndex();
	    print("\nALTER TABLE " + toQualifiedName(t.getName())
	            + " DROP CONSTRAINT " + getPhysicalName(pk));
		endStatement(StatementType.DROP, t);
	}

	public void addPrimaryKey(SQLTable t) throws SQLObjectException {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
		StringBuffer sqlStatement = new StringBuffer();
		boolean first = true;
		sqlStatement.append("\nALTER TABLE "+ toQualifiedName(t.getName())
				+ " ADD PRIMARY KEY (");
		for (SQLColumn c : t.getColumns()) {
			if (c.isPrimaryKey()) {
				if (!first) {
					sqlStatement.append(",");
				}else{
					first =false;
				}

				sqlStatement.append(createPhysicalName(colNameMap,c));
			}
		}
		sqlStatement.append(")");
		if (!first)
		{
			print(sqlStatement.toString());
			endStatement(StatementType.CREATE,t);
		}
	}

	/**
	 * Drop the specified index.
	 * <br>
	 * The default implementation should work for all databases.
	 * 
	 * @param index the index to drop.
	 * @throws SQLObjectException
	 */
	public void dropIndex(SQLIndex index) throws SQLObjectException {
		print("DROP INDEX ");
		println(toQualifiedName(index));
		endStatement(StatementType.DROP, index);
	}

	/**
	 * Rename an index.
	 * The default implementation works for PostgreSQL, Oracle, H2, HSQLDB
	 * @param oldIndex
	 * @param newIndex
	 * @throws SQLObjectException
	 */
	public void renameIndex(SQLIndex oldIndex, SQLIndex newIndex) throws SQLObjectException {
		print("ALTER INDEX ");
		print(toQualifiedName(oldIndex));
		print(" RENAME TO ");
		println(toQualifiedName(newIndex.getName()));
		endStatement(StatementType.ALTER, oldIndex);
	}

    /**
     * Adds a DDL statement to this generator that will create the
     * given index.
     *
     * @param index The specification of the index to create.  Note,
     * if the index type is STATISTIC, no DDL will be generated because
     * STATISTIC indices are just artificial JDBC constructs to describe
     * table statistics (you can't create or drop them).
     */
    public void addIndex(SQLIndex index) throws SQLObjectException {
        createPhysicalName(topLevelNames, index);

        println("");
        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }

        print("INDEX ");
        print(toQualifiedName(index.getName()));
        print("\n ON ");
        print(toQualifiedName(index.getParent()));
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : index.getChildren(SQLIndex.Column.class)) {
            if (!first) print(", ");
            if (c.getColumn() != null) {
                print(getPhysicalName(c.getColumn()));
            } else {
                print(c.getName());
            }
            print(c.getAscendingOrDescending() == AscendDescend.ASCENDING ? " ASC" : "");
            print(c.getAscendingOrDescending() == AscendDescend.DESCENDING ? " DESC" : "");
            first = false;
        }
        print(" )");
        endStatement(StatementType.CREATE, index);
    }

    /**
     * The generic DDL generator claims to support rollback
     * operation, so specific platforms that don't support it
     * should override this method.
     */
    public boolean supportsRollback() {
        return true;
    }

    /**
     * The generic DDL generator supports check constraint on columns. Specific
     * platforms that do not support it should override this method.
     * 
     * @return true if this {@link DDLGenerator} supports column check
     *         constraints.
     */
    public boolean supportsCheckConstraint() {
        return true;
    }

    /**
     * The generic DDL generator does not support enumeration on columns.
     * Specific platforms that do support it should override this method.
     * Platforms that do not support enumerations but support check constraints
     * can simply emulate them with its check constraint equivalent instead.
     * 
     * @return true if this {@link DDLGenerator} supports column enumeration.
     */
    public boolean supportsEnumeration() {
        return false;
    }

    /**
     * 
     * @param name
     * @return
     */
    public String getQuotedPhysicalName(String name) {
        logger.debug(" getQuotedphysical name: "+name);
        if (name != null && !name.isBlank()) {
          return identifierQuoteChar+name+identifierQuoteCharRight;
        }
        return name;
    }


    @Override
    public String getPhysicalName(SQLObject c) {
        String name = c.getPhysicalName();
        name = getQuotedPhysicalName(name);
        return name;
    }

    @Override
    public void setComparingDMForPostgres(boolean isComparingDMForPostgres) {
        this.isComparingDMForPostgres = isComparingDMForPostgres;
    }

    /**
     * @return the isComparingDMForPostgres
     */
    public boolean isComparingDMForPostgres() {
        return isComparingDMForPostgres;
    }

    /**
     * @return the session
     */
    protected ArchitectSwingSession getSession() {
        return session;
    }

    /**
     * @return the dsType
     */
    protected JDBCDataSourceType getDsType() {
        return dsType;
    }

    /**
     * @param dsType the dsType to set
     */
    protected void setDsType(JDBCDataSourceType dsType) {
        this.dsType = dsType;
    }
   
}
