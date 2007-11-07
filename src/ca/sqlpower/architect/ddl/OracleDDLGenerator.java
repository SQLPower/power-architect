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
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLSequence;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLStatement.StatementType;

public class OracleDDLGenerator extends GenericDDLGenerator {

	public OracleDDLGenerator() throws SQLException {
		super();
	}

	public static final String GENERATOR_VERSION = "$Revision$";

	private static final Logger logger = Logger.getLogger(OracleDDLGenerator.class);

	private static HashSet reservedWords;

	static {
		reservedWords = new HashSet();
		reservedWords.add("ACCESS");
		reservedWords.add("ADD");
		reservedWords.add("ALL");
		reservedWords.add("ALTER");
		reservedWords.add("AND");
		reservedWords.add("ANY");
		reservedWords.add("ARRAYLEN");
		reservedWords.add("AS");
		reservedWords.add("ASC");
		reservedWords.add("AUDIT");
		reservedWords.add("BETWEEN");
		reservedWords.add("BY");
		reservedWords.add("CHAR");
		reservedWords.add("CHECK");
		reservedWords.add("CLUSTER");
		reservedWords.add("COLUMN");
		reservedWords.add("COMMENT");
		reservedWords.add("COMPRESS");
		reservedWords.add("CONNECT");
		reservedWords.add("CREATE");
		reservedWords.add("CURRENT");
		reservedWords.add("DATE");
		reservedWords.add("DECIMAL");
		reservedWords.add("DEFAULT");
		reservedWords.add("DELETE");
		reservedWords.add("DESC");
		reservedWords.add("DISTINCT");
		reservedWords.add("DROP");
		reservedWords.add("ELSE");
		reservedWords.add("EXCLUSIVE");
		reservedWords.add("EXISTS");
		reservedWords.add("FILE");
		reservedWords.add("FLOAT");
		reservedWords.add("FOR");
		reservedWords.add("FROM");
		reservedWords.add("GRANT");
		reservedWords.add("GROUP");
		reservedWords.add("HAVING");
		reservedWords.add("IDENTIFIED");
		reservedWords.add("IMMEDIATE");
		reservedWords.add("IN");
		reservedWords.add("INCREMENT");
		reservedWords.add("INDEX");
		reservedWords.add("INITIAL");
		reservedWords.add("INSERT");
		reservedWords.add("INTEGER");
		reservedWords.add("INTERSECT");
		reservedWords.add("INTO");
		reservedWords.add("IS");
		reservedWords.add("LEVEL");
		reservedWords.add("LIKE");
		reservedWords.add("LOCK");
		reservedWords.add("LONG");
		reservedWords.add("MAXEXTENTS");
		reservedWords.add("MINUS");
		reservedWords.add("MODE");
		reservedWords.add("MODIFY");
		reservedWords.add("NOAUDIT");
		reservedWords.add("NOCOMPRESS");
		reservedWords.add("NOT");
		reservedWords.add("NOTFOUND");
		reservedWords.add("NOWAIT");
		reservedWords.add("NULL");
		reservedWords.add("NUMBER");
		reservedWords.add("OF");
		reservedWords.add("OFFLINE");
		reservedWords.add("ON");
		reservedWords.add("ONLINE");
		reservedWords.add("OPTION");
		reservedWords.add("OR");
		reservedWords.add("ORDER");
		reservedWords.add("PCTFREE");
		reservedWords.add("PRIOR");
		reservedWords.add("PRIVILEGES");
		reservedWords.add("PUBLIC");
		reservedWords.add("RAW");
		reservedWords.add("RENAME");
		reservedWords.add("RESOURCE");
		reservedWords.add("REVOKE");
		reservedWords.add("ROW");
		reservedWords.add("ROWID");
		reservedWords.add("ROWLABEL");
		reservedWords.add("ROWNUM");
		reservedWords.add("ROWS");
		reservedWords.add("START");
		reservedWords.add("SELECT");
		reservedWords.add("SESSION");
		reservedWords.add("SET");
		reservedWords.add("SHARE");
		reservedWords.add("SIZE");
		reservedWords.add("SMALLINT");
		reservedWords.add("SQLBUF");
		reservedWords.add("SUCCESSFUL");
		reservedWords.add("SYNONYM");
		reservedWords.add("SYSDATE");
		reservedWords.add("TABLE");
		reservedWords.add("THEN");
		reservedWords.add("TO");
		reservedWords.add("TRIGGER");
		reservedWords.add("UID");
		reservedWords.add("UNION");
		reservedWords.add("UNIQUE");
		reservedWords.add("UPDATE");
		reservedWords.add("USER");
		reservedWords.add("VALIDATE");
		reservedWords.add("VALUES");
		reservedWords.add("VARCHAR");
		reservedWords.add("VARCHAR2");
		reservedWords.add("VIEW");
		reservedWords.add("WHENEVER");
		reservedWords.add("WHERE");
		reservedWords.add("WITH");
	}

    @Override
	public void writeHeader() {
		println("-- Created by SQLPower Oracle 8i/9i/10g DDL Generator "+GENERATOR_VERSION+" --");
	}

    @Override
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap();

		typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("NUMBER", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("RAW", Types.BINARY, 2000, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("NUMBER", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("NUMBER", Types.NUMERIC, 1, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 2000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("NUMBER", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("NUMBER", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("NUMBER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, true, false));
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
        return reservedWords.contains(word.toUpperCase());
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
        return "ALTER TABLE "
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
		Map colNameMap = new HashMap();
		SQLTable t = c.getParentTable();
		print("\n ALTER TABLE ");
		print(toQualifiedName(t.getPhysicalName()));
		print(" MODIFY ");
		print(columnDefinition(c,colNameMap));
		endStatement(DDLStatement.StatementType.MODIFY, c);
	}

    /**
     * Different from the generic generator because the "COLUMN"
     * keyword is forbidden in Oracle.
     */
    @Override
    public void addColumn(SQLColumn c) {
        Map colNameMap = new HashMap();
        print("\n ALTER TABLE ");
        print(toQualifiedName(c.getParentTable()));
        print(" ADD ");
        print(columnDefinition(c,colNameMap));
        endStatement(DDLStatement.StatementType.CREATE, c);
    }
    
    /**
     * Overridden to also create sequences if there are auto-increment columns
     * in the table.
     */
    @Override
    public void addTable(SQLTable t) throws SQLException, ArchitectException {
        
        // Create all the sequences that will be needed for auto-increment cols in this table
        for (SQLColumn c : t.getColumns()) {
            if (c.isAutoIncrement()) {
                SQLSequence seq = new SQLSequence(toIdentifier(c.getAutoIncrementSequenceName()));
                print("CREATE SEQUENCE " + seq.getName());
                endStatement(StatementType.CREATE, seq);
            }
        }
        
        super.addTable(t);
    }
}
