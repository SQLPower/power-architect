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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.Deferrability;


public class SQLServerDDLGenerator extends GenericDDLGenerator {
	public SQLServerDDLGenerator() throws SQLException {
		super();
	}

	public static final String GENERATOR_VERSION = "$Revision$";
	private static final Logger logger = Logger.getLogger(SQLServerDDLGenerator.class);

	private static HashSet reservedWords;

	static {
		reservedWords = new HashSet();
		reservedWords.add("ADD");
		reservedWords.add("ALL");
		reservedWords.add("ALTER");
		reservedWords.add("AND");
		reservedWords.add("ANY");
		reservedWords.add("AS");
		reservedWords.add("ASC");
		reservedWords.add("AUTHORIZATION");
		reservedWords.add("BACKUP");
		reservedWords.add("BEGIN");
		reservedWords.add("BETWEEN");
		reservedWords.add("BREAK");
		reservedWords.add("BROWSE");
		reservedWords.add("BULK");
		reservedWords.add("BY");
		reservedWords.add("CASCADE");
		reservedWords.add("CASE");
		reservedWords.add("CHECK");
		reservedWords.add("CHECKPOINT");
		reservedWords.add("CLOSE");
		reservedWords.add("CLUSTERED");
		reservedWords.add("COALESCE");
		reservedWords.add("COLLATE");
		reservedWords.add("COLUMN");
		reservedWords.add("COMMIT");
		reservedWords.add("COMPUTE");
		reservedWords.add("CONSTRAINT");
		reservedWords.add("CONTAINS");
		reservedWords.add("CONTAINSTABLE");
		reservedWords.add("CONTINUE");
		reservedWords.add("CONVERT");
		reservedWords.add("CREATE");
		reservedWords.add("CROSS");
		reservedWords.add("CURRENT");
		reservedWords.add("CURRENT_DATE");
		reservedWords.add("CURRENT_TIME");
		reservedWords.add("CURRENT_TIMESTAMP");
		reservedWords.add("CURRENT_USER");
		reservedWords.add("CURSOR");
		reservedWords.add("DATABASE");
		reservedWords.add("DBCC");
		reservedWords.add("DEALLOCATE");
		reservedWords.add("DECLARE");
		reservedWords.add("DEFAULT");
		reservedWords.add("DELETE");
		reservedWords.add("DENY");
		reservedWords.add("DESC");
		reservedWords.add("DISK");
		reservedWords.add("DISTINCT");
		reservedWords.add("DISTRIBUTED");
		reservedWords.add("DOUBLE");
		reservedWords.add("DROP");
		reservedWords.add("DUMMY");
		reservedWords.add("DUMP");
		reservedWords.add("ELSE");
		reservedWords.add("END");
		reservedWords.add("ERRLVL");
		reservedWords.add("ESCAPE");
		reservedWords.add("EXCEPT");
		reservedWords.add("EXEC");
		reservedWords.add("EXECUTE");
		reservedWords.add("EXISTS");
		reservedWords.add("EXIT");
		reservedWords.add("FETCH");
		reservedWords.add("FILE");
		reservedWords.add("FILLFACTOR");
		reservedWords.add("FOR");
		reservedWords.add("FOREIGN");
		reservedWords.add("FREETEXT");
		reservedWords.add("FREETEXTTABLE");
		reservedWords.add("FROM");
		reservedWords.add("FULL");
		reservedWords.add("FUNCTION");
		reservedWords.add("GOTO");
		reservedWords.add("GRANT");
		reservedWords.add("GROUP");
		reservedWords.add("HAVING");
		reservedWords.add("HOLDLOCK");
		reservedWords.add("IDENTITY");
		reservedWords.add("IDENTITY_INSERT");
		reservedWords.add("IDENTITYCOL");
		reservedWords.add("IF");
		reservedWords.add("IN");
		reservedWords.add("INDEX");
		reservedWords.add("INNER");
		reservedWords.add("INSERT");
		reservedWords.add("INTERSECT");
		reservedWords.add("INTO");
		reservedWords.add("IS");
		reservedWords.add("JOIN");
		reservedWords.add("KEY");
		reservedWords.add("KILL");
		reservedWords.add("LEFT");
		reservedWords.add("LIKE");
		reservedWords.add("LINENO");
		reservedWords.add("LOAD");
		reservedWords.add("NATIONAL");
		reservedWords.add("NOCHECK");
		reservedWords.add("NONCLUSTERED");
		reservedWords.add("NOT");
		reservedWords.add("NULL");
		reservedWords.add("NULLIF");
		reservedWords.add("OF");
		reservedWords.add("OFF");
		reservedWords.add("OFFSETS");
		reservedWords.add("ON");
		reservedWords.add("OPEN");
		reservedWords.add("OPENDATASOURCE");
		reservedWords.add("OPENQUERY");
		reservedWords.add("OPENROWSET");
		reservedWords.add("OPENXML");
		reservedWords.add("OPTION");
		reservedWords.add("OR");
		reservedWords.add("ORDER");
		reservedWords.add("OUTER");
		reservedWords.add("OVER");
		reservedWords.add("PERCENT");
		reservedWords.add("PLAN");
		reservedWords.add("PRECISION");
		reservedWords.add("PRIMARY");
		reservedWords.add("PRINT");
		reservedWords.add("PROC");
		reservedWords.add("PROCEDURE");
		reservedWords.add("PUBLIC");
		reservedWords.add("RAISERROR");
		reservedWords.add("READ");
		reservedWords.add("READTEXT");
		reservedWords.add("RECONFIGURE");
		reservedWords.add("REFERENCES");
		reservedWords.add("REPLICATION");
		reservedWords.add("RESTORE");
		reservedWords.add("RESTRICT");
		reservedWords.add("RETURN");
		reservedWords.add("REVOKE");
		reservedWords.add("RIGHT");
		reservedWords.add("ROLLBACK");
		reservedWords.add("ROWCOUNT");
		reservedWords.add("ROWGUIDCOL");
		reservedWords.add("RULE");
		reservedWords.add("SAVE");
		reservedWords.add("SCHEMA");
		reservedWords.add("SELECT");
		reservedWords.add("SESSION_USER");
		reservedWords.add("SET");
		reservedWords.add("SETUSER");
		reservedWords.add("SHUTDOWN");
		reservedWords.add("SOME");
		reservedWords.add("STATISTICS");
		reservedWords.add("SYSTEM_USER");
		reservedWords.add("TABLE");
		reservedWords.add("TEXTSIZE");
		reservedWords.add("THEN");
		reservedWords.add("TO");
		reservedWords.add("TOP");
		reservedWords.add("TRAN");
		reservedWords.add("TRANSACTION");
		reservedWords.add("TRIGGER");
		reservedWords.add("TRUNCATE");
		reservedWords.add("TSEQUAL");
		reservedWords.add("UNION");
		reservedWords.add("UNIQUE");
		reservedWords.add("UPDATE");
		reservedWords.add("UPDATETEXT");
		reservedWords.add("USE");
		reservedWords.add("USER");
		reservedWords.add("VALUES");
		reservedWords.add("VARYING");
		reservedWords.add("VIEW");
		reservedWords.add("WAITFOR");
		reservedWords.add("WHEN");
		reservedWords.add("WHERE");
		reservedWords.add("WHILE");
		reservedWords.add("WITH");
		reservedWords.add("WRITETEXT");
	}

