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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.DepthFirstSearch;
import ca.sqlpower.architect.profile.ProfileFunctionDescriptor;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPObjectUtils;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLSequence;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;

public class GenericDDLGenerator extends AbstractSPObject implements DDLGenerator {

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
	protected Map typeMap;

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
	 * This list contains 0 or more {@link NameChangeWarning} objects.  It is
	 * populated as statements are added to the
	 * <code>ddlStatements</code> list.  If non-empty, this list of
	 * warnings should be presented to the user before the generated
	 * DDL is saved or executed (to give them a chance to fix the
	 * warnings and try again).
	 */
	protected List warnings;

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


    public GenericDDLGenerator(boolean allowConnection) throws SQLException {
        this.allowConnection = allowConnection;
        warnings = new ArrayList();
        ddlStatements = new ArrayList();
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

    public String generateDDLScript(Collection<SQLTable> tables) throws SQLException, SQLObjectException {
        List statements = generateDDLStatements(tables);

		ddl = new StringBuffer(4000);
		writeHeader();
		writeDDLTransactionBegin();

		Iterator it = statements.iterator();
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
        warnings = new ArrayList();
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

		try {
			if (allowConnection && tableList.size() > 0) {
                SQLDatabase parentDb = SPObjectUtils.getAncestor(tableList.get(0), SQLDatabase.class);
				con = parentDb.getConnection();
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
	public final void endStatement(DDLStatement.StatementType type, SQLObject sqlObject) {
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

	public void dropRelationship(SQLRelationship r) {

		print("\nALTER TABLE ");

		print( toQualifiedName(r.getFkTable()) );
		print(" DROP CONSTRAINT ");
		print(createPhysicalName(topLevelNames, r));
		endStatement(DDLStatement.StatementType.DROP, r);
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
		    warnings.add(new RelationshipMapsNoColumnsDDLWarning(r.getPkTable(), r.getFkTable()));
		    errorMsg.append("Warning: Relationship has no columns to map:\n");
		}

		for (ColumnMapping cm : r.getChildren(ColumnMapping.class)) {
			SQLColumn c = cm.getPkColumn();
			SQLColumn fkCol = cm.getFkColumn();

			// checks the fk column and pk column are the same type,
			// generates DDLWarning if not the same.
			if (ArchitectUtils.columnTypesDiffer(c.getType(), fkCol.getType())) {
			    warnings.add(new RelationshipColumnsTypesMismatchDDLWarning(c, fkCol));
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

		// sanity check for SET NULL and SET DEFAULT delete rules, whether or not DB supports them
        for (SQLRelationship.ColumnMapping cm : r.getChildren(ColumnMapping.class)) {
            UpdateDeleteRule deleteRule = r.getDeleteRule();
            SQLColumn fkcol = cm.getFkColumn();
            if (deleteRule == UpdateDeleteRule.SET_NULL && !fkcol.isDefinitelyNullable()) {
                warnings.add(new SetNullOnNonNullableColumnWarning(fkcol));
            } else if (deleteRule == UpdateDeleteRule.SET_DEFAULT &&
                       (fkcol.getDefaultValue() == null || fkcol.getDefaultValue().length() == 0)) {
                warnings.add(new SetDefaultOnColumnWithNoDefaultWarning(fkcol));
            }
        }

		if (supportsDeleteAction(r)) {
		    String deleteActionClause = getDeleteActionClause(r);

		    // avoid useless newline for empty clause
		    if (deleteActionClause.length() > 0) {
		        sql.append("\n").append(deleteActionClause);
		    }
		} else {
		    warnings.add(new UnsupportedFeatureDDLWarning(
		            getName() + " does not support " + r.getName() + "'s delete action", r));
		    errorMsg.append("Warning: " + getName() + " does not support this relationship's " +
		            "delete action (" + r.getDeleteRule() + ").\n");
		}

		// sanity check for SET NULL and SET DEFAULT update rules, whether or not DB supports them
        for (SQLRelationship.ColumnMapping cm : r.getChildren(SQLRelationship.ColumnMapping.class)) {
            UpdateDeleteRule updateRule = r.getUpdateRule();
            SQLColumn fkcol = cm.getFkColumn();
            if (updateRule == UpdateDeleteRule.SET_NULL && !fkcol.isDefinitelyNullable()) {
                warnings.add(new SetNullOnNonNullableColumnWarning(fkcol));
            } else if (updateRule == UpdateDeleteRule.SET_DEFAULT &&
                       (fkcol.getDefaultValue() == null || fkcol.getDefaultValue().length() == 0)) {
                warnings.add(new SetDefaultOnColumnWithNoDefaultWarning(fkcol));
            }
        }

		if (supportsUpdateAction(r)) {
            String updateActionClause = getUpdateActionClause(r);

            // avoid useless newline for empty clause
            if (updateActionClause.length() > 0) {
                sql.append("\n").append(updateActionClause);
            }
		} else {
            warnings.add(new UnsupportedFeatureDDLWarning(
                    getName() + " does not support " + r.getName() + "'s update action", r));
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
		    warnings.add(new UnsupportedFeatureDDLWarning(
                    getName() + " does not support " + r.getName() + "'s deferrability policy", r));
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

		endStatement(DDLStatement.StatementType.CREATE, r);

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
        print(c.getPhysicalName());
        print(" IS '");
        print(c.getRemarks().replaceAll("'", "''"));
        print("'");
        endStatement(DDLStatement.StatementType.COMMENT, c);
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
			endStatement(DDLStatement.StatementType.COMMENT, t);
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

	public void addColumn(SQLColumn c) {
		Map colNameMap = new HashMap();
		print("\nALTER TABLE ");
		print(toQualifiedName(c.getParent()));
		print(" ADD COLUMN ");
		print(columnDefinition(c,colNameMap));
		endStatement(DDLStatement.StatementType.CREATE, c);

	}

	public void dropColumn(SQLColumn c) {
		Map colNameMap = new HashMap();
		print("\nALTER TABLE ");
		print(toQualifiedName(c.getParent()));
		print(" DROP COLUMN ");
		print(createPhysicalName(colNameMap,c));
		endStatement(DDLStatement.StatementType.DROP, c);

	}

	public void modifyColumn(SQLColumn c) {
		Map colNameMap = new HashMap();
		SQLTable t = c.getParent();
		print("\nALTER TABLE ");
		print(toQualifiedName(t));
		print(" ALTER COLUMN ");
		print(columnDefinition(c,colNameMap));
		endStatement(DDLStatement.StatementType.MODIFY, c);
        addComment(c);
	}

	public void dropTable(SQLTable t) {
        print(makeDropTableSQL(t.getName()));
        endStatement(DDLStatement.StatementType.DROP, t);
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
	protected String columnDefinition(SQLColumn c, Map colNameMap) {
        StringBuffer def = new StringBuffer();

        // Column name
        def.append(createPhysicalName(colNameMap, c));

        def.append(" ");
        def.append(columnType(c));

        if ( c.getDefaultValue() != null && !c.getDefaultValue().equals("")) {
            def.append(" ");
            def.append("DEFAULT ");
            def.append(c.getDefaultValue());
        }

        def.append(columnNullability(c));

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

	/** Columnn type */
    public String columnType(SQLColumn c) {
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

    /** Columnn type */
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
		    GenericTypeDescriptor oldType = new GenericTypeDescriptor
		    (c.getSourceDataTypeName(), c.getType(), c.getPrecision(),
		            null, null, c.getNullable(), false, false);
		    oldType.determineScaleAndPrecision();
		    TypeMapDDLWarning o = new TypeMapDDLWarning(c, String.format(
                    "Type '%s' of column '%s' in table '%s' is unknown in the target platform",
                    SQLType.getTypeName(c.getType()),
                    c.getPhysicalName(),
                    c.getParent().getPhysicalName()), oldType, td);
		   if (!contains(warnings, o)) {
		       warnings.add(o);
		   }
        }
        return td;
    }

    private boolean contains(List list, TypeMapDDLWarning o) {
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof TypeMapDDLWarning) {
                if (((TypeMapDDLWarning)next).getMessage().equals(o.getMessage())) {
                    return true;
                }
            }
        }
        return false;
    }

	public void addTable(SQLTable t) throws SQLException, SQLObjectException {
		Map colNameMap = new HashMap();  // for detecting duplicate column names
		// generate a new physical name if necessary
		createPhysicalName(topLevelNames,t); // also adds generated physical name to the map
		print("\nCREATE TABLE ");
		print( toQualifiedName(t) );
		println(" (");
		boolean firstCol = true;
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();

			if (!firstCol) println(",");
			print("                ");

			print(columnDefinition(c,colNameMap));

			firstCol = false;
		}

		SQLIndex pk = t.getPrimaryKeyIndex();
		if (pk != null) {
		    print(",\n");
            print("                ");
		    writePKConstraintClause(pk);
		}

		print("\n)");
		endStatement(DDLStatement.StatementType.CREATE, t);
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
	    print(pk.getPhysicalName());
	    print(" PRIMARY KEY (");

	    boolean firstCol = true;
	    for (SQLIndex.Column col : pk.getChildren(SQLIndex.Column.class)) {
	        if (!firstCol) print(", ");
	        if (col.getColumn() == null) {
	            throw new IllegalStateException("Index column is not associated with the real column in the table.");
	        } else {
	            print(col.getColumn().getPhysicalName());
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

	    endStatement(DDLStatement.StatementType.ADD_PK, t);
	}

    /**
     * Adds statements for creating every exported key in the given table.
     */
	protected void writeExportedRelationships(SQLTable t) throws SQLObjectException {
		Iterator it = t.getExportedKeys().iterator();
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
		typeMap = new HashMap();
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
		return toQualifiedName(t.getPhysicalName());
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

        return DDLUtils.toQualifiedName(catalog, schema, tname);
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
	public Map getTypeMap()  {
		return this.typeMap;
	}

	/**
	 * Sets the value of typeMap
	 *
	 * @param argTypeMap Value to assign to this.typeMap
	 */
	public void setTypeMap(Map argTypeMap) {
		this.typeMap = argTypeMap;
	}

    public Map<String, ProfileFunctionDescriptor> getProfileFunctionMap() {
        return this.profileFunctionMap;
    }
    public void setProfileFunctionMap(Map profileFunctionMap) {
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
	 * Returns {@link #warnings}.
	 */
	public List getWarnings() {
		return warnings;
	}

	/**
	 * See {@link #targetCatalog}.
	 *
	 * @return the value of targetCatalog
	 */
	public String getTargetCatalog()  {
		return this.targetCatalog;
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
		return this.targetSchema;
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
	 * @throws SQLObjectException
     */
	protected String createPhysicalName(Map<String, SQLObject> dupCheck, SQLObject so) {
        logger.debug("transform identifier source: " + so.getPhysicalName());
        if ((so instanceof SQLTable || so instanceof SQLColumn) &&
                (so.getPhysicalName() != null && !so.getPhysicalName().trim().equals(""))) {
		    String physicalName = so.getPhysicalName();
		    logger.debug("The physical name for this SQLObject is: " + physicalName);
		    if (physicalName.lastIndexOf(' ') != -1) {
		        String renameTo = (toIdentifier(physicalName));
                warnings.add(new InvalidNameDDLWarning(
		                String.format("Name %s has white spaces", physicalName),
		                Arrays.asList(new SQLObject[] {so}),
		                String.format("Rename %s to %s", physicalName, renameTo ),
		                so, renameTo));
		    }
		} else {
		    so.setPhysicalName(toIdentifier(so.getName()));
		}
        String physicalName = so.getPhysicalName();
        if(isReservedWord(physicalName)) {
            String renameTo = physicalName   + "_1";
            warnings.add(new InvalidNameDDLWarning(
                    String.format("%s is a reserved word", physicalName),
                    Arrays.asList(new SQLObject[] { so }),
                    String.format("Rename %s to %s", physicalName, renameTo),
                    so, renameTo));
            return physicalName;
        }
        logger.debug("The logical name field now is: " + so.getName());
        logger.debug("The physical name field now is: " + physicalName);
        int pointIndex = so.getPhysicalName().lastIndexOf('.');
        if (!so.getPhysicalName().substring(pointIndex+1,pointIndex+2).matches("[a-zA-Z]")){
            String renameTo;
            if (so instanceof SQLTable) {
                renameTo = "Table_" + so.getPhysicalName();
            } else if (so instanceof SQLColumn) {
                renameTo = "Column_" + so.getPhysicalName();
            } else if (so instanceof SQLIndex) {
                renameTo = "Index_" + so.getPhysicalName();
            } else {
                renameTo = "X_" + so.getPhysicalName();
            }
            warnings.add(new InvalidNameDDLWarning(
                    String.format("Name %s starts with a non-alpha character", physicalName),
                    Arrays.asList(new SQLObject[] { so }),
                    String.format("Rename %s to %s", physicalName, renameTo),
                    so, renameTo));
            return physicalName;
        }

        logger.debug("transform identifier result: " + so.getPhysicalName());
        // XXX should change checkDupName(Map where, so.getPhysicalName(), so, "Duplicate Physical Name", so.getName());

        String physicalName2 = so.getPhysicalName();
        SQLObject object = dupCheck.get(physicalName2);
        if (object == null) {
			dupCheck.put(physicalName2, so);
        } else {

            int count = 1;
            String renameTo2;
            SQLObject object2;
            do {
                renameTo2 = physicalName2 + "_" + count;
                object2 = dupCheck.get(renameTo2);
                count++;
            } while (object2 != null);

            String message;
            if (so instanceof SQLColumn) {
                message = String.format("Column name %s in table %s already in use",
                        so.getPhysicalName(),
                        ((SQLColumn) so).getParent().getPhysicalName());
            } else {
                message = String.format("Global name %s already in use", physicalName2);
            }
            logger.debug("Rename to : " + renameTo2);

            warnings.add(new InvalidNameDDLWarning(
                    message,
                    Arrays.asList(new SQLObject[] { so }),
                    String.format("Rename %s to %s", physicalName2, renameTo2),
                    so, renameTo2));

            dupCheck.put(renameTo2, so);

		}

		return so.getPhysicalName();
	}

	/**
     * Generate, set, and return a valid identifier for this SQLSequence.
     * Has a side effect of changing the given SQLColumn's autoIncrementSequenceName.
     * @throws SQLObjectException
     *
     * @param dupCheck  The Map to check for duplicate names
     * @param seq       The SQLSequence to generate, set and return a valid identifier for.
     * @param col       The SQLColumn to where the side effect should occur.
     */
    protected String createSeqPhysicalName(Map<String, SQLObject> dupCheck, SQLSequence seq, SQLColumn col) {
        logger.debug("transform identifier source: " + seq.getPhysicalName());
        seq.setPhysicalName(toIdentifier(seq.getName()));
        String physicalName = seq.getPhysicalName();
        if(isReservedWord(physicalName)) {
            String renameTo = physicalName   + "_1";
            warnings.add(new InvalidSeqNameDDLWarning(
                    String.format("%s is a reserved word", physicalName),
                    seq, col,
                    String.format("Rename %s to %s", physicalName, renameTo),
                    renameTo));
            return physicalName;
        }

        int pointIndex = seq.getPhysicalName().lastIndexOf('.');
        if (!seq.getName().substring(pointIndex+1,pointIndex+2).matches("[a-zA-Z]")){
            String renameTo = "Seq_" + seq.getName();
            warnings.add(new InvalidSeqNameDDLWarning(
                    String.format("Name %s starts with a non-alpha character", physicalName),
                    seq, col,
                    String.format("Rename %s to %s", physicalName, renameTo),
                    renameTo));
            return physicalName;
        }

        logger.debug("transform identifier result: " + seq.getPhysicalName());
        // XXX should change checkDupName(Map where, so.getPhysicalName(), so, "Duplicate Physical Name", so.getName());

        String physicalName2 = seq.getPhysicalName();
        SQLObject object = dupCheck.get(physicalName2);
        if (object == null) {
            dupCheck.put(physicalName2, seq);
        } else {

            int count = 1;
            String renameTo2;
            SQLObject object2;
            do {
                renameTo2 = physicalName2 + "_" + count;
                object2 = dupCheck.get(renameTo2);
                count++;
            } while (object2 != null);

            String message = String.format("Global name %s already in use", physicalName);
            logger.debug("Rename to : " + renameTo2);

            warnings.add(new InvalidSeqNameDDLWarning(
                    message,
                    seq, col,
                    String.format("Rename %s to %s", physicalName, renameTo2),
                    renameTo2));

            dupCheck.put(renameTo2, seq);

        }

        return seq.getPhysicalName();
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
	            + " DROP CONSTRAINT " + pk.getPhysicalName());
		endStatement(DDLStatement.StatementType.DROP, t);
	}

	public void addPrimaryKey(SQLTable t) throws SQLObjectException {
		Map colNameMap = new HashMap();
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
			endStatement(DDLStatement.StatementType.CREATE,t);
		}
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
        for (SQLIndex.Column c : (List<SQLIndex.Column>) index.getChildren()) {
            if (!first) print(", ");
            print(c.getColumn().getPhysicalName());
            print(c.getAscendingOrDescending() == AscendDescend.ASCENDING ? " ASC" : "");
            print(c.getAscendingOrDescending() == AscendDescend.DESCENDING ? " DESC" : "");
            first = false;
        }
        print(" )");
        endStatement(DDLStatement.StatementType.CREATE, index);
    }

    /**
     * The generic DDL generator claims to support rollback
     * operation, so specific platforms that don't support it
     * should override this method.
     */
    public boolean supportsRollback() {
        return true;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (childType.isAssignableFrom(DDLStatement.class)) {
            return 0;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
        types.add(DDLStatement.class);
        return null;
    }

    public List<? extends SPObject> getChildren() {
        List<SPObject> children = new ArrayList<SPObject>();
        children.addAll(ddlStatements);
        return children;
    }

    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
                
    }
}
