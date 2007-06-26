package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.TablePane;

public class TestFruchtermanReingoldForceLayout extends TestCase {

	private PlayPen pp;
	private TablePane tp;
	private SQLTable table1;
	private SQLDatabase db;
	private Rectangle frame;
	private FruchtermanReingoldForceLayout layout;
    private TestingArchitectSwingSessionContext context;
	
	public void setUp() throws ArchitectException, IOException {
	    context = new TestingArchitectSwingSessionContext();
		db = new SQLDatabase();
		pp = new PlayPen(context.createSession());
		table1= new SQLTable(db,true);
		tp = new TablePane(table1,pp);
		pp.addTablePane(tp,new Point(10,10));
		layout = new FruchtermanReingoldForceLayout();
		frame = new Rectangle(new Point(),layout.getNewArea(pp.getTablePanes()));
	}
	
	public void testMagnitude() {
		assertEquals("Incorrect magnitude",5.0,layout.magnitude(new Point(3,4)));
		assertEquals("Incorrect magnitude",13.0,layout.magnitude(new Point(12,5)));
	}

	public void testIsDoneNoElem() throws ArchitectException {
		PlayPen p = new PlayPen(context.createSession());
		layout.setup(p.getTablePanes(),p.getRelationships(),frame);
		assertTrue(layout.isDone());
	}
	
	public void testIsDoneOneElem() {
		layout.setup(pp.getTablePanes(),pp.getRelationships(),frame);
		assertTrue(layout.isDone());
	}
	
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
