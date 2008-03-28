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
		t = new SQLTable(session.getTargetDatabase(), true);
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
