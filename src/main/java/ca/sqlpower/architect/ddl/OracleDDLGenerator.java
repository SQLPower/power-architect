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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLStatement.StatementType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLSequence;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;

/**
 * The base class for version-specific Oracle DDL generators. This class exists
 * in addition to the two version-specific ones in order to provide a generic
 * Oracle DDL generator for older PL.ini files that don't specify the newer DDL
 * generators
 */
public class OracleDDLGenerator extends GenericDDLGenerator {

	public OracleDDLGenerator() throws SQLException {
		super();
	}

	public static final String GENERATOR_VERSION = "$Revision$";

	private static final Logger logger = Logger.getLogger(OracleDDLGenerator.class);

    /**
     * These words are the words reserved by Oracle and cannot be used as a name
     * for tables, columns, relationships, indices, etc.
     */
	public static final HashSet<String> RESERVED_WORDS;

	static {
		RESERVED_WORDS = new HashSet<String>();
		RESERVED_WORDS.add("ACCESS");
		RESERVED_WORDS.add("ADD");
		RESERVED_WORDS.add("ALL");
		RESERVED_WORDS.add("ALTER");
		RESERVED_WORDS.add("AND");
		RESERVED_WORDS.add("ANY");
		RESERVED_WORDS.add("ARRAYLEN");
		RESERVED_WORDS.add("AS");
		RESERVED_WORDS.add("ASC");
		RESERVED_WORDS.add("AUDIT");
		RESERVED_WORDS.add("BETWEEN");
		RESERVED_WORDS.add("BY");
		RESERVED_WORDS.add("CHAR");
		RESERVED_WORDS.add("CHECK");
		RESERVED_WORDS.add("CLUSTER");
		RESERVED_WORDS.add("COLUMN");
		RESERVED_WORDS.add("COMMENT");
		RESERVED_WORDS.add("COMPRESS");
		RESERVED_WORDS.add("CONNECT");
		RESERVED_WORDS.add("CREATE");
		RESERVED_WORDS.add("CURRENT");
		RESERVED_WORDS.add("DATE");
		RESERVED_WORDS.add("DECIMAL");
		RESERVED_WORDS.add("DEFAULT");
		RESERVED_WORDS.add("DELETE");
		RESERVED_WORDS.add("DESC");
		RESERVED_WORDS.add("DISTINCT");
		RESERVED_WORDS.add("DROP");
		RESERVED_WORDS.add("ELSE");
		RESERVED_WORDS.add("EXCLUSIVE");
		RESERVED_WORDS.add("EXISTS");
		RESERVED_WORDS.add("FILE");
		RESERVED_WORDS.add("FLOAT");
		RESERVED_WORDS.add("FOR");
		RESERVED_WORDS.add("FROM");
		RESERVED_WORDS.add("GRANT");
		RESERVED_WORDS.add("GROUP");
		RESERVED_WORDS.add("HAVING");
		RESERVED_WORDS.add("IDENTIFIED");
		RESERVED_WORDS.add("IMMEDIATE");
		RESERVED_WORDS.add("IN");
		RESERVED_WORDS.add("INCREMENT");
		RESERVED_WORDS.add("INDEX");
		RESERVED_WORDS.add("INITIAL");
		RESERVED_WORDS.add("INSERT");
		RESERVED_WORDS.add("INTEGER");
		RESERVED_WORDS.add("INTERSECT");
		RESERVED_WORDS.add("INTO");
		RESERVED_WORDS.add("IS");
		RESERVED_WORDS.add("LEVEL");
		RESERVED_WORDS.add("LIKE");
		RESERVED_WORDS.add("LOCK");
		RESERVED_WORDS.add("LONG");
		RESERVED_WORDS.add("MAXEXTENTS");
		RESERVED_WORDS.add("MINUS");
		RESERVED_WORDS.add("MODE");
		RESERVED_WORDS.add("MODIFY");
		RESERVED_WORDS.add("NOAUDIT");
		RESERVED_WORDS.add("NOCOMPRESS");
		RESERVED_WORDS.add("NOT");
		RESERVED_WORDS.add("NOTFOUND");
		RESERVED_WORDS.add("NOWAIT");
		RESERVED_WORDS.add("NULL");
		RESERVED_WORDS.add("NUMBER");
		RESERVED_WORDS.add("OF");
		RESERVED_WORDS.add("OFFLINE");
		RESERVED_WORDS.add("ON");
		RESERVED_WORDS.add("ONLINE");
		RESERVED_WORDS.add("OPTION");
		RESERVED_WORDS.add("OR");
		RESERVED_WORDS.add("ORDER");
		RESERVED_WORDS.add("PCTFREE");
		RESERVED_WORDS.add("PRIOR");
		RESERVED_WORDS.add("PRIVILEGES");
		RESERVED_WORDS.add("PUBLIC");
		RESERVED_WORDS.add("RAW");
		RESERVED_WORDS.add("RENAME");
		RESERVED_WORDS.add("RESOURCE");
		RESERVED_WORDS.add("REVOKE");
		RESERVED_WORDS.add("ROW");
		RESERVED_WORDS.add("ROWID");
		RESERVED_WORDS.add("ROWLABEL");
		RESERVED_WORDS.add("ROWNUM");
		RESERVED_WORDS.add("ROWS");
		RESERVED_WORDS.add("START");
		RESERVED_WORDS.add("SELECT");
		RESERVED_WORDS.add("SESSION");
		RESERVED_WORDS.add("SET");
		RESERVED_WORDS.add("SHARE");
		RESERVED_WORDS.add("SIZE");
		RESERVED_WORDS.add("SMALLINT");
		RESERVED_WORDS.add("SQLBUF");
		RESERVED_WORDS.add("SUCCESSFUL");
		RESERVED_WORDS.add("SYNONYM");
		RESERVED_WORDS.add("SYSDATE");
		RESERVED_WORDS.add("TABLE");
		RESERVED_WORDS.add("THEN");
		RESERVED_WORDS.add("TO");
		RESERVED_WORDS.add("TRIGGER");
		RESERVED_WORDS.add("UID");
		RESERVED_WORDS.add("UNION");
		RESERVED_WORDS.add("UNIQUE");
		RESERVED_WORDS.add("UPDATE");
		RESERVED_WORDS.add("USER");
		RESERVED_WORDS.add("VALIDATE");
		RESERVED_WORDS.add("VALUES");
		RESERVED_WORDS.add("VARCHAR");
		RESERVED_WORDS.add("VARCHAR2");
		RESERVED_WORDS.add("VIEW");
		RESERVED_WORDS.add("WHENEVER");
		RESERVED_WORDS.add("WHERE");
		RESERVED_WORDS.add("WITH");
	}

