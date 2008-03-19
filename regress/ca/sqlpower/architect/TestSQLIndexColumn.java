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
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"PLATFORM DEFAULT\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
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
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"PLATFORM DEFAULT\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
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
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"PLATFORM DEFAULT\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
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
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"PLATFORM DEFAULT\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
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
            "    <index id=\"IDX1894\" populated=\"true\" index-type=\"PLATFORM DEFAULT\" name=\"PL_MATCH_UNIQUE\" physicalName=\"PL_MATCH_UNIQUE\" primaryKeyIndex=\"false\" unique=\"true\" >" +
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
