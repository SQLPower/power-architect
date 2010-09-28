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
package ca.sqlpower.architect.ddl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.profile.ProfileFunctionDescriptor;

/**
 * DDL Generator for Postgres 8.x (does not support e.g., ALTER COLUMN operations 7.[34]).
 */
public class PostgresDDLGenerator extends GenericDDLGenerator {

	public PostgresDDLGenerator() throws SQLException {
		super();
   	}

	public static final String GENERATOR_VERSION = "$Revision$";
	private static final Logger logger = Logger.getLogger(PostgresDDLGenerator.class);

	private static HashSet reservedWords;

	static {
		reservedWords = new HashSet();
        reservedWords.add("AND");
        reservedWords.add("ANY");
        reservedWords.add("ARRAY");
        reservedWords.add("AS");
        reservedWords.add("ASC");
        reservedWords.add("ASYMMETRIC");
        reservedWords.add("BOTH");
        reservedWords.add("CASE");
        reservedWords.add("CAST");
        reservedWords.add("CHECK");
        reservedWords.add("COLLATE");
        reservedWords.add("COLUMN");
        reservedWords.add("CONSTRAINT");
        reservedWords.add("CREATE");
        reservedWords.add("CURRENT_DATE");
        reservedWords.add("CURRENT_ROLE");
        reservedWords.add("CURRENT_TIME");
        reservedWords.add("CURRENT_TIMESTAMP");
        reservedWords.add("CURRENT_USER");
        reservedWords.add("DEFAULT");
        reservedWords.add("DEFERRABLE");
        reservedWords.add("DESC");
        reservedWords.add("DISTINCT");
        reservedWords.add("DO");
        reservedWords.add("ELSE");
        reservedWords.add("END");
        reservedWords.add("EXCEPT");
        reservedWords.add("FOR");
        reservedWords.add("FOREIGN");
        reservedWords.add("FROM");
        reservedWords.add("GRANT");
        reservedWords.add("GROUP");
        reservedWords.add("HAVING");
        reservedWords.add("IN");
        reservedWords.add("INITIALLY");
        reservedWords.add("INTERSECT");
        reservedWords.add("INTO");
        reservedWords.add("LEADING");
        reservedWords.add("LIMIT");
        reservedWords.add("LOCALTIME");
        reservedWords.add("LOCALTIMESTAMP");
        reservedWords.add("NEW");
        reservedWords.add("NOT");
        reservedWords.add("NULL");
        reservedWords.add("OFF");
        reservedWords.add("OFFSET");
        reservedWords.add("OLD");
        reservedWords.add("ON");
        reservedWords.add("ONLY");
        reservedWords.add("OR");
        reservedWords.add("ORDER");
        reservedWords.add("PLACING");
        reservedWords.add("PRIMARY");
        reservedWords.add("REFERENCES");
        reservedWords.add("RETURNING");
        reservedWords.add("SELECT");
        reservedWords.add("SESSION_USER");
        reservedWords.add("SOME");
        reservedWords.add("SYMMETRIC");
        reservedWords.add("TABLE");
        reservedWords.add("THEN");
        reservedWords.add("TO");
        reservedWords.add("TRAILING");
        reservedWords.add("UNION");
        reservedWords.add("UNIQUE");
        reservedWords.add("USER");
        reservedWords.add("USING");
        reservedWords.add("WHEN");
        reservedWords.add("WHERE");
        reservedWords.add("AUTHORIZATION");
        reservedWords.add("BETWEEN");
        reservedWords.add("BINARY");
        reservedWords.add("CROSS");
        reservedWords.add("FREEZE");
        reservedWords.add("FULL");
        reservedWords.add("ILIKE");
        reservedWords.add("INNER");
        reservedWords.add("IS");
        reservedWords.add("ISNULL");
        reservedWords.add("JOIN");
        reservedWords.add("LEFT");
        reservedWords.add("LIKE");
        reservedWords.add("NATURAL");
        reservedWords.add("NOTNULL");
        reservedWords.add("OUTER");
        reservedWords.add("OVERLAPS");
        reservedWords.add("RIGHT");
        reservedWords.add("SIMILAR");
        reservedWords.add("VERBOSE");

	}

