package regress.ca.sqlpower.architect.swingui;

import java.awt.Point;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUIProject;
import ca.sqlpower.architect.swingui.TablePane;

public class TestPlayPen extends TestCase {
	ArchitectFrame af;
	private PlayPen pp;
	private SQLDatabase ppdb;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		af = ArchitectFrame.getMainInstance();
		af.setProject(new SwingUIProject( "Undo Project"));
		pp = af.getProject().getPlayPen();
		ppdb = pp.getDatabase();
		
	}
	
	public void testUndoAddTable() throws ArchitectException {
		SQLTable t = new SQLTable(ppdb, "test_me", "", "TABLE", true);
		
		TablePane tp = new TablePane(t, pp);
		ppdb.addChild(t);

		pp.addTablePane(tp, new Point(99,98));


		
		// this isn't the point of the test, but adding the tablepane has to work!
		assertNotNull(pp.findTablePane(t));	
		
		//Undo the add child and the move table pane
		af.getUndoManager().undo();
		
		assertNull(pp.findTablePane(t));
	}

	public void testRedoAddTable() throws ArchitectException {
		SQLTable t = new SQLTable(ppdb, "test_me", "", "TABLE", true);
		
		TablePane tp = new TablePane(t, pp);
		
		ppdb.addChild(t);
		pp.addTablePane(tp, new Point(99,98));
		
		// this isn't the point of the test, but adding the tablepane has to work!
		assertNotNull(ppdb.getTableByName("test_me"));
		assertNotNull(pp.findTablePane(t));
		//undo the add child and the move table pane
		System.out.println("Undo action is "+af.getUndoManager().getUndoPresentationName());
		af.getUndoManager().undo();
		System.out.println("After undo, undo action is "+af.getUndoManager().getUndoPresentationName());
		assertNull(ppdb.getTableByName("test_me"));
		assertNull(pp.findTablePane(t));
		// redo the add table and the move
		af.getUndoManager().redo();
		tp = pp.findTablePane(t);
		assertNotNull("Table pane didn't come back!", tp);
		assertEquals("Table came back, but in wrong location",
				new Point(99,98), tp.getLocation());
	}

}
