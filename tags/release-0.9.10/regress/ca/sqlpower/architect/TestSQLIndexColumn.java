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
package ca.sqlpower.architect;

import java.io.ByteArrayInputStream;

import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.SwingUIProject;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.sql.PlDotIni;

public class TestSQLIndexColumn extends SQLTestCase {
    
    private SQLIndex.Column indexColumn; 
    private SwingUIProject project;
    private static final String ENCODING="UTF-8";
    private PlDotIni plIni;
    private ArchitectSwingSession session;
    private TestingArchitectSwingSessionContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SQLIndex index = new SQLIndex("Test Index",true,"", "HASH","");
        indexColumn =index.new Column("Index1",AscendDescend.UNSPECIFIED);
        context = new TestingArchitectSwingSessionContext();
        session = context.createSession(false);
        project = session.getProject();
        plIni = new PlDotIni();
        
    }
    public TestSQLIndexColumn(String name) throws Exception {
        super(name);
    }
    
    /**
     * This tests loading an ascending column on an index works in an older way.
     * This test is to confirm that we keep backwards compatibility.
     */
    public void testHistoricAscendingCol() throws Exception {
        String testData =
            "<?xml version='1.0'?>" +
            "<architect-project version='0.1'>" +
            " <project-name>TestSwingUIProject</project-name>" +
            " <project-data-sources>" +
            "  <data-source id='DS0'>" +
            "   <property key='Logical' value='Not Configured' />" +
            "  </data-source>" +
            " </project-data-sources>" +
            " <target-database dbcs-ref='DS0'>" +
            "  <table id=\"TAB1830\" populated=\"true\" name=\"mm_project\" objectType=\"TABLE\" physicalName=\"MM_PROJECT\" remarks=\"\" >" +
            "   <folder id=\"FOL1831\" populated=\"true\" name=\"Columns\" physicalName=\"Columns\" type=\"1\" >" +
            "    <column id=\"COL1832\" populated=\"true\" autoIncrement=\"true\" autoIncrementSequenceName=\"mm_project_oid_seq\" name=\"project_oid\" nullable=\"0\" physicalName=\"PROJECT_OID\" precision=\"22\" primaryKeySeq=\"0\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1833\" populated=\"true\" autoIncrement=\"false\" name=\"FOLDER_OID\" nullable=\"1\" physicalName=\"FOLDER_OID\" precision=\"22\" referenceCount=\"2\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1834\" populated=\"true\" autoIncrement=\"false\" name=\"project_name\" nullable=\"1\" physicalName=\"PROJECT_NAME\" precision=\"80\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"12\" />" +
            "   </folder>" +
            "   <folder id=\"FOL1889\" populated=\"true\" name=\"Exported Keys\" physicalName=\"Exported Keys\" type=\"3\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1890\" populated=\"true\" name=\"Imported Keys\" physicalName=\"Imported Keys\" type=\"2\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1891\" populated=\"true\" name=\"Indices\" physicalName=\"Indices\" type=\"4\" >" +
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"BTREE\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
            "     <index-column id=\"IDC1895\" populated=\"true\" ascending=\"true\" column-ref=\"COL1834\" descending=\"false\" name=\"project_name\" physicalName=\"MATCH_ID\" />" +
            "    </index>" +
            "   </folder>" +
            "  </table>" +
            " </target-database>" +
            "</architect-project>";
        
        ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
        project.load(r, plIni);
        SQLTable table = session.getPlayPen().getTables().get(0);
        assertEquals(AscendDescend.ASCENDING, table.getIndexByName("PL_MATCH_UNIQUE").getChildren().get(0).getAscendingOrDescending());
    }
    
    /**
     * This tests loading a descending column on an index works in an older way.
     * This test is to confirm that we keep backwards compatibility.
     */
    public void testHistoricDescendingCol() throws Exception {
        String testData =
            "<?xml version='1.0'?>" +
            "<architect-project version='0.1'>" +
            " <project-name>TestSwingUIProject</project-name>" +
            " <project-data-sources>" +
            "  <data-source id='DS0'>" +
            "   <property key='Logical' value='Not Configured' />" +
            "  </data-source>" +
            " </project-data-sources>" +
            " <target-database dbcs-ref='DS0'>" +
            "  <table id=\"TAB1830\" populated=\"true\" name=\"mm_project\" objectType=\"TABLE\" physicalName=\"MM_PROJECT\" remarks=\"\" >" +
            "   <folder id=\"FOL1831\" populated=\"true\" name=\"Columns\" physicalName=\"Columns\" type=\"1\" >" +
            "    <column id=\"COL1832\" populated=\"true\" autoIncrement=\"true\" autoIncrementSequenceName=\"mm_project_oid_seq\" name=\"project_oid\" nullable=\"0\" physicalName=\"PROJECT_OID\" precision=\"22\" primaryKeySeq=\"0\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1833\" populated=\"true\" autoIncrement=\"false\" name=\"FOLDER_OID\" nullable=\"1\" physicalName=\"FOLDER_OID\" precision=\"22\" referenceCount=\"2\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1834\" populated=\"true\" autoIncrement=\"false\" name=\"project_name\" nullable=\"1\" physicalName=\"PROJECT_NAME\" precision=\"80\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"12\" />" +
            "   </folder>" +
            "   <folder id=\"FOL1889\" populated=\"true\" name=\"Exported Keys\" physicalName=\"Exported Keys\" type=\"3\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1890\" populated=\"true\" name=\"Imported Keys\" physicalName=\"Imported Keys\" type=\"2\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1891\" populated=\"true\" name=\"Indices\" physicalName=\"Indices\" type=\"4\" >" +
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"BTREE\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
            "     <index-column id=\"IDC1895\" populated=\"true\" ascending=\"false\" column-ref=\"COL1834\" descending=\"true\" name=\"project_name\" physicalName=\"MATCH_ID\" />" +
            "    </index>" +
            "   </folder>" +
            "  </table>" +
            " </target-database>" +
            "</architect-project>";
        
        ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
        project.load(r, plIni);
        SQLTable table = session.getPlayPen().getTables().get(0);
        assertEquals(AscendDescend.DESCENDING, table.getIndexByName("PL_MATCH_UNIQUE").getChildren().get(0).getAscendingOrDescending());
    }
    
    /**
     * This tests loading an unspecified column on an index works in an older way.
     * This test is to confirm that we keep backwards compatibility.
     */
    public void testHistoricUnspecifiedCol() throws Exception {
        String testData =
            "<?xml version='1.0'?>" +
            "<architect-project version='0.1'>" +
            " <project-name>TestSwingUIProject</project-name>" +
            " <project-data-sources>" +
            "  <data-source id='DS0'>" +
            "   <property key='Logical' value='Not Configured' />" +
            "  </data-source>" +
            " </project-data-sources>" +
            " <target-database dbcs-ref='DS0'>" +
            "  <table id=\"TAB1830\" populated=\"true\" name=\"mm_project\" objectType=\"TABLE\" physicalName=\"MM_PROJECT\" remarks=\"\" >" +
            "   <folder id=\"FOL1831\" populated=\"true\" name=\"Columns\" physicalName=\"Columns\" type=\"1\" >" +
            "    <column id=\"COL1832\" populated=\"true\" autoIncrement=\"true\" autoIncrementSequenceName=\"mm_project_oid_seq\" name=\"project_oid\" nullable=\"0\" physicalName=\"PROJECT_OID\" precision=\"22\" primaryKeySeq=\"0\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1833\" populated=\"true\" autoIncrement=\"false\" name=\"FOLDER_OID\" nullable=\"1\" physicalName=\"FOLDER_OID\" precision=\"22\" referenceCount=\"2\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1834\" populated=\"true\" autoIncrement=\"false\" name=\"project_name\" nullable=\"1\" physicalName=\"PROJECT_NAME\" precision=\"80\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"12\" />" +
            "   </folder>" +
            "   <folder id=\"FOL1889\" populated=\"true\" name=\"Exported Keys\" physicalName=\"Exported Keys\" type=\"3\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1890\" populated=\"true\" name=\"Imported Keys\" physicalName=\"Imported Keys\" type=\"2\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1891\" populated=\"true\" name=\"Indices\" physicalName=\"Indices\" type=\"4\" >" +
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"BTREE\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
            "     <index-column id=\"IDC1895\" populated=\"true\" ascending=\"false\" column-ref=\"COL1834\" descending=\"false\" name=\"project_name\" physicalName=\"MATCH_ID\" />" +
            "    </index>" +
            "   </folder>" +
            "  </table>" +
            " </target-database>" +
            "</architect-project>";
        
        ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
        project.load(r, plIni);
        SQLTable table = session.getPlayPen().getTables().get(0);
        assertEquals(AscendDescend.UNSPECIFIED, table.getIndexByName("PL_MATCH_UNIQUE").getChildren().get(0).getAscendingOrDescending());
    }
    
    /**
     * This tests loading an ascending column on an index works.
     */
    public void testAscendingCol() throws Exception {
        String testData =
            "<?xml version='1.0'?>" +
            "<architect-project version='0.1'>" +
            " <project-name>TestSwingUIProject</project-name>" +
            " <project-data-sources>" +
            "  <data-source id='DS0'>" +
            "   <property key='Logical' value='Not Configured' />" +
            "  </data-source>" +
            " </project-data-sources>" +
            " <target-database dbcs-ref='DS0'>" +
            "  <table id=\"TAB1830\" populated=\"true\" name=\"mm_project\" objectType=\"TABLE\" physicalName=\"MM_PROJECT\" remarks=\"\" >" +
            "   <folder id=\"FOL1831\" populated=\"true\" name=\"Columns\" physicalName=\"Columns\" type=\"1\" >" +
            "    <column id=\"COL1832\" populated=\"true\" autoIncrement=\"true\" autoIncrementSequenceName=\"mm_project_oid_seq\" name=\"project_oid\" nullable=\"0\" physicalName=\"PROJECT_OID\" precision=\"22\" primaryKeySeq=\"0\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1833\" populated=\"true\" autoIncrement=\"false\" name=\"FOLDER_OID\" nullable=\"1\" physicalName=\"FOLDER_OID\" precision=\"22\" referenceCount=\"2\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1834\" populated=\"true\" autoIncrement=\"false\" name=\"project_name\" nullable=\"1\" physicalName=\"PROJECT_NAME\" precision=\"80\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"12\" />" +
            "   </folder>" +
            "   <folder id=\"FOL1889\" populated=\"true\" name=\"Exported Keys\" physicalName=\"Exported Keys\" type=\"3\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1890\" populated=\"true\" name=\"Imported Keys\" physicalName=\"Imported Keys\" type=\"2\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1891\" populated=\"true\" name=\"Indices\" physicalName=\"Indices\" type=\"4\" >" +
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"CLUSTERED\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
            "     <index-column id=\"IDC1895\" populated=\"true\" ascendingOrDescending=\"ASCENDING\" column-ref=\"COL1834\" name=\"project_name\" physicalName=\"MATCH_ID\" />" +
            "    </index>" +
            "   </folder>" +
            "  </table>" +
            " </target-database>" +
            "</architect-project>";
        
        ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
        project.load(r, plIni);
        SQLTable table = session.getPlayPen().getTables().get(0);
        assertEquals(AscendDescend.ASCENDING, table.getIndexByName("PL_MATCH_UNIQUE").getChildren().get(0).getAscendingOrDescending());
    }
    /**
     * This tests loading a descending column on an index works.
     */
    public void testDescendingCol() throws Exception {
        String testData =
            "<?xml version='1.0'?>" +
            "<architect-project version='0.1'>" +
            " <project-name>TestSwingUIProject</project-name>" +
            " <project-data-sources>" +
            "  <data-source id='DS0'>" +
            "   <property key='Logical' value='Not Configured' />" +
            "  </data-source>" +
            " </project-data-sources>" +
            " <target-database dbcs-ref='DS0'>" +
            "  <table id=\"TAB1830\" populated=\"true\" name=\"mm_project\" objectType=\"TABLE\" physicalName=\"MM_PROJECT\" remarks=\"\" >" +
            "   <folder id=\"FOL1831\" populated=\"true\" name=\"Columns\" physicalName=\"Columns\" type=\"1\" >" +
            "    <column id=\"COL1832\" populated=\"true\" autoIncrement=\"true\" autoIncrementSequenceName=\"mm_project_oid_seq\" name=\"project_oid\" nullable=\"0\" physicalName=\"PROJECT_OID\" precision=\"22\" primaryKeySeq=\"0\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1833\" populated=\"true\" autoIncrement=\"false\" name=\"FOLDER_OID\" nullable=\"1\" physicalName=\"FOLDER_OID\" precision=\"22\" referenceCount=\"2\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1834\" populated=\"true\" autoIncrement=\"false\" name=\"project_name\" nullable=\"1\" physicalName=\"PROJECT_NAME\" precision=\"80\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"12\" />" +
            "   </folder>" +
            "   <folder id=\"FOL1889\" populated=\"true\" name=\"Exported Keys\" physicalName=\"Exported Keys\" type=\"3\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1890\" populated=\"true\" name=\"Imported Keys\" physicalName=\"Imported Keys\" type=\"2\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1891\" populated=\"true\" name=\"Indices\" physicalName=\"Indices\" type=\"4\" >" +
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"BTREE\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
            "     <index-column id=\"IDC1895\" populated=\"true\" ascendingOrDescending=\"DESCENDING\" column-ref=\"COL1834\" name=\"project_name\" physicalName=\"MATCH_ID\" />" +
            "    </index>" +
            "   </folder>" +
            "  </table>" +
            " </target-database>" +
            "</architect-project>";
        
        ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
        project.load(r, plIni);
        SQLTable table = session.getPlayPen().getTables().get(0);
        assertEquals(AscendDescend.DESCENDING, table.getIndexByName("PL_MATCH_UNIQUE").getChildren().get(0).getAscendingOrDescending());
    }
    /**
     * This tests loading an unspecified column on an index works.
     */
    public void testUnspecifiedCol() throws Exception {
        String testData =
            "<?xml version='1.0'?>" +
            "<architect-project version='0.1'>" +
            " <project-name>TestSwingUIProject</project-name>" +
            " <project-data-sources>" +
            "  <data-source id='DS0'>" +
            "   <property key='Logical' value='Not Configured' />" +
            "  </data-source>" +
            " </project-data-sources>" +
            " <target-database dbcs-ref='DS0'>" +
            "  <table id=\"TAB1830\" populated=\"true\" name=\"mm_project\" objectType=\"TABLE\" physicalName=\"MM_PROJECT\" remarks=\"\" >" +
            "   <folder id=\"FOL1831\" populated=\"true\" name=\"Columns\" physicalName=\"Columns\" type=\"1\" >" +
            "    <column id=\"COL1832\" populated=\"true\" autoIncrement=\"true\" autoIncrementSequenceName=\"mm_project_oid_seq\" name=\"project_oid\" nullable=\"0\" physicalName=\"PROJECT_OID\" precision=\"22\" primaryKeySeq=\"0\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1833\" populated=\"true\" autoIncrement=\"false\" name=\"FOLDER_OID\" nullable=\"1\" physicalName=\"FOLDER_OID\" precision=\"22\" referenceCount=\"2\" remarks=\"\" scale=\"0\" type=\"4\" />" +
            "    <column id=\"COL1834\" populated=\"true\" autoIncrement=\"false\" name=\"project_name\" nullable=\"1\" physicalName=\"PROJECT_NAME\" precision=\"80\" referenceCount=\"1\" remarks=\"\" scale=\"0\" type=\"12\" />" +
            "   </folder>" +
            "   <folder id=\"FOL1889\" populated=\"true\" name=\"Exported Keys\" physicalName=\"Exported Keys\" type=\"3\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1890\" populated=\"true\" name=\"Imported Keys\" physicalName=\"Imported Keys\" type=\"2\" >" +
            "   </folder>" +
            "   <folder id=\"FOL1891\" populated=\"true\" name=\"Indices\" physicalName=\"Indices\" type=\"4\" >" +
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"BTREE\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
            "     <index-column id=\"IDC1895\" populated=\"true\" ascendingOrDescending=\"UNSPECIFIED\" column-ref=\"COL1834\" name=\"project_name\" physicalName=\"MATCH_ID\" />" +
            "    </index>" +
            "   </folder>" +
            "  </table>" +
            " </target-database>" +
            "</architect-project>";
        
        ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
        project.load(r, plIni);
        SQLTable table = session.getPlayPen().getTables().get(0);
        assertEquals(AscendDescend.UNSPECIFIED, table.getIndexByName("PL_MATCH_UNIQUE").getChildren().get(0).getAscendingOrDescending());
    }

    @Override
    protected SQLObject getSQLObjectUnderTest() {
        
        return indexColumn;
    }

}
