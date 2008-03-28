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

import java.sql.Types;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class TestTableEditPane extends TestCase {

	private SQLTable t;	
	private TableEditPanel tep;
	
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		t = new SQLTable(session.getPlayPen().getDatabase(), true);
		t.setName("Test Table");
		SQLColumn pk1 = new SQLColumn(t, "PKColumn1", Types.INTEGER, 10,0);
		
		t.addColumn(0,pk1);						
		pk1.setPrimaryKeySeq(1);

        tep = new TableEditPanel(session, t);
	}
	
    public void testChangeName(){           
        tep.setNameText("New Name");
        tep.applyChanges();
        assertEquals ("New Name", t.getName());     
    }

    public void testNameChangeUpdatesPk() throws Exception {
        assertEquals("Test Table_pk", t.getPrimaryKeyName());
        tep.setNameText("New Name");
        tep.applyChanges();
        assertEquals ("New Name_pk", t.getPrimaryKeyName());     
    }

    public void testNameChangeDoesNotUpdatePkWhenPkNameAlsoChanged() throws Exception {
        assertEquals("Test Table_pk", t.getPrimaryKeyName());
        tep.setNameText("New Name");
        tep.setPkNameText("New PK Name");
        tep.applyChanges();
        assertEquals ("New PK Name", t.getPrimaryKeyName());     
    }

}
