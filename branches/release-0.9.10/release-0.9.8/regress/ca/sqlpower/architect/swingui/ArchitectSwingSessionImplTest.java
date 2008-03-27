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

package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.sql.PlDotIni;

public class ArchitectSwingSessionImplTest extends TestCase {

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
        "    <column id='COL2' populated='true' autoIncrement='false' name='id' defaultValue='' nullable='0' precision='10' primaryKeySeq='0' referenceCount='1' remarks='' scale='0' type='4' />" +
        "    <column id='COL3' populated='true' autoIncrement='false' name='name' defaultValue='' nullable='0' precision='10' referenceCount='1' remarks='' scale='0' type='4' />" +
        "   </folder>" +
        "   <folder id='FOL4' populated='true' name='Exported Keys' type='3' >" +
        "   </folder>" +
        "   <folder id='FOL5' populated='true' name='Imported Keys' type='2' >" +
        "   </folder>" +
        "  </table>" +
        "  <table id='TAB6' populated='true' primaryKeyName='id' remarks='' name='Orders' >" +
        "   <folder id='FOL7' populated='true' name='Columns' type='1' >" +
        "    <column id='COL8' populated='true' autoIncrement='false' name='i&amp;d' defaultValue='' " +
        "    remarks=\"This isn't a problem\" nullable='0' precision='10' primaryKeySeq='0' referenceCount='1' scale='0' type='4' />" +
        "    <column id='COL9' populated='true' autoIncrement='false' name='customer&lt;id' defaultValue='' nullable='0' precision='10' referenceCount='1' remarks='' scale='0' type='4' />" +
        "   </folder>" +
        "   <folder id='FOL10' populated='true' name='Exported Keys' type='3' >" +
        "   </folder>" +
        "   <folder id='FOL11' populated='true' name='Imported Keys' type='2' >" +
        "   </folder>" +
        "  </table>" +
        "  <relationships>" +
        "   <relationship id='REL12' populated='true' deferrability='0' deleteRule='0' fk-table-ref='TAB0' fkCardinality='6' identifying='true' name='Orders_Customers_fk' pk-table-ref='TAB6' pkCardinality='2' updateRule='0' >" +
        "    <column-mapping id='CMP13' populated='true' fk-column-ref='COL2' pk-column-ref='COL8' />" +
        "   </relationship>" +
        "   <reference ref-id='REL12' />" +
        "  </relationships>" +
        " </target-database>" +
        " <ddl-generator type='ca.sqlpower.architect.ddl.GenericDDLGenerator' allow-connection='true'> </ddl-generator>" + 
        " <compare-dm-settings sqlScriptFormat='SQLServer 2000' outputFormatAsString='ENGLISH'>" +        
        " <source-stuff datastoreTypeAsString='PROJECT' connectName='Arthur_test' " +
        " schema='ARCHITECT_REGRESS' filepath='' />"+
        "<target-stuff datastoreTypeAsString='FILE' filePath='Testpath' /> </compare-dm-settings>"+
        " <play-pen>" +
        "  <table-pane table-ref='TAB0' x='85' y='101' />" +
        "  <table-pane table-ref='TAB6' x='196' y='38' />" +
        "  <table-link relationship-ref='REL12' pk-x='76' pk-y='60' fk-x='114' fk-y='30' />" +
        " </play-pen>" +
        " <profiles topNCount=\"10\">" +
        "  <profile-result ref-id=\"TAB0\" type=\"ca.sqlpower.architect.profile.TableProfileResult\" createStartTime=\"1185828799320\" createEndTime=\"1185828807187\" exception=\"false\"   rowCount=\"234937\"/>" +
        "  <profile-result ref-id=\"COL2\" type=\"ca.sqlpower.architect.profile.ColumnProfileResult\" createStartTime=\"1185828799479\" createEndTime=\"1185828801322\" exception=\"false\" avgLength=\"5.6169228346322635\" minLength=\"5\" maxLength=\"6\" nullCount=\"0\" distinctValueCount=\"234937\">" +
        "   <avgValue type=\"java.math.BigDecimal\" value=\"127470.085669775301\"/>" +
        "   <maxValue type=\"java.lang.Integer\" value=\"500001\"/>" +
        "   <minValue type=\"java.lang.Integer\" value=\"10001\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"10001\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26384\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26383\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26382\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26381\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26380\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26379\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26378\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26377\"/>" +
        "   <topNvalue count=\"1\" type=\"java.lang.Integer\" value=\"26376\"/>" +
        "  </profile-result>" +
        " </profiles>" +
        "</architect-project>";
    
