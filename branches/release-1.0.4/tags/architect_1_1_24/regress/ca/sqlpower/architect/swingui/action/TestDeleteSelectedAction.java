package ca.sqlpower.architect.swingui.action;

import java.awt.Point;
import java.sql.Types;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;

public class TestDeleteSelectedAction extends TestCase {
	
	private DeleteSelectedAction deleteAction;
	private PlayPen pp;
	private TablePane tp;
	private Relationship r;
	private TablePane tp2;
	
	protected void setUp() throws Exception {
		super.setUp();
		deleteAction = new DeleteSelectedAction();
		pp = new PlayPen();
		deleteAction.setPlayPen(pp);
		tp = new TablePane(new SQLTable(pp.getDatabase(),true),pp);
		tp.getModel().setName("Table1");
		tp.getModel().addColumn(new SQLColumn(tp.getModel(),"col1",Types.INTEGER,1,1));
		tp.getModel().addColumn(new SQLColumn(tp.getModel(),"col2",Types.INTEGER,1,1));
		
		tp2 = new TablePane(new SQLTable(pp.getDatabase(),true),pp);
		tp2.getModel().setName("Table2");
		tp2.getModel().addColumn(new SQLColumn(tp.getModel(),"col1",Types.INTEGER,1,1));
		tp2.getModel().addColumn(new SQLColumn(tp.getModel(),"col2",Types.INTEGER,1,1));
		
		SQLRelationship sqlRelationship = new SQLRelationship();
		pp.addTablePane(tp,new Point());
		pp.addTablePane(tp2, new Point());
		sqlRelationship.attachRelationship(tp.getModel(),tp.getModel(),false);
		r = new Relationship(pp,sqlRelationship);
		pp.addRelationship(r);
		
	}

	public void testTableSelected() throws ArchitectException{
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		tp.setSelected(true);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT));
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("Incorrect Tooltip", "Delete Table1",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp.setSelected(false);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.DESELECTION_EVENT));
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
	}
	
	public void testRelationshipSelected() {
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		r.setSelected(true);
		deleteAction.itemSelected(new SelectionEvent(r, SelectionEvent.SELECTION_EVENT));
		assertTrue("Action not enabled", deleteAction.isEnabled());
		r.setSelected(false);
		deleteAction.itemSelected(new SelectionEvent(r, SelectionEvent.DESELECTION_EVENT));
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
	}
	
	public void testTableAndRelationshipSelected() {
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		r.setSelected(true);
		deleteAction.itemSelected(new SelectionEvent(r, SelectionEvent.SELECTION_EVENT));
		tp.setSelected(true);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT));
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("Delete 2 items",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		r.setSelected(false);
		deleteAction.itemSelected(new SelectionEvent(r, SelectionEvent.DESELECTION_EVENT));
		assertTrue("Action not enabled when we still have an enabled component", deleteAction.isEnabled());
		tp.setSelected(false);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.DESELECTION_EVENT));
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
	}
	
	public void testColumnSelected() throws ArchitectException{
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		tp.setSelected(true);
		tp.selectColumn(0);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT));
		assertEquals("Delete col1",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp.selectColumn(1);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT));
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("tooltip incorrect for two selected columns","Delete 2 items",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp.selectNone();
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.DESELECTION_EVENT));
		assertTrue("Action not enable when columns unselected, but table selected",deleteAction.isEnabled());		
	}
	
	public void testTableAndColumnSelected(){
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		tp.setSelected(true);
		tp.selectColumn(0);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT));
		tp2.setSelected(true);		
		deleteAction.itemSelected(new SelectionEvent(tp2, SelectionEvent.SELECTION_EVENT));
		assertEquals("Delete 2 items",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp2.setSelected(false);
		deleteAction.itemSelected(new SelectionEvent(tp2, SelectionEvent.DESELECTION_EVENT));
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("tooltip incorrect for two selected columns","Delete col1",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp.selectNone();
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.DESELECTION_EVENT));
		assertTrue("Action not enable when columns unselected, but table selected",deleteAction.isEnabled());
	}
	
	public void testTableAndRelationshipAndColumnSelected(){
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		r.setSelected(true);
		deleteAction.itemSelected(new SelectionEvent(r, SelectionEvent.SELECTION_EVENT));
		tp.setSelected(true);
		tp.selectColumn(0);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT));
		tp2.setSelected(true);
		deleteAction.itemSelected(new SelectionEvent(tp2, SelectionEvent.SELECTION_EVENT));
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("Delete 3 items",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		r.setSelected(false);
		deleteAction.itemSelected(new SelectionEvent(r, SelectionEvent.DESELECTION_EVENT));
		tp.setSelected(false);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.DESELECTION_EVENT));		
		assertTrue("Action not enabled when we still have an enabled component", deleteAction.isEnabled());
		assertEquals("Delete Table2", deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp2.setSelected(false);
		deleteAction.itemSelected(new SelectionEvent(tp, SelectionEvent.DESELECTION_EVENT));
		assertFalse ("Nothing is selected", deleteAction.isEnabled());
		
	}
}
