package ca.sqlpower.architect.swingui;

import java.awt.Point;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

public class TestPlayPenContentPane extends TestCase {

	Relationship rel1;
	Relationship rel2;
	Relationship rel3;
	Relationship rel4;
	TablePane tp1;
	TablePane tp2;
	TablePane tp3;
	PlayPenContentPane ppcp;
	PlayPen pp;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		
		super.setUp(); // Is this REALLY necessary??
        
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		pp = session.getPlayPen();
		SQLTable t1 = new SQLTable(pp.getDatabase(), true);
		pp.getDatabase().addChild(t1);
		tp1 = new TablePane(t1,pp);
		pp.addTablePane(tp1, new Point(0,-10));
		SQLTable t2 = new SQLTable(pp.getDatabase(), true);
		pp.getDatabase().addChild(t2);
		tp2 =new TablePane(t2, pp);
		pp.addTablePane(tp2, new Point(-10,0));
		SQLRelationship sqlrel = new SQLRelationship();
		sqlrel.attachRelationship(t1, t2, false);
		ppcp= new PlayPenContentPane(pp);
		rel1 = new Relationship(pp,sqlrel);
		rel2 = new Relationship(pp,sqlrel);
		rel3 = new Relationship(pp,sqlrel);
		rel4 = new Relationship(pp,sqlrel);
		
		tp3 = new TablePane(t1,pp);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenContentPane.getFirstRelationIndex()'
	 */
	public void testGetFirstRelationIndex() {
		assertEquals("The children list is not size 0",0,ppcp.getFirstRelationIndex());
		ppcp.add(rel1,0);
		assertEquals("The relation was added as a non-relation",0,ppcp.getFirstRelationIndex());
		ppcp.add(rel2,0);
		ppcp.add(tp1,0);
		assertEquals("There should be 1 non relations in the list",1,ppcp.getFirstRelationIndex());
		ppcp.remove(tp1);
		assertEquals("There should be 0 non relations in the list",0,ppcp.getFirstRelationIndex());
		ppcp.remove(rel2);
		assertEquals("Removing a relation should not effect the index of the first relation",0,ppcp.getFirstRelationIndex());
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenContentPane.getComponentCount()'
	 */
	public void testGetComponentCount() {

		assertEquals("The children list is not size 0",0,ppcp.getComponentCount());
		ppcp.add(rel1,0);
		assertEquals("The relation was not added to the list properly",1,ppcp.getComponentCount());
		ppcp.add(rel2,0);
		ppcp.add(tp1,0);
		assertEquals("There should be 3 components in the list",3,ppcp.getComponentCount());
		ppcp.remove(tp1);
		assertEquals("Removing a non relationship gives an incorrect component count",2,ppcp.getComponentCount());
		ppcp.remove(rel2);
		assertEquals("Removing a relation gives an incorrect component count",1,ppcp.getComponentCount());
	}

}