    /**
     * A special stub context that can create a real ArchitectSwingSessionImpl,
     * which is what we are testing.
     */
    class StubContext extends TestingArchitectSwingSessionContext {

        public StubContext() throws IOException {
            super();
        }

        @Override
        public ArchitectSwingSession createSession(boolean showGUI) throws ArchitectException {
            return new ArchitectSwingSessionImpl(this, "testing");
        }
    }
    
    /**
     * Closing a session which has not had its frame created used to fail with
     * NPE. This is the regression test for that.
     */
    public void testCloseSessionWithoutGUI() throws Exception {
        ArchitectSwingSessionContext context = new StubContext();
        ArchitectSwingSession session = context.createSession(false);
        session.close();
    }
    
    /**
     * Test to ensure that the isNew property starts off as true, and then
     * ensure that it gets updated to be false after loading a project.
     */
    public void testIsNewFalseAfterProjectLoad() throws Exception {
        ArchitectSwingSessionContext context = new StubContext();
        ArchitectSwingSession session = context.createSession(false);
        assertTrue(session.isNew());
        
        ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
        session.getProject().load(r, new PlDotIni());
        assertFalse(session.isNew());
    }
    
    /**
     * Test to ensure that the isNew property starts off as true, and then
     * ensure that it gets updated to be false after a SQLObject event is fired.
     */
    public void testIsNewFalseAfterSQLObjectEvent() throws Exception {
        ArchitectSwingSessionContext context = new StubContext();
        ArchitectSwingSessionImpl session = (ArchitectSwingSessionImpl)context.createSession(false);
        assertTrue(session.isNew());
        
        SQLObjectEvent e = new SQLObjectEvent(new SQLDatabase(), "test");
        session.getProjectModificationWatcher().dbObjectChanged(e);
        assertFalse(session.isNew());
    }
    
    public void testRelationshipLinePrefUpdatesRelationships() throws Exception {
        ArchitectSwingSessionContext context = new StubContext();
        ArchitectSwingSessionImpl session = (ArchitectSwingSessionImpl)context.createSession(false);
        
        SQLDatabase db = new SQLDatabase();
        SQLTable t1 = new SQLTable(db, true);
        SQLTable t2 = new SQLTable(db, true);
        db.addChild(t1);
        db.addChild(t2);
        SQLRelationship sr = new SQLRelationship();
        sr.attachRelationship(t1, t2, false);
        
        session.getPlayPen().addTablePane(new TablePane(t1, session.getPlayPen()), new Point(0,0));
        session.getPlayPen().addTablePane(new TablePane(t2, session.getPlayPen()), new Point(0,0));
        
        Relationship r = new Relationship(session.getPlayPen(), sr);
        session.getPlayPen().addRelationship(r);
        
        session.setRelationshipLinesDirect(true);
        assertTrue(r.isStraightLine());
        
        session.setRelationshipLinesDirect(false);
        assertFalse(r.isStraightLine());
    }
    
    public void testSaveAndLoadRelationshipLineType() throws Exception {
        ArchitectSwingSessionContext context = new StubContext();
        ArchitectSwingSession session = context.createSession(false);
        session.getProject().load(new ByteArrayInputStream(testData.getBytes()), new PlDotIni());
        
        boolean newValueForStraightLines = !session.getRelationshipLinesDirect();
        session.setRelationshipLinesDirect(newValueForStraightLines);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        session.getProject().save(out, "utf-8");
        
        ArchitectSwingSession loadedSession = context.createSession(false);
        loadedSession.getProject().load(new ByteArrayInputStream(out.toByteArray()), new PlDotIni());
        assertEquals(newValueForStraightLines, loadedSession.getRelationshipLinesDirect());
        
        for (Relationship r : loadedSession.getPlayPen().getRelationships()) {
            assertEquals(newValueForStraightLines, r.isStraightLine());
        }
    }
}
