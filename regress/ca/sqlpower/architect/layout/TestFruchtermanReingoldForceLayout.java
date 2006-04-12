package regress.ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TablePane;

public class TestFruchtermanReingoldForceLayout extends TestCase {

	private PlayPen pp;
	private TablePane tp;
	private SQLTable table1;
	private SQLDatabase db;
	private Rectangle frame;
	private FruchtermanReingoldForceLayout layout;
	
	public void setUp() throws ArchitectException {
	
		db = new SQLDatabase();
		pp = new PlayPen();
		table1= new SQLTable(db,true);
		tp = new TablePane(table1,pp);
		pp.addTablePane(tp,new Point(10,10));
		layout = new FruchtermanReingoldForceLayout();
		layout.setPlayPen(pp);
		frame = new Rectangle(new Point(),layout.getNewArea(pp.getTablePanes()));
		
	}
	

	/*
	 * Test method for 'ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout.magnitude(Point)'
	 */
	public void testMagnitude() {
		assertEquals("Incorrect magnitude",5.0,layout.magnitude(new Point(3,4)));
		assertEquals("Incorrect magnitude",13.0,layout.magnitude(new Point(12,5)));
	}

	/*
	 * Test method for 'ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout.isDone()'
	 */
	public void testIsDoneNoElem() {
		PlayPen p = new PlayPen();
		layout.setPlayPen(p);
		layout.setup(p.getTablePanes(),p.getRelationships(),frame);
		assertTrue(layout.isDone());
	}
	
	public void testIsDoneOneElem() {
		layout.setup(pp.getTablePanes(),pp.getRelationships(),frame);
		assertTrue(layout.isDone());
	}
	

	/*
	 * Test method for 'ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout.nextFrame()'
	 */
	public void testNextFrame() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout.attractiveForce(double, double)'
	 */
	public void testAttractiveForce() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout.repulsiveForce(double, double)'
	 */
	public void testRepulsiveForce() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout.getEmptyRadius(PlayPen)'
	 */
	public void testGetEmptyRadius() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout.done()'
	 */
	public void testDone() throws ArchitectException {
		SQLTable sqlTable2 = new SQLTable(db,true);
		TablePane t2 =new TablePane(sqlTable2,pp);
		pp.addTablePane(t2,new Point(23,243));
		layout.setup(pp.getTablePanes(),pp.getRelationships(),frame);
		assertFalse(layout.isDone());
		layout.done();
		assertTrue(layout.isDone());
	}

	

}
