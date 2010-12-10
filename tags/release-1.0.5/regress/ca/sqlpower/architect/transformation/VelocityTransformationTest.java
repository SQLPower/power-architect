/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.transformation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.SwingUIProjectLoader;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.sql.PlDotIni;

/**
 *
 * @author Thomas Kellerer
 */
public class VelocityTransformationTest extends TestCase {
    
    public VelocityTransformationTest(String testName) {
        super(testName);
    }

	private SwingUIProjectLoader project;
	private static final String ENCODING="UTF-8";
	private PlDotIni plIni;
    private ArchitectSwingSession session;
    private TestingArchitectSwingSessionContext context;

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.SwingUIProject.SwingUIProject(String)'
	 */
	public void setUp() throws Exception {
        context = new TestingArchitectSwingSessionContext();
        session = context.createSession();
        project = session.getProjectLoader();
        plIni = new PlDotIni();
        plIni.read(new File("pl.regression.ini"));
		ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
		project.load(r, plIni);
	}

	private final String testData =
        "<?xml version='1.0'?>" +
        "<architect-project version='0.1'>" +
        " <project-name>TestSwingUIProject</project-name>" +
        " <project-data-sources>" +
        "  <data-source id='DS0'>" +
        "   <property key='Logical' value='Not Configured' />" +
        "  </data-source>" +
        " </project-data-sources>" +
        " <source-databases>" +
        " </source-databases>" +
        " <target-database dbcs-ref='DS0'>" +
        "  <table id='TAB0' populated='true' primaryKeyName='id' remarks='' name='Customers' >" +
        "   <folder id='FOL1' populated='true' name='Columns' type='1' >" +
        "    <column id='COL2' populated='true' autoIncrement='false' name='id' defaultValue='' nullable='0' precision='10' primaryKeySeq='0' referenceCount='1' remarks='' scale='0' type='12' />" +
        "    <column id='COL3' populated='true' autoIncrement='false' name='name' defaultValue='' nullable='0' precision='10' referenceCount='1' remarks='' scale='0' type='12' />" +
        "   </folder>" +
        "   <folder id='FOL4' populated='true' name='Exported Keys' type='3' >" +
        "   </folder>" +
        "   <folder id='FOL5' populated='true' name='Imported Keys' type='2' >" +
        "   </folder>" +
        "   <folder id='FOL13' populated='true' name='Indices' type='4' >" +
        "   </folder>" +
        "  </table>" +
        "  <table id='TAB6' populated='true' primaryKeyName='id' remarks='' name='Orders' >" +
        "   <folder id='FOL7' populated='true' name='Columns' type='1' >" +
        "    <column id='COL8' populated='true' autoIncrement='false' name='id' defaultValue='' " +
        "    nullable='0' precision='10' primaryKeySeq='0' referenceCount='1' scale='0' type='12' />" +
        "    <column id='COL9' populated='true' autoIncrement='false' name='customer_id' defaultValue='' nullable='0' precision='10' referenceCount='1' remarks='' scale='0' type='4' />" +
        "    <column id='COL15' populated='true' autoIncrement='false' name='purchase_date' defaultValue='' nullable='0' precision='0' referenceCount='1' remarks='' scale='0' type='91' />" +
        "   </folder>" +
        "   <folder id='FOL10' populated='true' name='Exported Keys' type='3' >" +
        "   </folder>" +
        "   <folder id='FOL11' populated='true' name='Imported Keys' type='2' >" +
        "   </folder>" +
        "   <folder id='FOL14' populated='true' name='Indices' type='4' >" +
        "   </folder>" +
        "  </table>" +
        "  <table id=\"TAB1830\" populated=\"true\" name=\"mm_project\" objectType=\"TABLE\" physicalName=\"MM_PROJECT\" remarks=\"\" >" +
        "   <folder id=\"FOL1831\" populated=\"true\" name=\"Columns\" physicalName=\"Columns\" type=\"1\" >" +
        "    <column id=\"COL1832\" populated=\"true\" autoIncrement=\"true\" autoIncrementSequenceName=\"mm_project_oid_seq\" name=\"project_oid\" nullable=\"0\" physicalName=\"PROJECT_OID\" precision=\"22\" primaryKeySeq=\"0\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"4\" />" +
        "    <column id=\"COL1833\" populated=\"true\" autoIncrement=\"false\" name=\"FOLDER_OID\" nullable=\"1\" physicalName=\"FOLDER_OID\" precision=\"22\" referenceCount=\"2\" remarks=\"\" scale=\"0\" type=\"4\" />" +
        "    <column id=\"COL1834\" populated=\"true\" autoIncrement=\"false\" name=\"project_name\" nullable=\"1\" physicalName=\"PROJECT_NAME\" precision=\"80\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"12\" />" +
        "    <column id=\"COL2222\" populated=\"true\" autoIncrement=\"false\" name=\"vision\" nullable=\"1\" physicalName=\"SIGHT\" precision=\"20\" referenceCount=\"1\" remarks=\"\" scale=\"20\" type=\"3\" />" +
        "   </folder>" +
        "   <folder id=\"FOL1889\" populated=\"true\" name=\"Exported Keys\" physicalName=\"Exported Keys\" type=\"3\" >" +
        "   </folder>" +
        "   <folder id=\"FOL1890\" populated=\"true\" name=\"Imported Keys\" physicalName=\"Imported Keys\" type=\"2\" >" +
        "   </folder>" +
        "   <folder id=\"FOL1891\" populated=\"true\" name=\"Indices\" physicalName=\"Indices\" type=\"4\" >" +
        "    <index id=\"IDX1892\" populated=\"true\" index-type=\"BTREE\" name=\"mm_project_pk\" physicalName=\"PL_MATCH_PK\" primaryKeyIndex=\"true\" unique=\"true\" >" +
        "     <index-column id=\"IDC1893\" populated=\"true\" ascending=\"false\" column-ref=\"COL1832\" descending=\"false\" name=\"project_oid\" physicalName=\"MATCH_OID\" />" +
        "    </index>" +
        "    <index id=\"IDX1894\" populated=\"true\" index-type=\"BTREE\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
        "     <index-column id=\"IDC1895\" populated=\"true\" ascending=\"false\" column-ref=\"COL1834\" descending=\"false\" name=\"project_name\" physicalName=\"MATCH_ID\" />" +
        "    </index>" +
        "   </folder>" +
        "  </table>" +
        "  <relationships>" +
        "   <relationship id='REL12' populated='true' deferrability='0' deleteRule='0' fk-table-ref='TAB0' fkCardinality='6' identifying='true' name='Orders_Customers_fk' pk-table-ref='TAB6' pkCardinality='2' updateRule='0' >" +
        "    <column-mapping id='CMP13' populated='true' fk-column-ref='COL2' pk-column-ref='COL8' />" +
        "   </relationship>" +
        "   <reference ref-id='REL12' />" +
        "  </relationships>" +
        " </target-database>" +
        " <ddl-generator type='ca.sqlpower.architect.ddl.SQLServerDDLGenerator' allow-connection='true'> </ddl-generator>" +
        " <compare-dm-settings ddlGenerator='ca.sqlpower.architect.ddl.SQLServerDDLGenerator' outputFormatAsString='ENGLISH'>" +
        " <source-stuff datastoreTypeAsString='PROJECT' connectName='Arthur_test' " +
        " schema='ARCHITECT_REGRESS' filepath='' />"+
        "<target-stuff datastoreTypeAsString='FILE' filePath='Testpath' /> </compare-dm-settings>"+
        " <play-pen zoom=\"12.3\" viewportX=\"200\" viewportY=\"20\" relationship-style=\"rectilinear\" showPrimaryTag=\"true\" showForeignTag=\"true\" showAlternateTag=\"true\"  columnVisibility=\"PK_FK\" >" +
        "  <table-pane table-ref='TAB0' x='85' y='101' />" +
        "  <table-pane table-ref='TAB6' x='196' y='38' />" +
        "  <table-link relationship-ref='REL12' pk-x='76' pk-y='60' fk-x='114' fk-y='30' />" +
        " </play-pen>" +
        " <profiles topNCount=\"10\">" +
        " </profiles>" +
        "</architect-project>";

