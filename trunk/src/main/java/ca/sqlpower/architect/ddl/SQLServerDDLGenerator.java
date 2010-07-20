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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLStatement.StatementType;
import ca.sqlpower.object.SPResolverRegistry;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.sqlobject.SQLCheckConstraint;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLEnumeration;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.sqlobject.SQLTypePhysicalProperties.SQLTypeConstraint;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

/**
 * The base class for version-specific SQL Server DDL generators. This class is
 * marked abstract because it is not appropriate to use it directly on any
 * version of SQL Server.
 */
public abstract class SQLServerDDLGenerator extends GenericDDLGenerator {
	public SQLServerDDLGenerator() throws SQLException {
		super();
	}

	public static final String GENERATOR_VERSION = "$Revision$";
	private static final Logger logger = Logger.getLogger(SQLServerDDLGenerator.class);
	private final String REGEX_CRLF = "(\r\n|\n\r|\r|\n)";

    /**
     * These words are the words reserved by SQL Server and cannot be used as a
     * name for tables, columns, relationships, indices, etc.
     */
	public static final HashSet<String> RESERVED_WORDS;

	static {
		RESERVED_WORDS = new HashSet<String>();
		RESERVED_WORDS.add("ADD");
		RESERVED_WORDS.add("ALL");
		RESERVED_WORDS.add("ALTER");
		RESERVED_WORDS.add("AND");
		RESERVED_WORDS.add("ANY");
		RESERVED_WORDS.add("AS");
		RESERVED_WORDS.add("ASC");
		RESERVED_WORDS.add("AUTHORIZATION");
		RESERVED_WORDS.add("BACKUP");
		RESERVED_WORDS.add("BEGIN");
		RESERVED_WORDS.add("BETWEEN");
		RESERVED_WORDS.add("BREAK");
		RESERVED_WORDS.add("BROWSE");
		RESERVED_WORDS.add("BULK");
		RESERVED_WORDS.add("BY");
		RESERVED_WORDS.add("CASCADE");
		RESERVED_WORDS.add("CASE");
		RESERVED_WORDS.add("CHECK");
		RESERVED_WORDS.add("CHECKPOINT");
		RESERVED_WORDS.add("CLOSE");
		RESERVED_WORDS.add("CLUSTERED");
		RESERVED_WORDS.add("COALESCE");
		RESERVED_WORDS.add("COLLATE");
		RESERVED_WORDS.add("COLUMN");
		RESERVED_WORDS.add("COMMIT");
		RESERVED_WORDS.add("COMPUTE");
		RESERVED_WORDS.add("CONSTRAINT");
		RESERVED_WORDS.add("CONTAINS");
		RESERVED_WORDS.add("CONTAINSTABLE");
		RESERVED_WORDS.add("CONTINUE");
		RESERVED_WORDS.add("CONVERT");
		RESERVED_WORDS.add("CREATE");
		RESERVED_WORDS.add("CROSS");
		RESERVED_WORDS.add("CURRENT");
		RESERVED_WORDS.add("CURRENT_DATE");
		RESERVED_WORDS.add("CURRENT_TIME");
		RESERVED_WORDS.add("CURRENT_TIMESTAMP");
		RESERVED_WORDS.add("CURRENT_USER");
		RESERVED_WORDS.add("CURSOR");
		RESERVED_WORDS.add("DATABASE");
		RESERVED_WORDS.add("DBCC");
		RESERVED_WORDS.add("DEALLOCATE");
		RESERVED_WORDS.add("DECLARE");
		RESERVED_WORDS.add("DEFAULT");
		RESERVED_WORDS.add("DELETE");
		RESERVED_WORDS.add("DENY");
		RESERVED_WORDS.add("DESC");
		RESERVED_WORDS.add("DISK");
		RESERVED_WORDS.add("DISTINCT");
		RESERVED_WORDS.add("DISTRIBUTED");
		RESERVED_WORDS.add("DOUBLE");
		RESERVED_WORDS.add("DROP");
		RESERVED_WORDS.add("DUMMY");
		RESERVED_WORDS.add("DUMP");
		RESERVED_WORDS.add("ELSE");
		RESERVED_WORDS.add("END");
		RESERVED_WORDS.add("ERRLVL");
		RESERVED_WORDS.add("ESCAPE");
		RESERVED_WORDS.add("EXCEPT");
		RESERVED_WORDS.add("EXEC");
		RESERVED_WORDS.add("EXECUTE");
		RESERVED_WORDS.add("EXISTS");
		RESERVED_WORDS.add("EXIT");
		RESERVED_WORDS.add("FETCH");
		RESERVED_WORDS.add("FILE");
		RESERVED_WORDS.add("FILLFACTOR");
		RESERVED_WORDS.add("FOR");
		RESERVED_WORDS.add("FOREIGN");
		RESERVED_WORDS.add("FREETEXT");
		RESERVED_WORDS.add("FREETEXTTABLE");
		RESERVED_WORDS.add("FROM");
		RESERVED_WORDS.add("FULL");
		RESERVED_WORDS.add("FUNCTION");
		RESERVED_WORDS.add("GOTO");
		RESERVED_WORDS.add("GRANT");
		RESERVED_WORDS.add("GROUP");
		RESERVED_WORDS.add("HAVING");
		RESERVED_WORDS.add("HOLDLOCK");
		RESERVED_WORDS.add("IDENTITY");
		RESERVED_WORDS.add("IDENTITY_INSERT");
		RESERVED_WORDS.add("IDENTITYCOL");
		RESERVED_WORDS.add("IF");
		RESERVED_WORDS.add("IN");
		RESERVED_WORDS.add("INDEX");
		RESERVED_WORDS.add("INNER");
		RESERVED_WORDS.add("INSERT");
		RESERVED_WORDS.add("INTERSECT");
		RESERVED_WORDS.add("INTO");
		RESERVED_WORDS.add("IS");
		RESERVED_WORDS.add("JOIN");
		RESERVED_WORDS.add("KEY");
		RESERVED_WORDS.add("KILL");
		RESERVED_WORDS.add("LEFT");
		RESERVED_WORDS.add("LIKE");
		RESERVED_WORDS.add("LINENO");
		RESERVED_WORDS.add("LOAD");
		RESERVED_WORDS.add("NATIONAL");
		RESERVED_WORDS.add("NOCHECK");
		RESERVED_WORDS.add("NONCLUSTERED");
		RESERVED_WORDS.add("NOT");
		RESERVED_WORDS.add("NULL");
		RESERVED_WORDS.add("NULLIF");
		RESERVED_WORDS.add("OF");
		RESERVED_WORDS.add("OFF");
		RESERVED_WORDS.add("OFFSETS");
		RESERVED_WORDS.add("ON");
		RESERVED_WORDS.add("OPEN");
		RESERVED_WORDS.add("OPENDATASOURCE");
		RESERVED_WORDS.add("OPENQUERY");
		RESERVED_WORDS.add("OPENROWSET");
		RESERVED_WORDS.add("OPENXML");
		RESERVED_WORDS.add("OPTION");
		RESERVED_WORDS.add("OR");
		RESERVED_WORDS.add("ORDER");
		RESERVED_WORDS.add("OUTER");
		RESERVED_WORDS.add("OVER");
		RESERVED_WORDS.add("PERCENT");
		RESERVED_WORDS.add("PLAN");
		RESERVED_WORDS.add("PRECISION");
		RESERVED_WORDS.add("PRIMARY");
		RESERVED_WORDS.add("PRINT");
		RESERVED_WORDS.add("PROC");
		RESERVED_WORDS.add("PROCEDURE");
		RESERVED_WORDS.add("PUBLIC");
		RESERVED_WORDS.add("RAISERROR");
		RESERVED_WORDS.add("READ");
		RESERVED_WORDS.add("READTEXT");
		RESERVED_WORDS.add("RECONFIGURE");
		RESERVED_WORDS.add("REFERENCES");
		RESERVED_WORDS.add("REPLICATION");
		RESERVED_WORDS.add("RESTORE");
		RESERVED_WORDS.add("RESTRICT");
		RESERVED_WORDS.add("RETURN");
		RESERVED_WORDS.add("REVOKE");
		RESERVED_WORDS.add("RIGHT");
		RESERVED_WORDS.add("ROLLBACK");
		RESERVED_WORDS.add("ROWCOUNT");
		RESERVED_WORDS.add("ROWGUIDCOL");
		RESERVED_WORDS.add("RULE");
		RESERVED_WORDS.add("SAVE");
		RESERVED_WORDS.add("SCHEMA");
		RESERVED_WORDS.add("SELECT");
		RESERVED_WORDS.add("SESSION_USER");
		RESERVED_WORDS.add("SET");
		RESERVED_WORDS.add("SETUSER");
		RESERVED_WORDS.add("SHUTDOWN");
		RESERVED_WORDS.add("SOME");
		RESERVED_WORDS.add("STATISTICS");
		RESERVED_WORDS.add("SYSTEM_USER");
		RESERVED_WORDS.add("TABLE");
		RESERVED_WORDS.add("TEXTSIZE");
		RESERVED_WORDS.add("THEN");
		RESERVED_WORDS.add("TO");
		RESERVED_WORDS.add("TOP");
		RESERVED_WORDS.add("TRAN");
		RESERVED_WORDS.add("TRANSACTION");
		RESERVED_WORDS.add("TRIGGER");
		RESERVED_WORDS.add("TRUNCATE");
		RESERVED_WORDS.add("TSEQUAL");
		RESERVED_WORDS.add("UNION");
		RESERVED_WORDS.add("UNIQUE");
		RESERVED_WORDS.add("UPDATE");
		RESERVED_WORDS.add("UPDATETEXT");
		RESERVED_WORDS.add("USE");
		RESERVED_WORDS.add("USER");
		RESERVED_WORDS.add("VALUES");
		RESERVED_WORDS.add("VARYING");
		RESERVED_WORDS.add("VIEW");
		RESERVED_WORDS.add("WAITFOR");
		RESERVED_WORDS.add("WHEN");
		RESERVED_WORDS.add("WHERE");
		RESERVED_WORDS.add("WHILE");
		RESERVED_WORDS.add("WITH");
		RESERVED_WORDS.add("WRITETEXT");
	}

