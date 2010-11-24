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
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.TestingArchitectSessionContext;
import ca.sqlpower.architect.diff.CompareSQL;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.CompareDMFormatter;
import ca.sqlpower.architect.swingui.CompareDMPanel;
import ca.sqlpower.architect.swingui.CompareDMSettings;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.CompareDMSettings.DatastoreType;
import ca.sqlpower.architect.swingui.CompareDMSettings.OutputFormat;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;

public class SQLServerDDLGeneratorTest extends TestCase {
    
    private static final Logger logger = Logger.getLogger(SQLServerDDLGenerator.class);

	public void testGenerateComment() throws Exception {
		// it shouldn't matter which of the two (2000, 2005) are instantiated
		// as the functionality for forward engineering comments is implemented
		// in the base class
		SQLServerDDLGenerator ddl = new SQLServer2000DDLGenerator();
		SQLTable tbl = new SQLTable();
		tbl.initFolders(true);
		tbl.setPhysicalName("test_table");
		tbl.setRemarks("Test single ' quotes");
		SQLColumn id = new SQLColumn(tbl, "id", Types.INTEGER, 0, 0);
		id.setRemarks("The row's primary key");
		tbl.addColumn(id);
		SQLColumn name = new SQLColumn(tbl, "name", Types.VARCHAR, 50, 0);
		name.setRemarks("The person's name");
		tbl.addColumn(name);
		ddl.addTable(tbl);

		// the first statement is the CREATE table
		// second, third and fourth statements should be the comments as SQL comments starting with --
		// as SQL Server does not support comments on database objects (at least not in a sensible manner)
		List<DDLStatement> stmts = ddl.getDdlStatements();
		assertEquals(4, stmts.size());
		assertEquals("-- Comment for table [test_table]: Test single ' quotes", stmts.get(1).getSQLText().trim());
		assertEquals("-- Comment for column [id]: The row's primary key", stmts.get(2).getSQLText().trim());
		assertEquals("-- Comment for column [name]: The person's name", stmts.get(3).getSQLText().trim());
	}

