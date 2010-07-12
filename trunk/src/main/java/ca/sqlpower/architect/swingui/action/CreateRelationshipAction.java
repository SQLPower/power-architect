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
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.PlayPen.CancelableListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

public class CreateRelationshipAction extends AbstractArchitectAction
	implements ActionListener, SelectionListener, CancelableListener {

	private static final Logger logger = Logger.getLogger(CreateRelationshipAction.class);

	protected boolean identifying;
	protected TablePane pkTable;
	protected TablePane fkTable;
	
	/**
	 * This property is true when we are actively creating a
	 * relationship.  The original implementation was to add and
	 * remove this action from the playpen selection listener list,
	 * but it caused ConcurrentModificationException.
	 */
	protected boolean active;

	public CreateRelationshipAction(ArchitectFrame frame, boolean identifying) {
        super(frame, 
              identifying ? Messages.getString("CreateRelationshipAction.createIdentifyingRelationshipActionName") : Messages.getString("CreateRelationshipAction.createNonIdentifyingRelationshipActionName"), //$NON-NLS-1$ //$NON-NLS-2$
              identifying ? Messages.getString("CreateRelationshipAction.createIdentifyingRelationshipActionDescription"): Messages.getString("CreateRelationshipAction.createNonIdentifyingRelationshipActionDescription"), //$NON-NLS-1$ //$NON-NLS-2$
              identifying ? "new_id_relationship" : "new_nonid_relationship"); //$NON-NLS-1$ //$NON-NLS-2$
        
		if (identifying) {
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_R,0));
		} else {
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.SHIFT_MASK));
		}
		this.identifying = identifying;
		logger.debug("(constructor) hashcode is: " + super.hashCode()); //$NON-NLS-1$
        
		frame.addSelectionListener(this);
		frame.addCancelableListener(this);
	}

	public void actionPerformed(ActionEvent evt) {
		getPlaypen().fireCancel();
		pkTable = null;
		fkTable = null;
		logger.debug("Starting to create relationship, setting active to TRUE!"); //$NON-NLS-1$
		active = true;
		getPlaypen().getCursorManager().placeModeStarted();
		//playpen.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		// gets over the "can't select a selected item"
		getPlaypen().selectNone();
	}

	static public void doCreateRelationship(SQLTable pkTable, SQLTable fkTable, PlayPen pp, boolean identifying) {
	    ArchitectSwingProject project = pp.getSession().getWorkspace(); 
		try {
		    project.begin("Adding relationship");
			pp.startCompoundEdit("Add Relationship"); //$NON-NLS-1$
			SQLRelationship model = SQLRelationship.createRelationship(pkTable, fkTable, identifying);
			
			Relationship r = new Relationship(model, pp.getContentPane());
			pp.addRelationship(r);
			r.revalidate();
			project.commit();
		} catch (SQLObjectException ex) {
			logger.error("Couldn't create relationship", ex); //$NON-NLS-1$
			ASUtils.showExceptionDialogNoReport(pp, Messages.getString("CreateRelationshipAction.couldNotCreateRelationship"), ex); //$NON-NLS-1$
			project.rollback("Couldn't create relationship");
		} catch (Throwable e) {
		    project.rollback("Couldn't create relationship");		    
		    throw new RuntimeException(e);
		} finally {
			pp.endCompoundEdit("Ending the creation of a relationship"); //$NON-NLS-1$
		}
	}

    public void itemSelected(SelectionEvent e) {
	
		if (!active) return;
		if (e.getMultiselectType() == SelectionEvent.PLAYPEN_SWITCH_MULTISELECT) return;

		Selectable s = e.getSelectableSource();

		// don't care when tables (or anything else) are deselected
		if (!s.isSelected()) return;

		if (s instanceof TablePane) {
			if (pkTable == null) {
				pkTable = (TablePane) s;
				logger.debug("Creating relationship: PK Table is "+pkTable); //$NON-NLS-1$
			} else {
				fkTable = (TablePane) s;
				logger.debug("Creating relationship: FK Table is "+fkTable); //$NON-NLS-1$
				try {
					doCreateRelationship(pkTable.getModel(),fkTable.getModel(),getPlaypen(),identifying);  // this might fail, but still set things back to "normal"
				} finally {
					resetAction();
				}
			}
		} else {
			if (logger.isDebugEnabled())
				logger.debug("The user clicked on a non-table component: "+s); //$NON-NLS-1$
		}
	}

	private void resetAction() {
		pkTable = null;
		fkTable = null;
		getPlaypen().getCursorManager().placeModeFinished();
		active = false;
	}

	public void itemDeselected(SelectionEvent e) {
		// don't particularly care
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void cancel() {
		resetAction();	
	}
}
