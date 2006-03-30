package ca.sqlpower.architect.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;

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
		
		for (int ii = 0; ii < changed.length; ii++) {
			if (children[ii] instanceof SQLRelationship) {
				SQLRelationship rel = (SQLRelationship) children[ii];
				int size =rel.getChildren().size();
			    for(int jj = 0; jj < size ; jj++){
					rel.removeChild(0);
				}
				rel.attachRelationship(rel.getPkTable(), rel.getFkTable());
			} else {
				sqlObject.addChild(changed[ii], children[ii]);
			}
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