    /**
     * Test for the first part of bug 1827. Previously with SQL Server 2008 if
     * you forward engineered a table and then compared the table with the play
     * pen that was just used to forward engineer it you would find differences.
     * However there should be none.
     */
	public void testCompareDMClean() throws Exception {
	    ArchitectSessionContext context = new TestingArchitectSessionContext();
        final DataSourceCollection<JDBCDataSource> plIni = context.getPlDotIni();
        JDBCDataSource ds = plIni.getDataSource("sql server 2008", JDBCDataSource.class);
        
        if (ds == null) 
            fail("No server named 'sql server 2008' is defined in the ini file so we cannot connect to the database.");
        
        String tableName = "TestCompareDMClean";
        final SQLDatabase db = new SQLDatabase(ds);
        Connection con = null;
        try {
            con = db.getConnection();
            Statement stmt = con.createStatement();
            stmt.execute("drop table " + tableName);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            con.close();
            con = null;
        }
        
        
        //create a table with the test columns in the play pen
        SQLDatabase ppdb = new SQLDatabase();
        ppdb.setPlayPenDatabase(true);
        ppdb.setPopulated(true);
        ppdb.setName("Play pen DB");
        SQLTable t = new SQLTable(ppdb, true);
        t.setName(tableName);
        SQLColumn bigIntCol = new SQLColumn();
        bigIntCol.setName("bigIntCol");
        bigIntCol.setType(plIni.getSQLType("BIGINT"));
        assertNotNull(bigIntCol.getUserDefinedSQLType().getUpstreamType());
        t.addColumn(bigIntCol);
        SQLColumn tinyIntCol = new SQLColumn();
        tinyIntCol.setName("tinyIntCol");
        tinyIntCol.setType(plIni.getSQLType("TINYINT"));
        assertNotNull(tinyIntCol.getUserDefinedSQLType().getUpstreamType());
        t.addColumn(tinyIntCol);
        SQLColumn dateTimeCol = new SQLColumn();
        dateTimeCol.setName("dateTimeCol");
        dateTimeCol.setType(plIni.getSQLType("DATE"));
        assertNotNull(dateTimeCol.getUserDefinedSQLType().getUpstreamType());
        t.addColumn(dateTimeCol);
        
        try {
            //forward engineer the play pen to the connection
            List<DDLStatement> statements = DDLUtils.createDDLGenerator(ds).generateDDLStatements(Collections.singletonList(t));
            logger.info("Running script " + statements);
            con = db.getConnection();
            Statement stmt = con.createStatement();
            for (DDLStatement statement : statements) {
                stmt.execute(statement.getSQLText());
            }
            
            //create a compareSQL object with the play pen objects in one side and the database in the other
            SQLTable dbTable = db.getTableByName(tableName);
            CompareSQL comparator = new CompareSQL(Collections.singletonList(dbTable), Collections.singletonList(t), true);
            //run the compareDM 
            List<DiffChunk<SQLObject>> diffs = comparator.generateTableDiffs(context.createSession());
            CompareDMSettings dmSettings = new CompareDMSettings();
            dmSettings.setOutputFormat(OutputFormat.SQL);
            dmSettings.setDdlGenerator(SQLServer2005DDLGenerator.class);
            dmSettings.setSuppressSimilarities(true);
            dmSettings.getSourceSettings().setDatastoreType(DatastoreType.DATABASE);
            ArchitectSwingSession swingSession = new TestingArchitectSwingSessionContext().createSession();
            CompareDMPanel panel = new CompareDMPanel(swingSession, null);
            dmSettings.setSourceStuff(panel.new SourceOrTargetStuff() {
                @Override
                public synchronized SQLDatabase getDatabase() {
                    return db;
                } 
            });
            dmSettings.getTargetSettings().setDatastoreType(DatastoreType.PROJECT);
            CompareDMFormatter formatter = new CompareDMFormatter(swingSession, null, dmSettings);
            DDLGenerator gen = formatter.formatForSQLOutput(diffs, diffs, dbTable, t);
            assertTrue(gen.getDdlStatements().isEmpty());
        } finally {
            if (con != null) {
                try {
                    Statement stmt = con.createStatement();
                    stmt.execute("drop table " + tableName);
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    try {
                        con.close();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        }
    }

    /**
     * Test relating to bug 1827. While the first test checks forward
     * engineering a table will match what is in the database if the user
     * actually creates a table elsewhere with a date or time column they will
     * compare as nvarchars in the compare DM feature.
     */
    public void testCompareDMDates() throws Exception {
        ArchitectSessionContext context = new TestingArchitectSessionContext();
        final DataSourceCollection<JDBCDataSource> plIni = context.getPlDotIni();
        JDBCDataSource ds = plIni.getDataSource("sql server 2008", JDBCDataSource.class);
        
        if (ds == null) 
            fail("No server named 'sql server 2008' is defined in the ini file so we cannot connect to the database.");
        
        String tableName = "TestCompareDMDates";
        final SQLDatabase db = new SQLDatabase(ds);
        Connection con = null;
        try {
            con = db.getConnection();
            Statement stmt = con.createStatement();
            stmt.execute("drop table " + tableName);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            con.close();
            con = null;
        }
        
        
        //create a table with the test columns in the play pen
        SQLDatabase ppdb = new SQLDatabase();
        ppdb.setPlayPenDatabase(true);
        ppdb.setPopulated(true);
        ppdb.setName("Play pen DB");
        SQLTable t = new SQLTable(ppdb, true);
        t.setName(tableName);
        SQLColumn dateCol = new SQLColumn();
        dateCol.setName("dateCol");
        dateCol.setType(plIni.getSQLType("DATE"));
        assertNotNull(dateCol.getUserDefinedSQLType().getUpstreamType());
        t.addColumn(dateCol);
        SQLColumn timeCol = new SQLColumn();
        timeCol.setName("timeCol");
        timeCol.setType(plIni.getSQLType("TIME"));
        assertNotNull(timeCol.getUserDefinedSQLType().getUpstreamType());
        t.addColumn(timeCol);
        SQLColumn timestampCol = new SQLColumn();
        timestampCol.setName("timestampCol");
        timestampCol.setType(plIni.getSQLType("TIMESTAMP"));
        assertNotNull(timestampCol.getUserDefinedSQLType().getUpstreamType());
        t.addColumn(timestampCol);
        
        try {
            //forward engineer the play pen to the connection
            con = db.getConnection();
            Statement stmt = con.createStatement();
            stmt.execute("Create table " + tableName + "(dateCol DATE NOT NULL, timeCol TIME NOT NULL, timestampCol DATETIME NOT NULL)");
            
            //create a compareSQL object with the play pen objects in one side and the database in the other
            SQLTable dbTable = db.getTableByName(tableName);
            CompareSQL comparator = new CompareSQL(Collections.singletonList(dbTable), Collections.singletonList(t), true);
            //run the compareDM 
            List<DiffChunk<SQLObject>> diffs = comparator.generateTableDiffs(context.createSession());
            CompareDMSettings dmSettings = new CompareDMSettings();
            dmSettings.setOutputFormat(OutputFormat.SQL);
            dmSettings.setDdlGenerator(SQLServer2005DDLGenerator.class);
            dmSettings.setSuppressSimilarities(true);
            dmSettings.getSourceSettings().setDatastoreType(DatastoreType.DATABASE);
            ArchitectSwingSession swingSession = new TestingArchitectSwingSessionContext().createSession();
            CompareDMPanel panel = new CompareDMPanel(swingSession, null);
            dmSettings.setSourceStuff(panel.new SourceOrTargetStuff() {
                @Override
                public synchronized SQLDatabase getDatabase() {
                    return db;
                } 
            });
            dmSettings.getTargetSettings().setDatastoreType(DatastoreType.PROJECT);
            CompareDMFormatter formatter = new CompareDMFormatter(swingSession, null, dmSettings);
            DDLGenerator gen = formatter.formatForSQLOutput(diffs, diffs, dbTable, t);
            assertTrue(gen.getDdlStatements().isEmpty());
        } finally {
            if (con != null) {
                try {
                    Statement stmt = con.createStatement();
                    stmt.execute("drop table " + tableName);
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    try {
                        con.close();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        }
    }
	
}