	public void testTransform() throws Exception {
		File tempdir = new File(System.getProperty("java.io.tmpdir"));
		File vm = new File(tempdir, "test_template.vm");

		OutputStream out = new FileOutputStream(vm);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, ENCODING));
		
		writer.print("#foreach ($table in $tables) \n" +
		  "Table: $table.name\n" +
		  "#foreach ($col in $table.columns)\n" +
		  "     $col.shortDisplayName\n" +
		  "#end\n" +
		  "#end\n");
		writer.close();

		File output = new File(tempdir, "test-output.txt");

		VelocityTransformation vt = new VelocityTransformation();
		vt.transform(vm, output, session);
		assertTrue(output.exists());
		FileInputStream fi = new FileInputStream(output);
		InputStreamReader in = new InputStreamReader(fi, "UTF-8");
		List<String> lines = IOUtils.readLines(in);
		assertEquals("Table: Customers", lines.get(0));
		assertEquals("     id: VARCHAR(10)", lines.get(1));
		assertEquals("     name: VARCHAR(10)", lines.get(2));
		assertEquals("Table: Orders", lines.get(3));
        assertEquals("     id: VARCHAR(10)", lines.get(4));
        assertEquals("     customer_id: INTEGER", lines.get(5));
        assertEquals("     purchase_date: DATE", lines.get(6));
        assertEquals("Table: mm_project", lines.get(7));
        assertEquals("     project_oid: INTEGER", lines.get(8));
        assertEquals("     FOLDER_OID: INTEGER", lines.get(9));
        assertEquals("     project_name: VARCHAR(80)", lines.get(10));
        assertEquals("     vision: DECIMAL(20, 20)", lines.get(11));
	}

}
