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
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLSequence;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;

/**
 * DDL Generator for Postgres 8.x (does not support e.g., ALTER COLUMN operations 7.[34]).
 */
public class PostgresDDLGenerator extends GenericDDLGenerator {

	public PostgresDDLGenerator() throws SQLException {
		super();
   	}

	public static final String GENERATOR_VERSION = "$Revision$";
	private static final Logger logger = Logger.getLogger(PostgresDDLGenerator.class);

    /**
     * These words are the words reserved by PostgreSQL and cannot be used as a
     * name for tables, columns, relationships, indices, etc.
     */
	public static final HashSet<String> RESERVED_WORDS;

	static {
		RESERVED_WORDS = new HashSet<String>();
        RESERVED_WORDS.add("AND");
        RESERVED_WORDS.add("ANY");
        RESERVED_WORDS.add("ARRAY");
        RESERVED_WORDS.add("AS");
        RESERVED_WORDS.add("ASC");
        RESERVED_WORDS.add("ASYMMETRIC");
        RESERVED_WORDS.add("BOTH");
        RESERVED_WORDS.add("CASE");
        RESERVED_WORDS.add("CAST");
        RESERVED_WORDS.add("CHECK");
        RESERVED_WORDS.add("COLLATE");
        RESERVED_WORDS.add("COLUMN");
        RESERVED_WORDS.add("CONSTRAINT");
        RESERVED_WORDS.add("CREATE");
        RESERVED_WORDS.add("CURRENT_DATE");
        RESERVED_WORDS.add("CURRENT_ROLE");
        RESERVED_WORDS.add("CURRENT_TIME");
        RESERVED_WORDS.add("CURRENT_TIMESTAMP");
        RESERVED_WORDS.add("CURRENT_USER");
        RESERVED_WORDS.add("DEFAULT");
        RESERVED_WORDS.add("DEFERRABLE");
        RESERVED_WORDS.add("DESC");
        RESERVED_WORDS.add("DISTINCT");
        RESERVED_WORDS.add("DO");
        RESERVED_WORDS.add("ELSE");
        RESERVED_WORDS.add("END");
        RESERVED_WORDS.add("EXCEPT");
        RESERVED_WORDS.add("FOR");
        RESERVED_WORDS.add("FOREIGN");
        RESERVED_WORDS.add("FROM");
        RESERVED_WORDS.add("GRANT");
        RESERVED_WORDS.add("GROUP");
        RESERVED_WORDS.add("HAVING");
        RESERVED_WORDS.add("IN");
        RESERVED_WORDS.add("INITIALLY");
        RESERVED_WORDS.add("INTERSECT");
        RESERVED_WORDS.add("INTO");
        RESERVED_WORDS.add("LEADING");
        RESERVED_WORDS.add("LIMIT");
        RESERVED_WORDS.add("LOCALTIME");
        RESERVED_WORDS.add("LOCALTIMESTAMP");
        RESERVED_WORDS.add("NEW");
        RESERVED_WORDS.add("NOT");
        RESERVED_WORDS.add("NULL");
        RESERVED_WORDS.add("OFF");
        RESERVED_WORDS.add("OFFSET");
        RESERVED_WORDS.add("OLD");
        RESERVED_WORDS.add("ON");
        RESERVED_WORDS.add("ONLY");
        RESERVED_WORDS.add("OR");
        RESERVED_WORDS.add("ORDER");
        RESERVED_WORDS.add("PLACING");
        RESERVED_WORDS.add("PRIMARY");
        RESERVED_WORDS.add("REFERENCES");
        RESERVED_WORDS.add("RETURNING");
        RESERVED_WORDS.add("SELECT");
        RESERVED_WORDS.add("SESSION_USER");
        RESERVED_WORDS.add("SOME");
        RESERVED_WORDS.add("SYMMETRIC");
        RESERVED_WORDS.add("TABLE");
        RESERVED_WORDS.add("THEN");
        RESERVED_WORDS.add("TO");
        RESERVED_WORDS.add("TRAILING");
        RESERVED_WORDS.add("UNION");
        RESERVED_WORDS.add("UNIQUE");
        RESERVED_WORDS.add("USER");
        RESERVED_WORDS.add("USING");
        RESERVED_WORDS.add("WHEN");
        RESERVED_WORDS.add("WHERE");
        RESERVED_WORDS.add("AUTHORIZATION");
        RESERVED_WORDS.add("BETWEEN");
        RESERVED_WORDS.add("BINARY");
        RESERVED_WORDS.add("CROSS");
        RESERVED_WORDS.add("FREEZE");
        RESERVED_WORDS.add("FULL");
        RESERVED_WORDS.add("ILIKE");
        RESERVED_WORDS.add("INNER");
        RESERVED_WORDS.add("IS");
        RESERVED_WORDS.add("ISNULL");
        RESERVED_WORDS.add("JOIN");
        RESERVED_WORDS.add("LEFT");
        RESERVED_WORDS.add("LIKE");
        RESERVED_WORDS.add("NATURAL");
        RESERVED_WORDS.add("NOTNULL");
        RESERVED_WORDS.add("OUTER");
        RESERVED_WORDS.add("OVERLAPS");
        RESERVED_WORDS.add("RIGHT");
        RESERVED_WORDS.add("SIMILAR");
        RESERVED_WORDS.add("VERBOSE");

	}

