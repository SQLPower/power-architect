package ca.sqlpower.architect.swingui;

import java.awt.Point;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

public class TestRelationship extends TestCase {

	Relationship rel;
	PlayPen pp;
	
	protected void setUp() throws Exception {
		super.setUp();
		pp = ArchitectFrame.getMainInstance().getProject().getPlayPen();
		SQLTable t1 = new SQLTable(pp.getDatabase(), true);
		pp.getDatabase().addChild(t1);
		pp.addTablePane(new TablePane(t1, pp), new Point(0,0));
		SQLTable t2 = new SQLTable(pp.getDatabase(), true);
		pp.getDatabase().addChild(t2);
		pp.addTablePane(new TablePane(t2, pp), new Point(0,0));
		SQLRelationship sqlrel = new SQLRelationship();
		sqlrel.attachRelationship(t1, t2, false);
		rel = new Relationship(pp, sqlrel);
	}
	
	public void testCopyConstructor() {
		PlayPen newpp = new PlayPen(pp.getDatabase());
		Relationship rel2 = new Relationship(rel, newpp.getContentPane(), null, null);
		assertNotSame("The new relationship component has the same UI delegate as the original", rel.getUI(), rel2.getUI());
	}

}
