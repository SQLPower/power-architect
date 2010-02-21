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

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class LiquibaseDDLGenerator extends GenericDDLGenerator implements DDLGenerator {

	public static final String GENERATOR_VERSION = "$Revision: 3271 $";

	private static final Logger logger = Logger.getLogger(LiquibaseDDLGenerator.class);

    public LiquibaseDDLGenerator() throws SQLException {
		super(false);
    }

	public String getName() {
	    return "Liquibase";
	}

	public void writeHeader() {
		println("<!-- Created by SQLPower Generic DDL Generator "+GENERATOR_VERSION+" -->");
	}

	/**
	 * Returns nothing. Not needed for Liquibase
	 */
	public String getStatementTerminator() {
		return "";
	}

	/**
	 * Does nothing.  Not needed for Liquibase
	 */
	public void writeDDLTransactionBegin() {
		println("<changeSet author=\"CHANGEME\" id=\"CHANGEME\">");
	}

	/**
	 * Does nothing.  Not needed for Liquibase
	 */
	public void writeDDLTransactionEnd() {
		println("</changeSet>");
	}

	public void writeCreateDB(SQLDatabase db) {
		// not necessary for Liquibase
	}

	public void dropRelationship(SQLRelationship r) {

		print("<dropForeignKeyConstraint ");
		print(getTableQualifier(r.getFkTable(), "baseTableName", "baseSchemaName"));
		print(" constraintName=\"");
		print(getName(r));
		println("\"/>");
		endStatement(DDLStatement.StatementType.XMLTAG, r);
	}

	private String getName(SQLObject o) {
		if (StringUtils.isEmpty(o.getPhysicalName())) {
			return o.getName();
		}
		return o.getPhysicalName();
	}

	public void renameRelationship(SQLRelationship oldFK, SQLRelationship newFK) {
        println("<comment>Renaming foreign key " + getName(oldFK) + " to " + getName(newFK) + "</comment>");
        dropRelationship(oldFK);
		addRelationship(newFK);
	}

    /**
     * Adds a statement for creating the given foreign key relationship in
     * the target database.  Depends on the {@link #getDeferrabilityClause(SQLRelationship)}
     * method for the target database's way of describing the deferrability policy.
     */
	public void addRelationship(SQLRelationship r) {
	    StringBuilder sql = new StringBuilder();
	    StringBuilder errorMsg = new StringBuilder();

	    StringBuilder typesMismatchMsg = new StringBuilder();

	    sql.append("<addForeignKeyConstraint baseTableName=\"");
		sql.append(getTableQualifier(r.getFkTable(), "baseTableName", "baseSchemaName") );
		sql.append("\" constraintName=\"");
		sql.append(getName(r));
		sql.append("\" baseColumnNames=\"");
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
		sql.append("\"");

        sql.append(" referencedTableName=\"");
		sql.append(toQualifiedName(r.getPkTable()));
		sql.append("\" referencedColumnNames=\"");
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

		sql.append("\"");

		// adds to error msg if there were types mismatch
		if (typesMismatchMsg.length() != 0) {
		    errorMsg.append("Warning: Column types mismatch in the following column mapping(s):\n");
		    errorMsg.append(typesMismatchMsg.toString());
		}

		if (!"NO ACTION".equals(getDeleteActionClause(r))) {
			sql.append(" onDelete=\"");
			sql.append(getDeleteActionClause(r));
			sql.append("\"");
		}
		if (!"NO ACTION".equals(getUpdateActionClause(r))) {
			sql.append(" onUpdate=\"");
			sql.append(getUpdateActionClause(r));
			sql.append("\"");
		}

		if (isDeferrable(r)) {
			sql.append(" deferrable=\"");
			sql.append(isDeferrable(r));
			sql.append("\"");

			sql.append(" initiallyDeferred=\"");
			sql.append(isInitiallyDeferred(r));
			sql.append("\"");
		}

		sql.append("/>");
        println(sql.toString());

		endStatement(DDLStatement.StatementType.XMLTAG, r);
	}

    /**
     * Returns true if this DDL generator supports the given relationship's
     * delete action.
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
        return getUpdateDeleteRule(r.getDeleteRule());
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
        return getUpdateDeleteRule(r.getUpdateRule());
    }

    /**
     * Returns true if the constraint is marked as deferrable.
     *
     * @param r The relationship the deferrability clause is for
     * @return true if deferribility is either INITIALLY_DEFERRED or INITIALLY_IMMEDIATE
     * in r.
     */
    public boolean isDeferrable(SQLRelationship r) {
		return r.getDeferrability() == Deferrability.INITIALLY_DEFERRED || r.getDeferrability() == Deferrability.INITIALLY_IMMEDIATE;
    }

    public boolean isInitiallyDeferred(SQLRelationship r) {
		return r.getDeferrability() == Deferrability.INITIALLY_DEFERRED;
    }

    public void addComment(SQLObject o) {
		if (o instanceof SQLColumn) {
			modifyColumnComment((SQLColumn)o);
		}
    }

    public void modifyComment(SQLObject o) {
		if (o instanceof SQLColumn) {
			modifyColumnComment((SQLColumn)o);
		}
    }

    public void addComment(SQLColumn c) {
		modifyColumnComment(c);
    }

	private void modifyColumnComment(SQLColumn c) {
		SQLTable t = c.getParent();
		print("<modifyColumn ");
		print(getTableQualifier(t));
		println(">");
		print("  <column name=\"");
		print(getName(c));
		print("\" remarks=");
		print(getQuotedRemarks(c.getRemarks()));
		println("/>");
		println("</modifyColumn>");
		endStatement(DDLStatement.StatementType.XMLTAG, c);
	}

	public void addComment(SQLTable t, boolean includeColumns) {
  		println("<!-- The following comment should be added to the table " + StringEscapeUtils.escapeXml(getName(t)) + "-->");
        println("<!-- " + StringEscapeUtils.escapeXml(t.getRemarks()) + "-->");
		if (includeColumns) {
            addColumnComments(t);
        }
	}

	/**
	 * Creates a <addColumn> definition tag
	 */
	public void addColumn(SQLColumn c) {
		print("<addColumn ");
		print(getTableQualifier(c.getParent()));
		println(">");
		println(columnDefinition("  ", c));
		println("</addColumn>");
		if (c.isAutoIncrementSequenceNameSet() && c.isAutoIncrement()) {
			print("<createSequence sequenceName=\"");
			print(c.getAutoIncrementSequenceName());
			println("\"/>");
		}
		endStatement(DDLStatement.StatementType.XMLTAG, c);
	}

    public void renameColumn(SQLColumn oldCol, SQLColumn newCol) {
        print("<renameColumn ");
        print(getTableQualifier(oldCol.getParent()));
        print(" oldColumnName=\"");
        print(getName(oldCol));
        print("\" newColumnName=\"");
        print(getName(newCol));
        println("\"/>");
        endStatement(DDLStatement.StatementType.XMLTAG, newCol);
    }


	/**
	 * Creates a <dropColumn> definition tag
	 */
	public void dropColumn(SQLColumn c) {
		print("<dropColumn ");
		print(getTableQualifier(c.getParent()));
		print(" columnName=\"");
		print(getName(c));
		println("\"/>");

		if (c.isAutoIncrement() && c.isAutoIncrementSequenceNameSet()) {
			print("<dropSequence sequenceName=\"");
			print(c.getAutoIncrementSequenceName());
			println("\"/>");
		}
		endStatement(DDLStatement.StatementType.XMLTAG, c);
	}

	/**
	 * Creates a <modifyColumn> definition tag
	 */
	public void modifyColumn(SQLColumn c) {
		SQLTable t = c.getParent();
		print("<modifyColumn ");
		print(getTableQualifier(t));
		println(">");
		println(columnDefinition("  ", c));
		println("</modifyColumn>");
		endStatement(DDLStatement.StatementType.XMLTAG, c);
	}

	/**
	 * Creates a <dropTable> definition tag
	 */
	public void dropTable(SQLTable t) {
		print("<dropTable ");
		print(getTableQualifier(t));
		println("/>");

		try {
			for (SQLColumn col : t.getColumns()) {
				if (col.isAutoIncrement() && col.isAutoIncrementSequenceNameSet()) {
					print("<dropSequence sequenceName=\"");
					print(col.getAutoIncrementSequenceName());
					println("\"/>");
				}
			}
		} catch (Exception e) {
			logger.error("Error when creating dropSequence", e);
		}
        endStatement(DDLStatement.StatementType.XMLTAG, t);
    }

	/**
	 * Creates a <column> definition tag
	 */
	protected String columnDefinition(String indent, SQLColumn c) {
        StringBuilder def = new StringBuilder(50);

		def.append(indent);
		def.append("<column name=\"");
        def.append(getName(c));

		GenericTypeDescriptor type = getTypeDescriptor(c);
        def.append("\" type=\"");
        def.append(columnType(c));
		def.append("\"");

		if (StringUtils.isNotBlank(c.getRemarks())) {
			def.append(" remarks=");
			def.append(getQuotedRemarks(c.getRemarks()));
		}

        if (StringUtils.isNotBlank(c.getDefaultValue())) {
			if (isNumericType(type)) {
				def.append(" defaultValueNumeric=\"");
				def.append(c.getDefaultValue());
				def.append("\"");
			}
			else if (isDateType(type)) {
				def.append(" defaultValueDate=\"");
				def.append(c.getDefaultValue());
				def.append("\"");
			}
			else if (type.getDataType() == Types.BOOLEAN) {
				def.append(" defaultValueBoolean=\"");
				def.append(c.getDefaultValue());
				def.append("\"");
			} else {
				def.append(" defaultValue=\"");
				def.append(c.getDefaultValue());
				def.append("\"");
			}
		}

		if (c.isAutoIncrement()) {
			def.append(" autoIncrement=\"true\"");
		}

		if (!c.isDefinitelyNullable()) {
			def.append(">");
			def.append(EOL);
			def.append(indent);
			def.append(indent);
			def.append("<constraints nullable=\"false\"/>");
			def.append(EOL);
			def.append(indent);
			def.append("</column>");
		} else {
			def.append("/>");
		}
        return def.toString();
    }

	private boolean isDateType(GenericTypeDescriptor td)	{
		int type = td.getDataType();
		return (type == Types.DATE || type == Types.TIMESTAMP);
	}

	private boolean isNumericType(GenericTypeDescriptor td)	{
		int type = td.getDataType();
		return (type == Types.BIGINT ||
		        type == Types.INTEGER ||
		        type == Types.DECIMAL ||
		        type == Types.DOUBLE ||
		        type == Types.FLOAT ||
		        type == Types.NUMERIC ||
		        type == Types.REAL ||
		        type == Types.SMALLINT ||
		        type == Types.TINYINT);
	}

	/** Columnn type */
    public String columnType(SQLColumn c) {
		return getColumnDataTypeName(c);
    }

    /** Columnn type */
    public String getColumnDataTypeName(SQLColumn c) {
        StringBuilder def = new StringBuilder();
        GenericTypeDescriptor td = getTypeDescriptor(c);
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

    protected GenericTypeDescriptor getTypeDescriptor(SQLColumn c) {
		return super.failsafeGetTypeDescriptor(c);
    }

	private String getQuotedRemarks(String remarks) {
		return "\"" + StringEscapeUtils.escapeXml(remarks) + "\"";
	}

    public void renameTable(SQLTable oldTable, SQLTable newTable) {
        print("<renameTable ");
        print(getTableQualifier(oldTable, "oldTableName", "schemaName"));
        print(" newTableName=\"");
        print(newTable.getPhysicalName());
        println("\"/>");
        endStatement(DDLStatement.StatementType.XMLTAG, newTable);
    }


	public void addTable(SQLTable t) throws SQLException, SQLObjectException {
		Set<SQLColumn> sequenceColumns = new HashSet<SQLColumn>(t.getChildCount());
		print("<createTable ");
		print( getTableQualifier(t) );
		if (StringUtils.isNotBlank(t.getRemarks())) {
			print(" remarks=");
			print(getQuotedRemarks(t.getRemarks()));
		}
		println(">");
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();
			println(columnDefinition("  ", c));
			if (c.isAutoIncrementSequenceNameSet() && c.isAutoIncrement()) {
				sequenceColumns.add(c);
			}
		}
		println("</createTable>");

		SQLIndex pk = t.getPrimaryKeyIndex();
		if (pk.getChildCount() > 0) {
		    writePKConstraintClause(pk);
		}

		for (SQLColumn col : sequenceColumns) {
			print("<createSequence sequenceName=\"");
			print(col.getAutoIncrementSequenceName());
			println("\"/>");
		}

		endStatement(DDLStatement.StatementType.XMLTAG, t);
	}

    /**
     * Writes out the primary key constraint clause for the given primary key
     * index.
     * <p>
     * Side effect: the PK object's name will be initialized if necessary.
     *
     * @param pk  The primary key index
     * @throws SQLObjectException
     *             If the pk object wasn't already populated and the populate
     *             attempt fails.
     */
	protected void writePKConstraintClause(SQLIndex pk) throws SQLObjectException {
	    if (!pk.isPrimaryKeyIndex()) {
	        throw new IllegalArgumentException("The given index is not a primary key");
	    }
		print("<addPrimaryKey ");
		print(getTableQualifier(pk.getParent()));
		print(" constraintName=\"");
	    print(getName(pk));
	    print("\" columnNames=\"");

	    boolean firstCol = true;
	    for (SQLIndex.Column col : pk.getChildren(SQLIndex.Column.class)) {
	        if (!firstCol) print(", ");
	        if (col.getColumn() == null) {
	            throw new IllegalStateException("Index column is not associated with the real column in the table.");
	        } else {
	            print(getName(col));
	        }
	        firstCol = false;
	    }
	    println("\"/>");
	}

	protected void writePrimaryKey(SQLTable t) throws SQLObjectException {
	    writePKConstraintClause(t.getPrimaryKeyIndex());
	    endStatement(DDLStatement.StatementType.XMLTAG, t);
	}

	/**
	 * Always returns false.
	 * @return false
	 */
	public boolean getAllowConnection()  {
		return false;
	}

	/**
	 * Ignores setting of this attribute as connection do not make sense for Liquibase
	 *
	 * @param argAllowConnection Value to assign to this.allowConnection
	 */
	public void setAllowConnection(boolean argAllowConnection) {
	}

	public String getCatalogTerm() {
		return null;
	}

	public String getSchemaTerm() {
		return null;
	}

    /**
     * Generates a standard <code>DROP TABLE $tablename</code> command.  Should work on most platforms.
     */
    public String makeDropTableSQL(String table) {
        return "<dropTable " + getTableQualifier(table) + "/>";
    }

    /**
     * Generates a <dropForeignKeyConstraint> tag
     */
    public String makeDropForeignKeySQL(String fkTable, String fkName) {
		return "<dropForeignKeyConstraint "
		  + getTableQualifier(fkTable, "baseTableName", "baseTableSchemaName")
		  + " constraintName=\""
          + fkName
		  + "\"/>";
    }

	public void dropPrimaryKey(SQLTable t) throws SQLObjectException {
	    SQLIndex pk = t.getPrimaryKeyIndex();
	    println("<dropPrimaryKey " + getTableQualifier(t)
		  + "\" constraintName=\"" + getName(pk) + "\"/>");
		endStatement(DDLStatement.StatementType.XMLTAG, t);
	}

	public void addPrimaryKey(SQLTable t) throws SQLObjectException {
		StringBuilder sql = new StringBuilder(100);
		boolean first = true;
		sql.append("<addPrimaryKey " + getTableQualifier(t));
		sql.append(" constraintName=\"" + t.getPrimaryKeyIndex() + "\"");
		sql.append(" columnNames=\"");
		for (SQLColumn c : t.getColumns()) {
			if (c.isPrimaryKey()) {
				if (!first) {
					sql.append(",");
				}else{
					first =false;
				}

				sql.append(getName(c));
			}
		}
		sql.append("\"/>");
		println(sql.toString());
		endStatement(DDLStatement.StatementType.XMLTAG,t);
	}

	@Override
	public void renameIndex(SQLIndex oldIndex, SQLIndex newIndex) throws SQLObjectException {
		// renaming an index is currently not supported in Liquibase 1.8
		dropIndex(oldIndex);
		addIndex(newIndex);
	}

	public void dropIndex(SQLIndex index) throws SQLObjectException {
		print("<dropIndex indexName=\"");
		print(getName(index));
		print("\" tableName=\"");
		print(getName(index.getParent()));
		println("\"/>");
		endStatement(DDLStatement.StatementType.XMLTAG, index);
	}

    /**
     * Adds the necessary <createIndex> tag.
     *
     * @param index The specification of the index to create.
     */
    public void addIndex(SQLIndex index) throws SQLObjectException {
        print("<createIndex ");
		print(getTableQualifier(index.getParent()));
		print(" indexName=\"");
		print(getName(index));
		print("\"");
        if (index.isUnique()) {
            print(" unique=\"true\"");
        }
		println(">");

        for (SQLIndex.Column c : (List<SQLIndex.Column>) index.getChildren()) {

			// ASC/DESC is currently not supported with Liqubase (1.8)
//            print(c.getAscendingOrDescending() == AscendDescend.ASCENDING ? " ASC" : "");
//            print(c.getAscendingOrDescending() == AscendDescend.DESCENDING ? " DESC" : "");
            println("  <column name=\"" + getName(c) + "\"/>") ;
        }
        println("</createIndex>");
        endStatement(DDLStatement.StatementType.XMLTAG, index);
    }

    private String getTableQualifier(SQLObject o) {
        return getTableQualifier(o, "tableName", "schemaName");
    }

    private String getTableQualifier(String tablename) {
        return getTableQualifier(tablename, "tableName", "schemaName");
    }

    private String getTableQualifier(SQLObject o, String tableNameAttr, String schemaNameAttr) {
        return getTableQualifier(getName(o), tableNameAttr, schemaNameAttr);
    }

    private String getTableQualifier(String tableName, String tableNameAttr, String schemaNameAttr) {
        StringBuilder result = new StringBuilder(50);
        result.append(tableNameAttr);
        result.append("=\"");
        result.append(tableName);
        result.append("\"");
        String schema = getTargetSchema();
        if (StringUtils.isNotBlank(schema)) {
            result.append(" ");
            result.append(schemaNameAttr);
            result.append("=\"");
            result.append(schema);
            result.append("\"");
        }
        return result.toString();
    }
}
