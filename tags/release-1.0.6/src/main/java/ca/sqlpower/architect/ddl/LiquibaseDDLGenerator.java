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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ddl.DDLStatement.StatementType;
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

public class LiquibaseDDLGenerator extends GenericDDLGenerator implements DDLGenerator {

	public static final String GENERATOR_VERSION = "$Revision: 3271 $";

	private static final Logger logger = Logger.getLogger(LiquibaseDDLGenerator.class);

	private boolean separateChangeSets;
	private String author;
	private boolean generateId;
	private int currentId = 1;
	private boolean useAddPKSingleColumn = false;

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

	public void applySettings(LiquibaseSettings options) {
		setUseSeparateChangeSets(options.getUseSeparateChangeSets());
		setAuthor(options.getAuthor());
		setGenerateId(options.getGenerateId());
		setIdStart(options.getIdStart());
		setUseAddPKTagForSingleColumns(options.getUseAddPKTagForSingleColumns());
	}

	/**
	 * Controls usage of the the &lt;addPrimaryKey&gt; tag for single column primary keys.
	 * <br/>
	 * If set to false, it will only be used for multi-column primary keys. For single column primary keys 
	 * a nested &lt;constraint&gt; tag with a "primaryKey" attribute will be used instead.
	 * <br/>
	 * &lt;addPrimaryKey&gt; does not work when used with MySQL and autoincrement columns, so this feature
	 * is essentially a workaround for a MySQL defiency
	 *
	 * @param flag if false, addPrimaryKey will only be used for multi-column primary keys
	 */
	public void setUseAddPKTagForSingleColumns(boolean flag) {
		useAddPKSingleColumn = flag;
	}
	
	/**
	 * Control the usage of changeSets in the generated output.
	 * <br/>
	 * If true, each statement will be "enclosed" in a changeSet
	 * If false, no changeSet tags will be generated
	 * <br/>
	 * If changeSet Generation is enabled, the author and id to be used
	 * can be controlled through setAuthor() and setGenerateId()
	 *
	 * @param flag true, changeSets will be generated
	 * @see #setAuthor(java.lang.String)
	 * @see #setGenerateId(boolean)
	 */
	public void setUseSeparateChangeSets(boolean flag) {
		separateChangeSets = flag;
	}

	/**
	 * Define the autor name to be used in the changeSet tag.
	 * <br/>
	 * This is only relevant if setUseSeparateChangeSets() has been activated
	 *
	 * @param name the author to be used
	 * @see #setUseSeparateChangeSets(boolean)
	 * @see #setGenerateId(boolean)
	 */
	public void setAuthor(String name) {
		author = name;
	}

	/**
	 * Define the start value for the ID attribute of the changeSets.
	 * <br/>
	 * This is only used if changeSet generation has been enabled.
	 *
	 * @param startValue the value for the first changeSet that is generated
	 * @see #setUseSeparateChangeSets(boolean)
	 * @see #setGenerateId(flag)
	 */
	public void setIdStart(int startValue) {
		currentId = startValue;
	}

	/**
	 * Controls if the id attribute should be a generated (numeric) value
	 * for each changeset
	 *
	 * @param flag turns the id generation on or off
	 * @see #setIdStart(int)
	 * @see #setUseSeparateChangeSets(boolean)
	 */
	public void setGenerateId(boolean flag) {
		generateId = flag;
	}

	protected void startOfStatement() {
		if (separateChangeSets) {
			writeOpenChangeSet();
		}
	}

	protected void writeOpenChangeSet() {
		String tag = getChangeSetStartTag();
		println(tag);
	}

	protected String getChangeSetStartTag() {
		StringBuilder tag = new StringBuilder(100);
		tag.append("<changeSet");
		if (StringUtils.isBlank(author)) {
			tag.append(" author=\"CHANGEME\"");
		} else {
			tag.append(" author=");
			tag.append(escapeAttributeValue(author));
		}

		if (generateId) {
			tag.append(" id=\"");
			tag.append(Integer.toString(currentId));
			tag.append("\"");
			currentId++;
		} else {
			tag.append(" id=\"CHANGEME\"");
		}
		tag.append(">");
		return tag.toString();
	}

	protected void endOfStatement() {
		if (separateChangeSets) {
			println("</changeSet>");
		}
	}

	public void writeDDLTransactionBegin() {
	}

	public void writeDDLTransactionEnd() {
	}

	public void writeCreateDB(SQLDatabase db) {
		// not necessary for Liquibase
	}

	@Override
	public List<DDLStatement> getDdlStatements() {
		List<DDLStatement> result = super.getDdlStatements();
		if (!separateChangeSets) {
			DDLStatement startTag = new DDLStatement((SQLObject)null, StatementType.XMLTAG, getChangeSetStartTag() + EOL, "", null, null);
			result.add(0, startTag);
			DDLStatement endTag = new DDLStatement((SQLObject)null, StatementType.XMLTAG, "</changeSet>", "", null, null);
			result.add(endTag);
		}
		return result;
	}

