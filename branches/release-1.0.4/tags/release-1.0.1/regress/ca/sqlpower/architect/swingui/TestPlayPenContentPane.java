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

import java.awt.Point;

import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.testutil.NewValueMaker;

public class TestPlayPenContentPane extends PersistedSPObjectTest {

    Relationship rel1;
	Relationship rel2;
	Relationship rel3;
	Relationship rel4;
	TablePane tp1;
	TablePane tp2;
	TablePane tp3;
	PlayPenContentPane ppcp;
	PlayPen pp;
	
	public TestPlayPenContentPane(String name) {
	    super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		super.setUp(); // Is this REALLY necessary??
        
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		pp = session.getPlayPen();
		SQLTable t1 = new SQLTable(session.getTargetDatabase(), true);
		session.getTargetDatabase().addChild(t1);
		tp1 = new TablePane(t1,pp.getContentPane());
		pp.addTablePane(tp1, new Point(0,-10));
		SQLTable t2 = new SQLTable(session.getTargetDatabase(), true);
		session.getTargetDatabase().addChild(t2);
		tp2 = new TablePane(t2, pp.getContentPane());
		pp.addTablePane(tp2, new Point(-10,0));
		SQLRelationship sqlrel = new SQLRelationship();
		sqlrel.attachRelationship(t1, t2, false);
		final SQLDatabase modelContainer = new SQLDatabase();
		getRootObject().addChild(modelContainer, 0);
        ppcp = new PlayPenContentPane(modelContainer);
		ppcp.setPlayPen(pp);
		rel1 = new Relationship(sqlrel,pp.getContentPane());
		rel2 = new Relationship(sqlrel,pp.getContentPane());
		rel3 = new Relationship(sqlrel,pp.getContentPane());
		rel4 = new Relationship(sqlrel,pp.getContentPane());
		
		tp3 = new TablePane(t1,pp.getContentPane());
		
		session.getWorkspace().setPlayPenContentPane(ppcp);
		getRootObject().addChild(session.getWorkspace(), 0);
	}

    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return PlayPenComponent.class;
    }

    @Override
    public SPObject getSPObjectUnderTest() {
        return ppcp;
    }
    
    public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }

}