	public String getName() {
	    return "Microsoft SQL Server";
	}

	public boolean isReservedWord(String word) {
		return reservedWords.contains(word.toUpperCase());
	}

    @Override
	public void writeHeader() {
		println("-- Created by SQLPower SQLServer 2000 DDL Generator "+GENERATOR_VERSION+" --");
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
		typeMap = new HashMap();

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
        print("\n ALTER TABLE ");
        print(toQualifiedName(c.getParentTable()));
        print(" ADD ");
        print(columnDefinition(c,new HashMap()));
        endStatement(DDLStatement.StatementType.CREATE, c);
    }

    @Override
    public void addIndex(SQLIndex index) throws ArchitectException {
        if (logger.isDebugEnabled()) {
            String parentTableName = null;
            String parentFolder = null;
            if (index.getParentTable() != null) {
                parentTableName = index.getParentTable().getName();
            }
            if (index.getParent() != null) {
                parentFolder = index.getParent().getName();
            }
            logger.debug("Adding index: " + index + " (parent table " + parentTableName + ") (parentFolder " + parentFolder + ")");
        }
        if (index.getType() == SQLIndex.STATISTIC )
            return;

        createPhysicalName(topLevelNames, index);

        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }
        print(" "+ index.getType()+" ");
        print("INDEX ");
        print(DDLUtils.toQualifiedName(null,null,index.getName()));
        print("\n ON ");
        print(toQualifiedName(index.getParentTable()));
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : (List<SQLIndex.Column>) index.getChildren()) {
            if (!first) print(", ");
            print(c.getName());
            first = false;
        }
        print(" )\n");
        endStatement(DDLStatement.StatementType.CREATE, index);
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
}