	public void dropRelationship(SQLRelationship r) {
		startOfStatement();
		print("<dropForeignKeyConstraint ");
		print(getTableQualifier(r.getFkTable(), "baseTableName", "baseSchemaName"));
		print(" constraintName=\"");
		print(getName(r));
		println("\"/>");
		endOfStatement();
		endStatement(StatementType.XMLTAG, r);
	}

	private String getName(SQLObject o) {
		if (StringUtils.isEmpty(o.getPhysicalName())) {
			return o.getName();
		}
		return o.getPhysicalName();
	}

	public void renameRelationship(SQLRelationship oldFK, SQLRelationship newFK) {
		startOfStatement();
        println("<comment>Renaming foreign key " + getName(oldFK) + " to " + getName(newFK) + "</comment>");
        dropRelationship(oldFK);
		addRelationship(newFK);
		endOfStatement();
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

	    sql.append("<addForeignKeyConstraint ");
		sql.append(getTableQualifier(r.getFkTable(), "baseTableName", "baseSchemaName") );
		sql.append(" constraintName=\"");
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
		startOfStatement();
        println(sql.toString());
		endOfStatement();
		endStatement(StatementType.XMLTAG, r);
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
		startOfStatement();
		print("<modifyColumn ");
		print(getTableQualifier(t));
		println(">");
		print("  <column name=\"");
		print(getName(c));
		print("\" remarks=");
		print(escapeAttributeValue(c.getRemarks()));
		println("/>");
		println("</modifyColumn>");
		endOfStatement();
		endStatement(StatementType.XMLTAG, c);
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
		startOfStatement();
		print("<addColumn ");
		print(getTableQualifier(c.getParent()));
		println(">");
		println(columnDefinition("  ", c, false));
		println("</addColumn>");
		if (c.isAutoIncrementSequenceNameSet() && c.isAutoIncrement()) {
			print("<createSequence sequenceName=\"");
			print(c.getAutoIncrementSequenceName());
			println("\"/>");
		}
		endOfStatement();
		endStatement(StatementType.XMLTAG, c);
	}

    public void renameColumn(SQLColumn oldCol, SQLColumn newCol) {
		startOfStatement();
        print("<renameColumn ");
        print(getTableQualifier(oldCol.getParent()));
        print(" oldColumnName=\"");
        print(getName(oldCol));
        print("\" newColumnName=\"");
        print(getName(newCol));
        println("\"/>");
		endOfStatement();
        endStatement(StatementType.XMLTAG, newCol);
    }


	/**
	 * Creates a <dropColumn> definition tag
	 */
	public void dropColumn(SQLColumn c) {
		startOfStatement();
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
		endOfStatement();
		endStatement(StatementType.XMLTAG, c);
	}

	/**
	 * Creates a <modifyColumn> definition tag
	 */
	public void modifyColumn(SQLColumn c) {
		startOfStatement();
		SQLTable t = c.getParent();
		print("<modifyColumn ");
		print(getTableQualifier(t));
		println(">");
		println(columnDefinition("  ", c, false));
		println("</modifyColumn>");
		endOfStatement();
		endStatement(StatementType.XMLTAG, c);
	}

	/**
	 * Creates a <dropTable> definition tag
	 */
	public void dropTable(SQLTable t) {
		startOfStatement();
		print("<dropTable ");
		print(getTableQualifier(t));
		println("/>");
		endOfStatement();

		try {
			for (SQLColumn col : t.getColumns()) {
				if (col.isAutoIncrement() && col.isAutoIncrementSequenceNameSet()) {
					startOfStatement();
					print("<dropSequence sequenceName=\"");
					print(col.getAutoIncrementSequenceName());
					println("\"/>");
					endOfStatement();
				}
			}
		} catch (Exception e) {
			logger.error("Error when creating dropSequence", e);
		}
        endStatement(StatementType.XMLTAG, t);
    }