	public String getName() {
	    return "Microsoft SQL Server";
	}

	public boolean isReservedWord(String word) {
		return RESERVED_WORDS.contains(word.toUpperCase());
	}

    @Override
	public void writeHeader() {
		println("-- Created by SQLPower SQLServer (all versions) DDL Generator "+GENERATOR_VERSION+" --");
	}

    @Override
	public void writeDDLTransactionBegin() {
        // nothing needs to be done for beginning a transaction
	}

	/**
	 * Prints "GO" on its own line.
	 */
    @Override
	public void writeDDLTransactionEnd() {
		println("GO");
	}

	/**
	 * Returns an empty string because SS2k doesn't need DDL statement
	 * terminators.
	 */
    @Override
	public String getStatementTerminator() {
        return "";
	}

    @Override
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap<Integer, GenericTypeDescriptor>();

		typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 2000, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("IMAGE", Types.BLOB, 2147483647, "0x", null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 3, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("TEXT", Types.CLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATETIME", Types.DATE, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("REAL", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("INT", Types.INTEGER, 10, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("IMAGE", Types.LONGVARBINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("TEXT", Types.LONGVARCHAR, 2147483647, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("DATETIME", Types.TIME, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("DATETIME", Types.TIMESTAMP, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 3, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 8000, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(SQLType.NVARCHAR), new GenericTypeDescriptor("NVARCHAR", SQLType.NVARCHAR, 4000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(SQLType.NCHAR), new GenericTypeDescriptor("NCHAR", SQLType.NCHAR, 4000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(SQLType.NCLOB), new GenericTypeDescriptor("NCLOB", SQLType.NCLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}

    /**
	 * Returns the string "Database".
	 */
    @Override
	public String getCatalogTerm() {
		return "Database";
	}

	/**
	 * Returns the string "Owner".
	 */
    @Override
	public String getSchemaTerm() {
		return "Owner";
	}

	/**
	 * Turns a logical identifier into a legal identifier (physical name) for SQL Server.
     *
     * <p>Uses a deterministic method to generate tie-breaking numbers when there is a namespace
     * conflict.  If you pass null as the physical name, it will use just the logical name when
     * trying to come up with tie-breaking hashes for identifier names.  If the first attempt
     * at generating a unique name fails, subsequent calls should pass each new illegal
     * identifier which will be used with the logical name to generate a another hash.
     *
     * <p>SQL Server 7.0 Rules:
     * <ul>
     *  <li> no spaces
     *  <li> 128 character limit
     *  <li> can only be comprised of letters, numbers, underscores
     *  <li> can't be an sql server reserved word
     *  <li> can also use "@$#_"
     * </ul>
     *
     * <p>XXX: the illegal character replacement routine does not play well with regex chars like ^ and |
	 */
	private String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_');
		logger.debug("after replace of spaces: " + ident);

		// replace anything that is not a letter, character, or underscore with an underscore...
		ident = ident.replaceAll("[^a-zA-Z0-9_@$#]", "_");

		// first time through
		if (physicalName == null) {
			// length is ok
            if (ident.length() < 129) {
				return ident;
			} else {
				// length is too big
				logger.debug("truncating identifier: " + ident);
				String base = ident.substring(0,125);
				int tiebreaker = ((ident.hashCode() % 1000) + 1000) % 1000;
				logger.debug("new identifier: " + base + tiebreaker);
				return (base + tiebreaker);
			}
		} else {
			// back for more, which means that we probably
            // had a namespace conflict.  Hack the ident down
            // to size if it's too big, and then generate
            // a hash tiebreaker using the ident and the
            // passed value physicalName
			logger.debug("physical identifier is not unique, regenerating: " + physicalName);
			String base = ident;
			if (ident.length() > 125) {
				base = ident.substring(0,125);
			}
			int tiebreaker = (((ident + physicalName).hashCode() % 1000) + 1000) % 1000;
			logger.debug("regenerated identifier is: " + (base + tiebreaker));
			return (base + tiebreaker);
		}
	}

	/**
	 * SQL Server does not really support comments on database objects. There
	 * seems to be an undocumented sp_addcomment procedure, but I have no idea
	 * how exactly it works (as it is not documented) and from within Java
	 * those comments are not accessible anyway.
	 * So addComment() simply prints out a SQL comment containing the
	 * remarks from the user.
	 * @param t
	 * @param includeColumns
	 */
	@Override
	public void addComment(SQLTable t, boolean includeColumns) {
		// SQL Server only supports comments in "extended properties" but
		// they are not read in any of the public APIs anyway so they are more
		// "write-only" comments.
		// So we only write a SQL comment with the table's comment here

		if (t.getRemarks() != null && t.getRemarks().trim().length() > 0) {
			print("\n-- Comment for table [" + t.getPhysicalName() + "]: ");
			print(t.getRemarks().replaceAll(REGEX_CRLF, "\n-- "));
			endStatement(StatementType.COMMENT, t);

			if (includeColumns) {
				addColumnComments(t);
			}
		}
	}

	@Override
    public void addComment(SQLColumn c) {
        if (c.getRemarks() == null || c.getRemarks().trim().length() == 0) return;

        print("\n-- Comment for column [");
        print(c.getName());
        print("]: ");
        print(c.getRemarks().replaceAll(REGEX_CRLF, "\n-- "));
        endStatement(StatementType.COMMENT, c);
    }

    @Override
	public String toIdentifier(String name) {
		return toIdentifier(name,null);
	}

    /**
     * Adds support for the SQL Server <code>identity</code> feature.
     */
    @Override
    public String columnType(SQLColumn c) {
        String type = super.columnType(c);
        if (c.isAutoIncrement()) {
            type += " IDENTITY";
        }
        return type;
    }

    @Override
    public void addColumn(SQLColumn c) {
        print("\nALTER TABLE ");
        print(toQualifiedName(c.getParent()));
        print(" ADD ");
        print(columnDefinition(c,new HashMap<String, SQLObject>()));
        endStatement(StatementType.CREATE, c);
    }

    @Override
    public void addIndex(SQLIndex index) throws SQLObjectException {
        if (logger.isDebugEnabled()) {
            String parentTableName = null;
            String parentFolder = null;
            if (index.getParent() != null) {
                parentTableName = index.getParent().getName();
            }
            if (index.getParent() != null) {
                parentFolder = index.getParent().getName();
            }
            logger.debug("Adding index: " + index + " (parent table " + parentTableName + ") (parentFolder " + parentFolder + ")");
        }

        createPhysicalName(topLevelNames, index);

        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }
        if(index.isClustered()) {
            print(" CLUSTERED ");
        } else {
            print(" NONCLUSTERED ");
        }
        print("INDEX ");
        print(DDLUtils.toQualifiedName(null,null,index.getName()));
        print("\n ON ");
        print(toQualifiedName(index.getParent()));
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : index.getChildren(SQLIndex.Column.class)) {
            if (!first) print(", ");
            if (c.getColumn() != null) {
                print(c.getColumn().getPhysicalName());
            } else {
                print(c.getName());
            }
            first = false;
        }
        print(" )\n");
        endStatement(StatementType.CREATE, index);
    }

    @Override
    public String getDeferrabilityClause(SQLRelationship r) {
        if (supportsDeferrabilityPolicy(r)) {
            return "";
        } else {
            throw new UnsupportedOperationException(getName() + " does not support " +
                    r.getName() + "'s deferrability policy (" + r.getDeferrability() + ").");
        }
    }

    @Override
    public boolean supportsDeferrabilityPolicy(SQLRelationship r) {
        if (!Arrays.asList(Deferrability.values()).contains(r.getDeferrability())) {
            throw new IllegalArgumentException("Unknown deferrability policy: " + r.getDeferrability());
        } else {
            return r.getDeferrability() == Deferrability.NOT_DEFERRABLE;
        }
    }

    /**
     * Overrides the syntax to omit the "constraint" keyword.
     */
    @Override
    public void dropPrimaryKey(SQLTable t) {
        SQLIndex pk = t.getPrimaryKeyIndex();
        print("\nALTER TABLE " + toQualifiedName(t.getName())
                + " DROP " + pk.getPhysicalName());
        endStatement(StatementType.DROP, t);
    }

    @Override
    public String makeDropForeignKeySQL(String fkTable, String fkName) {
        return "\nALTER TABLE "
        +toQualifiedName(fkTable)
        +" DROP CONSTRAINT "
        +fkName;
    }

	@Override
	public void renameColumn(SQLColumn oldCol, SQLColumn newCol) {
		Map<String, SQLObject> empty = new HashMap<String, SQLObject>(0);
		print("sp_rename @objname='");
		print(toQualifiedName(oldCol.getParent()));
		print(".");
		print(createPhysicalName(empty, oldCol));
		print("', @newname='");
		print(createPhysicalName(empty, newCol));
		print("', @objtype='COLUMN'");
		endStatement(StatementType.ALTER, newCol);
	}

	@Override
	public void renameIndex(SQLIndex oldIndex, SQLIndex newIndex) throws SQLObjectException {
		print("sp_rename @objname='");
		print(toQualifiedName(oldIndex));
		print("', @newname='");
		print(toQualifiedName(newIndex));
		print("', @objtype='INDEX'");
		endStatement(StatementType.ALTER, newIndex);
	}

	@Override
	public void renameRelationship(SQLRelationship oldFK, SQLRelationship newFK) {
		Map<String, SQLObject> empty = new HashMap<String, SQLObject>(0);
		print("sp_rename @objname='");
		print(createPhysicalName(empty, oldFK));
		print("', @newname='");
		print(createPhysicalName(empty, newFK));
		println("', @objtype='OBJECT'");
		endStatement(StatementType.ALTER, newFK);
	}

	@Override
	public void renameTable(SQLTable oldTable, SQLTable newTable) {
		Map<String, SQLObject> empty = new HashMap<String, SQLObject>(0);
		print("sp_rename @objname='");
		print(createPhysicalName(empty, oldTable));
		print("', @newname='");
		print(createPhysicalName(empty, newTable));
		println("'");
		endStatement(StatementType.ALTER, newTable);
	}

    /**
     * SQL Server does not allow multiple named check constraints on the column
     * level. Only one named or unnamed check constraint is allowed. Instead, we
     * must add them on the table level.
     * 
     * @see #addTable(SQLTable)
     */
	@Override
	protected String columnDefinition(SQLColumn c, Map<String, SQLObject> colNameMap) {
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

        def.append(columnNullability(c));
        
        List<SQLEnumeration> enumerations;
        SQLTypeConstraint constraintType = type.getConstraintType(getPlatformName());
        if (constraintType == null) {
            constraintType = type.getDefaultPhysicalProperties().getConstraintType();
            enumerations = type.getDefaultPhysicalProperties().getChildrenWithoutPopulating(SQLEnumeration.class);
        } else {
            enumerations = type.getEnumerations(getPlatformName());
        }
        
        // Add enumeration.
        if (constraintType == SQLTypeConstraint.ENUM) {
            String columnEnumeration = columnEnumeration(c, enumerations);
            if (columnEnumeration != null && columnEnumeration.length() > 0) {
                def.append(" " + columnEnumeration);
            }
        }

        logger.debug("column definition "+ def.toString());
        return def.toString();
	}

    /**
     * Overridden to generate add table statement to have domain/type level
     * check constraints be added on the table level. The reason why it is being
     * generated on the table level is because SQL Server does not allow
     * multiple named check constraints on a single column. Only one named or
     * unnamed check constraint per column is allowed.
     * 
     * Since check constraints from multiple objects are being combined into the
     * table level, we must ensure that there are no name conflicts by
     * prepending tags to identify which SQLObject type and physical name the
     * check constraint is actually supposed to be applied on. e.g.
     * col_<column-name>_<constraint-name> or table_<table-name>_<constraint-name>.
     * This is especially important since actual table level check constraints
     * will be added in the future.
     */
	@Override
	public void addTable(SQLTable t) throws SQLException, SQLObjectException {
	       Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();  // for detecting duplicate column names
	        // generate a new physical name if necessary
	        createPhysicalName(topLevelNames,t); // also adds generated physical name to the map
	        print("\nCREATE TABLE ");
	        print( toQualifiedName(t) );
	        println(" (");
	        boolean firstCol = true;
	        
	        List<SQLColumn> columns = t.getColumns();
	        
	        for (SQLColumn c : columns) {
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
	        
	        for (SQLColumn c : columns) {
	            UserDefinedSQLType type = c.getUserDefinedSQLType();
	            List<SQLCheckConstraint> checkConstraints;
	            SQLTypeConstraint constraintType = type.getConstraintType(getPlatformName());
	            if (constraintType == null) {
	                constraintType = type.getDefaultPhysicalProperties().getConstraintType();
	                checkConstraints = type.getDefaultPhysicalProperties().getCheckConstraints();
	            } else {
	                checkConstraints = type.getCheckConstraints(getPlatformName());
	            }
	            
	            if (constraintType == SQLTypeConstraint.CHECK) {
	                print(",\n");
	                print(columnCheckConstraint(c, checkConstraints));
	            }
	        }

	        print("\n)");
	        endStatement(StatementType.CREATE, t);
	        addComment(t, true);
	}

    /**
     * Overridden because check constraints can only be added to the table
     * level. Each constraint clause is delimited by a comma.
     */
	@Override
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
                sb.append(",\n");
            }
            sb.append("                ");
            sb.append(String.format("CONSTRAINT %s CHECK (%s)",
                    constraint.getName(),
                    helper.substitute(constraint.getConstraint())));
        }
        
        SPResolverRegistry.deregister(c, resolver);
        
        return sb.toString();
    }

}