    @Override
	public boolean isReservedWord(String word) {
		return reservedWords.contains(word.toUpperCase());
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
		typeMap = new HashMap();

		typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("NUMERIC", Types.BIGINT, 1000, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BYTEA", Types.BINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("BYTEA", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
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

    @Override
    protected void createProfileFunctionMap() {
        profileFunctionMap = new HashMap();
        profileFunctionMap.put("varchar", new ProfileFunctionDescriptor("varchar", Types.VARCHAR, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("char", new ProfileFunctionDescriptor("char", Types.CHAR, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("name", new ProfileFunctionDescriptor("name", Types.CHAR, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("bpchar", new ProfileFunctionDescriptor("bpchar", Types.CHAR, true,true,true,false,true,true,true,true));

        profileFunctionMap.put("date", new ProfileFunctionDescriptor("date", Types.DATE, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("time", new ProfileFunctionDescriptor("time", Types.TIME, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("timestamp", new ProfileFunctionDescriptor("timestamp", Types.TIMESTAMP, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("timestamptz", new ProfileFunctionDescriptor("timestamptz", Types.TIMESTAMP, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("timetz", new ProfileFunctionDescriptor("timetz", Types.TIME, true,true,true,false,true,true,true,true));


        profileFunctionMap.put("float4", new ProfileFunctionDescriptor("float", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("float8", new ProfileFunctionDescriptor("float", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("int2", new ProfileFunctionDescriptor("int", Types.INTEGER, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("int4", new ProfileFunctionDescriptor("int", Types.INTEGER, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("int8", new ProfileFunctionDescriptor("int", Types.INTEGER, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("float4", new ProfileFunctionDescriptor("float", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("float4", new ProfileFunctionDescriptor("float", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("numeric", new ProfileFunctionDescriptor("numeric", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("interval", new ProfileFunctionDescriptor("interval", Types.FLOAT, true,true,true,true,true,true,true,true));


        profileFunctionMap.put("bool", new ProfileFunctionDescriptor("bool", Types.BOOLEAN, true,false,false,false,false,false,false,true));
        profileFunctionMap.put("bit", new ProfileFunctionDescriptor("bit", Types.BIT, true,false,false,false,false,false,false,true));

        profileFunctionMap.put("bytea", new ProfileFunctionDescriptor("bytea", Types.BLOB, false,false,false,false,false,false,false,true));
        profileFunctionMap.put("text", new ProfileFunctionDescriptor("text", Types.BLOB, false,false,false,false,false,false,false,true));
        profileFunctionMap.put("oid", new ProfileFunctionDescriptor("oid", Types.BLOB, false,false,false,false,false,false,false,true));
        profileFunctionMap.put("xid", new ProfileFunctionDescriptor("xid", Types.BLOB, false,false,false,false,false,false,false,true));
        profileFunctionMap.put("cid", new ProfileFunctionDescriptor("cid", Types.BLOB, false,false,false,false,false,false,false,true));

        /* add data type start with '_' like _int4 to the mapping */
        Map profileFunctionMap2 = new HashMap();
        for ( Map.Entry<String, ProfileFunctionDescriptor> entry : profileFunctionMap.entrySet() ) {
            String key = entry.getKey();
            ProfileFunctionDescriptor pfd = entry.getValue();
            profileFunctionMap2.put("_"+key, pfd);
        }
        profileFunctionMap.putAll(profileFunctionMap2);
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
        return "\n ALTER TABLE ONLY "
            + toQualifiedName(fkTable)
            + " DROP CONSTRAINT "
            + fkName;
    }

    @Override
    public void modifyColumn(SQLColumn c) {
        Map colNameMap = new HashMap();
        SQLTable t = c.getParentTable();
        print("\n ALTER TABLE ONLY ");
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

        endStatement(DDLStatement.StatementType.MODIFY, c);

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
    public void addIndex(SQLIndex index) throws ArchitectException {
        if (index.getType() == IndexType.STATISTIC )
            return;
        checkDupIndexname(index);
        println("");
        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }
        print("INDEX ");
        print(index.getName());
        print("\n ON ");
        print(toQualifiedName(index.getParentTable()));
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : (List<SQLIndex.Column>) index.getChildren()) {
            if (!first) print(", ");
            print(c.getName());
            //TODO: ASC and DESC are not supported in the current version of PostgreSQL (8.2.3)
            //but is expected to be added in later versions (8.3 for example)
            first = false;
        }

        print(" )");
        endStatement(DDLStatement.StatementType.CREATE, index);
    }
}