	public String getName() {
	    return "PostgreSQL";
	}

    @Override
	public boolean isReservedWord(String word) {
		return RESERVED_WORDS.contains(word.toUpperCase());
	}

	@Override
	public void writeHeader() {
		println("-- Created by SQLPower PostgreSQL DDL Generator "+GENERATOR_VERSION+" --");
	}

	/**
	 * Creates and populates <code>typeMap</code> using
	 * DatabaseMetaData, but ignores nullability as reported by the
	 * driver's type map (because all types are reported as
	 * non-nullable).
	 */
	@Override
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap<Integer, GenericTypeDescriptor>();

		typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 1000, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BYTEA", Types.BINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("BYTEA", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("BOOLEAN", Types.BOOLEAN, 1, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("TEXT", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("NUMERIC", Types.DECIMAL, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE PRECISION", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("REAL", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("BYTEA", Types.LONGVARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("TEXT", Types.LONGVARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("SMALLINT", Types.TINYINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("BYTEA", Types.VARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}

	/**
	 * Turns a logical identifier into a legal identifier (physical name) for PostgreSQL.
     * Also, downcases the identifier for consistency.
     *
     * <p>Uses a deterministic method to generate tie-breaking numbers when there is a namespace
     * conflict.  If you pass null as the physical name, it will use just the logical name when
     * trying to come up with tie-breaking hashes for identifier names.  If the first attempt
     * at generating a unique name fails, subsequent calls should pass each new illegal
     * identifier which will be used with the logical name to generate a another hash.
     *
     * <p>Postgres 8.0 rules:
     * <ul>
     *  <li> no spaces
     *  <li> 63 character limit
     *  <li> identifiers must begin with a letter (one is added if needed)
     *  <li> can't be a postgres reserved word
     *  <li> can only be comprised of letters, numbers, underscores, and $
     * </ul>
	 */
	private String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		if (logger.isDebugEnabled()) logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_').toLowerCase();
		if (logger.isDebugEnabled()) logger.debug("after replace of spaces: " + ident);


		// replace anything that is not a letter, character, or underscore with an underscore...
		ident = ident.replaceAll("[^a-zA-Z0-9_$]", "_");

		// first time through
        // XXX clean this up
		if (physicalName == null) {
			// length is ok
            if (ident.length() <= 63) {
				return ident;
			} else {
				// length is too big
				if (logger.isDebugEnabled()) logger.debug("truncating identifier: " + ident);
				String base = ident.substring(0, 60);
				int tiebreaker = ((ident.hashCode() % 1000) + 1000) % 1000;
				if (logger.isDebugEnabled()) logger.debug("new identifier: " + base + tiebreaker);
				return (base + tiebreaker);
			}
		} else {
			// back for more, which means that we probably
            // had a namespace conflict.  Hack the ident down
            // to size if it's too big, and then generate
            // a hash tiebreaker using the ident and the
            // passed value physicalName
			if (logger.isDebugEnabled()) logger.debug("physical identifier is not unique, regenerating: " + physicalName);
			String base = ident;
			if (ident.length() > 63) {
				base = ident.substring(0, 60);
			}
			int tiebreaker = (((ident + physicalName).hashCode() % 1000) + 1000) % 1000;
			if (logger.isDebugEnabled()) logger.debug("regenerated identifier is: " + (base + tiebreaker));
			return (base + tiebreaker);
		}
	}

    @Override
	public String toIdentifier(String name) {
		return toIdentifier(name,null);
	}

	/**
     * Generates a command for dropping a foreign key.
     * The statement looks like <code>ALTER TABLE ONLY $fktable DROP CONSTRAINT $fkname</code>.
     */
    @Override
    public String makeDropForeignKeySQL(String fkTable, String fkName) {
        return "\nALTER TABLE ONLY "
            + toQualifiedName(fkTable)
            + " DROP CONSTRAINT "
            + fkName;
    }

    @Override
    public void modifyColumn(SQLColumn c) {
        Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
        SQLTable t = c.getParent();
        print("\nALTER TABLE ONLY ");
        print( toQualifiedName(t) );
        print(" ALTER COLUMN ");

        // Column name
        String columnPhysName = createPhysicalName(colNameMap,c);
        print(columnPhysName);
        print(" TYPE ");
        print(columnType(c));

        // Column nullability
        print(", ALTER COLUMN ");
        print(columnPhysName);
        print(" ");
        print(c.isDefinitelyNullable() ? "DROP" : "SET");
        print(" NOT NULL");

        endStatement(StatementType.MODIFY, c);

    }

	/**
	 * Returns null, even though Postgres calls this "Database."  The reason is,
	 * you can't refer to objects in a different database than the default
	 * database for your current connection.  Also, the Postgres DatabaseMetaData
	 * always shows nulls for the catalog/database name of tables.
	 */
    @Override
	public String getCatalogTerm() {
		return null;
	}

	/**
	 * Returns "Schema".
	 */
    @Override
	public String getSchemaTerm() {
		return "Schema";
	}

	/**
	 * Returns the previously-set target schema name, or "public" if there is no
	 * current setting. Public is the Postgres default when no schema is
	 * specified.
	 */
    @Override
	public String getTargetSchema() {
		if (targetSchema != null) return targetSchema;
		else return "public";
	}

    /**
     * create index ddl in postgresql syntax
     */
    @Override
    public void addIndex(SQLIndex index) throws SQLObjectException {

        createPhysicalName(topLevelNames, index);
        println("");
        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }
        print("INDEX ");
        print(toIdentifier(index.getName()));
        print("\n ON ");
        print(toQualifiedName(index.getParent()));
        if(index.getType() != null) {
            print(" USING "+ index.getType());
        }
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : index.getChildren(SQLIndex.Column.class)) {
            if (!first) print(", ");
            if (c.getColumn() != null) {
                print(c.getColumn().getPhysicalName());
            } else {
                print(c.getName());
            }
            //TODO: ASC and DESC are not supported in the current version of PostgreSQL (8.2.3)
            //but is expected to be added in later versions (8.3 for example)
			//Thomas Kellerer: ASC/DESC is available since 8.3.0...
            print(c.getAscendingOrDescending() == AscendDescend.ASCENDING ? " ASC" : "");
            print(c.getAscendingOrDescending() == AscendDescend.DESCENDING ? " DESC" : "");

            first = false;
        }

        print(" )");
        endStatement(StatementType.CREATE, index);
        if(index.isClustered()) {
            addCluster(index, toIdentifier(index.getName()), index.getParent().getName());
        }
    }

    /**
     * This will create a clustered index on a given table.
     */
    private void addCluster(SQLIndex index, String indexName, String table) {
        println("");
        print("CLUSTER " + indexName + " ON " + table);
        endStatement(StatementType.CREATE, index);
    }

    @Override
    public void addTable(SQLTable t) throws SQLException, SQLObjectException {

        // Create all the sequences that will be needed for auto-increment cols in this table
        for (SQLColumn c : t.getColumns()) {
            if (c.isAutoIncrement()) {
                SQLSequence seq = new SQLSequence(toIdentifier(c.getAutoIncrementSequenceName()));
                print("\nCREATE SEQUENCE ");
                print(toQualifiedName(seq.getName()));
                endStatement(StatementType.CREATE, seq);
            }
        }

        super.addTable(t);

        // attach sequences to columns
        for (SQLColumn c : t.getColumns()) {
            if (c.isAutoIncrement()) {
                SQLSequence seq = new SQLSequence(toIdentifier(c.getAutoIncrementSequenceName()));
                print("\nALTER SEQUENCE " + toQualifiedName(seq.getName()) + " OWNED BY " + toQualifiedName(t) + "." + c.getPhysicalName());
                endStatement(StatementType.CREATE, seq);
            }
        }
    }

    /**
     * Augments the default columnDefinition behaviour by adding the correct
     * default value clause for auto-increment columns. For non-autoincrement
     * columns, the behaviour is the same as {@link GenericDDLGenerator#columnDefinition(SQLColumn, Map)}.
     */
    @Override
    protected String columnDefinition(SQLColumn c, Map<String, SQLObject> colNameMap) {
        String nameAndType = super.columnDefinition(c, colNameMap);

        if (c.isAutoIncrement()) {
            SQLSequence seq = new SQLSequence(toIdentifier(c.getAutoIncrementSequenceName()));
            return nameAndType + " DEFAULT nextval(" + SQL.quote(toQualifiedName(seq.getName())) + ")";
        } else {
            return nameAndType;
        }
    }

    @Override
    protected String getPlatformName() {
        return "PostgreSQL";
    }
    
	@Override
	public void renameIndex(SQLIndex oldIndex, SQLIndex newIndex) throws SQLObjectException {
		print("ALTER INDEX ");
		print(toQualifiedName(oldIndex));
		print(" RENAME TO ");
		println(toQualifiedName(newIndex.getName()));
		endStatement(StatementType.ALTER, oldIndex);
	}

	/**
	 * Drop the specified index.
	 * <br/>
	 * The default implementation should work for all databases.
	 *
	 * @param index the index to drop.
	 * @throws SQLObjectException
	 */
	public void dropIndex(SQLIndex index) throws SQLObjectException {
		print("DROP INDEX ");
		print(toQualifiedName(index));
		println(" CASCADE");
		endStatement(StatementType.DROP, index);
	}

    /**
     * XXX Although PostgreSQL supports enumeration, a separate type must be
     * declared to define this enumeration before using it in a column. Also, if
     * we used PostgreSQL's enumeration feature, we would need to ensure the
     * type it is defined under has a unique name, and that it is deleted once
     * the column is dropped.
     * 
     * Instead, we will allow the {@link DDLGenerator} to define the enumeration
     * as a check constraint instead, which is perfectly fine, and simpler to
     * implement. There is no need to worry about this check constraint being
     * unique or any cleanup after the column is dropped.
     */
	@Override
	public boolean supportsEnumeration() {
	    return false;
	}
}