	/**
	 * Creates a <column> definition tag
	 */
	protected String columnDefinition(String indent, SQLColumn c, boolean tableHasSingleColumnPK) {
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
			def.append(escapeAttributeValue(c.getRemarks()));
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

		boolean needsConstraint = !c.isDefinitelyNullable() || tableHasSingleColumnPK && c.isPrimaryKey() && !useAddPKSingleColumn;

		if (needsConstraint) {
			def.append(">");
			def.append(EOL);
			def.append(indent);
			def.append(indent);
			def.append("<constraints");
			if (tableHasSingleColumnPK && c.isPrimaryKey() && !useAddPKSingleColumn) {
				def.append(" primaryKey=\"true\"");
				SQLTable tbl = c.getParent();
				try {
					SQLIndex pk = tbl.getPrimaryKeyIndex();
					def.append(" constraintName=\"");
					def.append(getName(pk));
					def.append("\"");
				} catch (Exception e) {
					logger.error("Could not obtain PK index", e);
				}
			}
			if (!c.isDefinitelyNullable()) {
				def.append(" nullable=\"false\"");
			}
			def.append("/>");
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

	/**
	 * Returns the passed string usable as the value for a tag attribute.
	 * <br/>
	 * The returned value is enclosed in double quotes and escapes necessary characters
	 *
	 * @param value
	 * @return an escaped and quoted version of the passed value
	 */
	private String escapeAttributeValue(String value) {
		return "\"" + StringEscapeUtils.escapeXml(value) + "\"";
	}

    public void renameTable(SQLTable oldTable, SQLTable newTable) {
		startOfStatement();
        print("<renameTable ");
        print(getTableQualifier(oldTable, "oldTableName", "schemaName"));
        print(" newTableName=\"");
        print(newTable.getPhysicalName());
        println("\"/>");
		endOfStatement();
        endStatement(StatementType.XMLTAG, newTable);
    }


	public void addTable(SQLTable t) throws SQLException, SQLObjectException {
		Set<SQLColumn> sequenceColumns = new HashSet<SQLColumn>(t.getChildCount());
		startOfStatement();
		SQLIndex pk = t.getPrimaryKeyIndex();

		boolean singleColumnPK = pk.getChildren(SQLIndex.Column.class).size() == 1;

		print("<createTable ");
		print( getTableQualifier(t) );
		if (StringUtils.isNotBlank(t.getRemarks())) {
			print(" remarks=");
			print(escapeAttributeValue(t.getRemarks()));
		}
		println(">");
		Iterator<SQLColumn> it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();
			println(columnDefinition("  ", c, singleColumnPK));
			if (c.isAutoIncrementSequenceNameSet() && c.isAutoIncrement()) {
				sequenceColumns.add(c);
			}
		}
		println("</createTable>");
		endOfStatement();

		if (pk.getChildCount() > 0 && singleColumnPK && useAddPKSingleColumn) {
		    writePKConstraintClause(pk);
		}

		for (SQLColumn col : sequenceColumns) {
			startOfStatement();
			print("<createSequence sequenceName=\"");
			print(col.getAutoIncrementSequenceName());
			println("\"/>");
			endOfStatement();
		}

		endStatement(StatementType.XMLTAG, t);
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
		startOfStatement();
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
		endOfStatement();
	}

	protected void writePrimaryKey(SQLTable t) throws SQLObjectException {
	    writePKConstraintClause(t.getPrimaryKeyIndex());
	    endStatement(StatementType.XMLTAG, t);
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
		startOfStatement();
	    println("<dropPrimaryKey " + getTableQualifier(t)
		  + " constraintName=\"" + getName(pk) + "\"/>");
		endOfStatement();
		endStatement(StatementType.XMLTAG, t);
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
		startOfStatement();
		println(sql.toString());
		endOfStatement();
		endStatement(StatementType.XMLTAG,t);
	}

	@Override
	public void renameIndex(SQLIndex oldIndex, SQLIndex newIndex) throws SQLObjectException {
		// renaming an index is currently not supported in Liquibase 1.9
		dropIndex(oldIndex);
		addIndex(newIndex);
	}

	public void dropIndex(SQLIndex index) throws SQLObjectException {
		startOfStatement();
		print("<dropIndex indexName=\"");
		print(getName(index));
		print("\" tableName=\"");
		print(getName(index.getParent()));
		println("\"/>");
		endOfStatement();
		endStatement(StatementType.XMLTAG, index);
	}

    /**
     * Adds the necessary <createIndex> tag.
     *
     * @param index The specification of the index to create.
     */
    public void addIndex(SQLIndex index) throws SQLObjectException {
		startOfStatement();
        print("<createIndex ");
		print(getTableQualifier(index.getParent()));
		print(" indexName=\"");
		print(getName(index));
		print("\"");
        if (index.isUnique()) {
            print(" unique=\"true\"");
        }
		println(">");
        for (SQLIndex.Column c : index.getChildren(SQLIndex.Column.class)) {

			// ASC/DESC is currently not supported with Liqubase (1.8)
//            print(c.getAscendingOrDescending() == AscendDescend.ASCENDING ? " ASC" : "");
//            print(c.getAscendingOrDescending() == AscendDescend.DESCENDING ? " DESC" : "");
            println("  <column name=\"" + getName(c) + "\"/>") ;
        }
        println("</createIndex>");
		endOfStatement();
        endStatement(StatementType.XMLTAG, index);
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
    
    @Override
    public boolean supportsCheckConstraint() {
        return false;
    }
}
