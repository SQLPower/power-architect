package regress.ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.event.ActionEvent;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.layout.SpringForce;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;

public class TestSpringForce extends TestCase {

	AutoLayoutAction action;
	SpringForce layout; 
	PlayPen pp;
	protected void setUp() throws Exception {
		super.setUp();
		layout = new SpringForce();
		action = new AutoLayoutAction();

		action.setLayout(layout);
		
		pp = new PlayPen(new SQLDatabase());
		action.setPlayPen(pp);
	}

	public void testNoOverlaps() throws ArchitectException {
		 pp = action.getPlayPen();
		SQLDatabase ppdb = pp.getDatabase();
		SQLTable t1 = new SQLTable(ppdb, "This is the name of the first table", "", "TABLE", true);
		SQLTable t2 = new SQLTable(ppdb, "This table is way cooler than the first one", "", "TABLE", true);
		
		TablePane tp1 = new TablePane(t1, pp);
		TablePane tp2 = new TablePane(t2, pp);
		
		pp.addTablePane(tp1, new Point(10,10));
		pp.addTablePane(tp2, new Point(20,20));
		
		// they start off overlapping
		assertTrue(tp1.getBounds().intersects(tp2.getBounds()));
		
		action.actionPerformed(new ActionEvent(this, 0, null));
		
		// they end up separated
		assertFalse(tp1.getBounds().intersects(tp2.getBounds()));
	}

	public void testStraitAbove() throws ArchitectException
	{
		pp = action.getPlayPen();
		SQLDatabase ppdb = pp.getDatabase();
		SQLTable t1 = new SQLTable(ppdb, "This is the name of the first table", "", "TABLE", true);
		SQLTable t2 = new SQLTable(ppdb, "This table is way cooler than the first one", "", "TABLE", true);
		
		TablePane tp1 = new TablePane(t1, pp);
		TablePane tp2 = new TablePane(t2, pp);
		
		pp.addTablePane(tp1, new Point(10,10));
		pp.addTablePane(tp2, new Point(10,20));
		
		// they start off overlapping
		assertTrue(tp1.getBounds().intersects(tp2.getBounds()));
		
		action.actionPerformed(new ActionEvent(this, 0, null));
		
		// they end up separated
		assertFalse(tp1.getBounds().intersects(tp2.getBounds()));
}

	public void testOntop() throws ArchitectException
	{
		 pp = action.getPlayPen();
		SQLDatabase ppdb = pp.getDatabase();
		SQLTable t1 = new SQLTable(ppdb, "This is the name of the first table", "", "TABLE", true);
		SQLTable t2 = new SQLTable(ppdb, "This table is way cooler than the first one", "", "TABLE", true);
		
		TablePane tp1 = new TablePane(t1, pp);
		TablePane tp2 = new TablePane(t2, pp);
		
		pp.addTablePane(tp1, new Point(10,10));
		pp.addTablePane(tp2, new Point(10,10));
		
		// they start off overlapping
		assertTrue(tp1.getBounds().intersects(tp2.getBounds()));
		
		action.actionPerformed(new ActionEvent(this, 0, null));
		
		// they end up separated
		assertFalse(tp1.getBounds().intersects(tp2.getBounds()));
}
	
	public void testMoveToZero() throws ArchitectException
	{	
		pp = action.getPlayPen();
		SQLDatabase ppdb = pp.getDatabase();
		SQLTable t1 = new SQLTable(ppdb, "This is the name of the first table", "", "TABLE", true);
		
		
		TablePane tp1 = new TablePane(t1, pp);
		
		
		pp.addTablePane(tp1, new Point(10,10));
		
		action.actionPerformed(new ActionEvent(this, 0, null));
		
		// they end up separated
		assertFalse(tp1.getLocation().equals(new Point()));
}
	/*
	 * Test method for 'ca.sqlpower.architect.layout.SpringForce.SpringForce()'
	 */
	public void testSpringForce() {
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.layout.SpringForce.setup(List<TablePane>, List<Relationship>)'
	 */
	public void testSetup() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.layout.SpringForce.isDone()'
	 */
	public void testIsDone() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.layout.SpringForce.nextFrame()'
	 */
	public void testNextFrame() {

	}

}
