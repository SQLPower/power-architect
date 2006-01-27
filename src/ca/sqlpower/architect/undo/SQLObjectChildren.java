package ca.sqlpower.architect.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;

public abstract class SQLObjectChildren extends AbstractUndoableEdit {

	protected SQLObjectEvent e;
	protected String toolTip;
	
	public SQLObjectChildren() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void createEditFromEvent(SQLObjectEvent event){
		
		e = event;
		createToolTip();
		
	}
	public abstract void createToolTip();
	
	public void removeChildren(){
		int changed[] =e.getChangedIndices();
		SQLObject sqlObject= e.getSQLSource();
		for (int ii = 0; ii < changed.length;ii++)
		{
			sqlObject.removeChild(changed[ii]);
		}
	}
	
	
	public void addChildren() throws ArchitectException {
	
		int changed[] = e.getChangedIndices();
		SQLObject sqlObject= e.getSQLSource();
		SQLObject children[] = e.getChildren();
		
		for (int ii = 0; ii < changed.length;ii++)
		{
			sqlObject.addChild(changed[ii],children[ii]);
		}
	}
	
	
	@Override
	public boolean canRedo() {
		return true;
	}
	
	@Override
	public boolean canUndo() {
		return true;
	}
	
	@Override
	public String getPresentationName() {
		
		return toolTip;
	}
	
}