	public String getName() {
	    return "Oracle 8i";
	}

    @Override
	public void writeHeader() {
		println("-- Created by SQLPower Oracle 8i/9i/10g DDL Generator "+GENERATOR_VERSION+" --");
	}

    @Override
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap<Integer, GenericTypeDescriptor>();

		typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("NUMBER", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("RAW", Types.BINARY, 2000, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("NUMBER", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("NUMBER", Types.NUMERIC, 1, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 2000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("NUMBER", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("NUMBER", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("NUMBER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("LONG RAW", Types.LONGVARBINARY, 2000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMBER", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("NUMBER", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("NUMBER", Types.SMALLINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("DATE", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("DATE", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("NUMBER", Types.TINYINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("LONG", Types.LONGVARCHAR, 2000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("LONG RAW", Types.VARBINARY, 2000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR2", Types.VARCHAR, 4000, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}

	/**
	 * Turns a logical identifier into a legal identifier (physical name) for Oracle 8i/9i.
     * Also, upcases the identifier for consistency.
     *
     * <p>Uses a deterministic method to generate tie-breaking numbers when there is a namespace
     * conflict.  If you pass null as the physical name, it will use just the logical name when
     * trying to come up with tie-breaking hashes for identifier names.  If the first attempt
     * at generating a unique name fails, subsequent calls should pass each new illegal
     * identifier which will be used with the logical name to generate a another hash.
     *
     * <p>Oracle Rules:
     * <ul>
     *  <li> no spaces
     *  <li> 30 character limit
     *  <li> identifiers must begin with a letter (the offending chunk of characters is moved to the
     *       back of the new physical identifier)
     *  <li> can't be an oracle reserved word
     *  <li> can only be comprised of letters, numbers, and underscores (XXX: does not play well with regex chars like ^ and |)
     * </ul>
	 */
	private String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		if (logger.isDebugEnabled()) logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_').toUpperCase();
		if (logger.isDebugEnabled()) logger.debug("after replace of spaces: " + ident);

		// replace anything that is not a letter, character, or underscore with an underscore...
		ident = ident.replaceAll("[^a-zA-Z0-9_]", "_");

		// first time through
		if (physicalName == null) {
			// length is ok
            if (ident.length() < 31) {
				return ident;
			} else {
				// length is too big
				if (logger.isDebugEnabled()) logger.debug("truncating identifier: " + ident);
				String base = ident.substring(0,27);
				int tiebreaker = ((ident.hashCode() % 1000) + 1000) % 1000;
				if (logger.isDebugEnabled()) logger.debug("new identifier: " + base + tiebreaker);
				return (base + tiebreaker);
			}
		} else {
			// back for more, which means that we had a
            // namespace conflict.  Hack the ident down
            // to size if it's too big, and then generate
            // a hash tiebreaker using the ident and the
            // current value of physicalName
			if (logger.isDebugEnabled()) logger.debug("physical idenfier is not unique, regenerating: " + physicalName);
			String base = ident;
			if (ident.length() > 27) {
				base = ident.substring(0, 27);
			}
			int tiebreaker = (((ident + physicalName).hashCode() % 1000) + 1000) % 1000;
			if (logger.isDebugEnabled()) logger.debug("regenerated identifier is: " + (base + tiebreaker));
			return (base + tiebreaker);
		}
    }

    /**
     * Subroutine for toIdentifier().  Probably a generally useful feature that we
     * should pull up to the GenericDDLGenerator.
     */
    public boolean isReservedWord(String word) {
        return RESERVED_WORDS.contains(word.toUpperCase());
    }

    @Override
	public String toIdentifier(String name) {
		return toIdentifier(name,null);
	}

	/**
	 * Returns null because Oracle doesn't have catalogs.
	 */
    @Override
	public String getCatalogTerm() {
		return null;
	}

	/**
	 * Returns the string "Schema".
	 */
    @Override
	public String getSchemaTerm() {
		return "Schema";
	}

    /**
     * Generates a command for dropping a foreign key on oracle.
     * The statement looks like <code>ALTER TABLE $fktable DROP CONSTRAINT $fkname</code>.
     */
    @Override
    public String makeDropForeignKeySQL(String fkTable, String fkName) {
        return "\nALTER TABLE "
            + toQualifiedName(fkTable)
            + " DROP CONSTRAINT "
            + fkName;
    }

    /**
     * Different from the generic generator because Oracle
     * requires the non-standard keyword "MODIFY" instead of
     * "ALTER COLUMN".
     */
    @Override
    public void modifyColumn(SQLColumn c) {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
		SQLTable t = c.getParent();
		print("\nALTER TABLE ");
		print(toQualifiedName(t.getPhysicalName()));
		print(" MODIFY ");
		print(columnDefinition(c,colNameMap));
		endStatement(StatementType.MODIFY, c);
	}

    protected String columnNullability(SQLColumn c) {
        GenericTypeDescriptor td = failsafeGetTypeDescriptor(c);
        if (c.isDefinitelyNullable()) {
			if (! td.isNullable()) {
				throw new UnsupportedOperationException
					("The data type "+td.getName()+" is not nullable on the target database platform.");
			}
			return " NULL";
		} else {
			return " NOT NULL";
		}
    }
    
    /**
     * Different from the generic generator because the "COLUMN"
     * keyword is forbidden in Oracle.
     */
    @Override
    public void addColumn(SQLColumn c) {
        Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
        print("\nALTER TABLE ");
        print(toQualifiedName(c.getParent()));
        print(" ADD ");
        print(columnDefinition(c,colNameMap));
        endStatement(StatementType.CREATE, c);
    }
    
    /**
     * create index ddl in oracle syntax
     */
    @Override
    public void addIndex(SQLIndex index) throws SQLObjectException {
        createPhysicalName(topLevelNames, index);
        println("");
        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }
		boolean isBitmapIndex = index.getType() != null && index.getType().equals("BITMAP");
        if(isBitmapIndex) {
            print("BITMAP ");
        }
        print("INDEX ");
        print(toQualifiedName(index.getName()));
        print("\n ON ");
        print(toQualifiedName(index.getParent()));
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : index.getChildren(SQLIndex.Column.class)) {
            if (!first) print(", ");
            print(c.getColumn().getPhysicalName());
			if (!isBitmapIndex) {
				print(c.getAscendingOrDescending() == AscendDescend.ASCENDING ? " ASC" : "");
				print(c.getAscendingOrDescending() == AscendDescend.DESCENDING ? " DESC" : "");
			}
            first = false;
        }

        print(" )");
        if(index.getType() != null && index.getType().equals("CTXCAT")) {            
            print("\n INDEXTYPE IS "+index.getType());
        }
        endStatement(StatementType.CREATE, index);
    }
    
    /**
     * Overridden to also create sequences if there are auto-increment columns
     * in the table.
     */
    @Override
    public void addTable(SQLTable t) throws SQLException, SQLObjectException {
        
        // Create all the sequences that will be needed for auto-increment cols in this table
        for (SQLColumn c : t.getColumns()) {
            if (c.isAutoIncrement()) {
                SQLSequence seq = new SQLSequence(toIdentifier(c.getAutoIncrementSequenceName()));
                print("\nCREATE SEQUENCE ");
                print(seq.getName());
                endStatement(StatementType.CREATE, seq);
            }
        }
        
        super.addTable(t);
    }
    
    @Override
    public boolean supportsDeleteAction(SQLRelationship r) {
        UpdateDeleteRule action = r.getDeleteRule();
        return (action != UpdateDeleteRule.SET_DEFAULT);
    }
    
    @Override
    public String getDeleteActionClause(SQLRelationship r) {
        UpdateDeleteRule action = r.getDeleteRule();
        if (action == UpdateDeleteRule.CASCADE) {
            return "ON DELETE CASCADE";
        } else if (action == UpdateDeleteRule.SET_NULL) {
            return "ON DELETE SET NULL";
        } else if (action == UpdateDeleteRule.RESTRICT) {
            return "";
        } else if (action == UpdateDeleteRule.NO_ACTION) {
            return "";
        } else {
            throw new IllegalArgumentException("Oracle does not support this delete action: " + action);
        }
    }
    
    /**
     * Oracle does not support any explicit ON UPDATE clause in FK constraints,
     * but the default behaviour is basically the same as NO ACTION or RESTRICT
     * of other platforms. So this method claims those rules are supported,
     * but the others are not.
     */
    @Override
    public boolean supportsUpdateAction(SQLRelationship r) {
        UpdateDeleteRule action = r.getUpdateRule();
        return (action == UpdateDeleteRule.NO_ACTION) || (action == UpdateDeleteRule.RESTRICT);
    }

    /**
     * Only returns the empty string for supported update actions (see
     * {@link #supportsUpdateAction(SQLRelationship)}), and throws an
     * {@link IllegalArgumentException} if the update rule is not supported.
     */
    @Override
    public String getUpdateActionClause(SQLRelationship r) {
        UpdateDeleteRule action = r.getUpdateRule();
        if (!supportsUpdateAction(r)) {
            throw new IllegalArgumentException(
                    "This update action is not supported in Oracle: " + action);
        } else {
            return "";
        }
    }

    @Override
    public boolean supportsRollback() {
        return false;
    }
}
