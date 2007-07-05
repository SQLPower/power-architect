/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.PlayPen.CancelableListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

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

	public CreateRelationshipAction(ArchitectSwingSession session, boolean identifying) {
        super(session, 
              identifying ? "New Identifying Relationship" : "New Non-Identifying Relationship",
              identifying ? "New Identifying Relationship": "New Non-Identifying Relationship",
              identifying ? "new_id_relationship" : "new_nonid_relationship");
        
		if (identifying) {
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_R,0));
		} else {
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.SHIFT_MASK));
		}
		this.identifying = identifying;
		logger.debug("(constructor) hashcode is: " + super.hashCode());
        
        if (this.playpen != null) {
            this.playpen.addSelectionListener(this);
            this.playpen.addCancelableListener(this);
            setEnabled(true);
        } else {
            setEnabled(false);
        }
	}

	public void actionPerformed(ActionEvent evt) {
		playpen.fireCancel();
		pkTable = null;
		fkTable = null;
		logger.debug("Starting to create relationship, setting active to TRUE!");
		active = true;
		playpen.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		// gets over the "can't select a selected item"
		playpen.selectNone();
	}

	static public void doCreateRelationship(SQLTable pkTable, SQLTable fkTable, PlayPen pp, boolean identifying) {
		try {
			pp.startCompoundEdit("Add Relationship");
			fkTable.setMagicEnabled(false);
			SQLRelationship model = new SQLRelationship();
			// XXX: need to ensure uniqueness of setName(), but 
			// to_identifier should take care of this...			
			model.setName(pkTable.getName()+"_"+fkTable.getName()+"_fk"); 
			model.setIdentifying(identifying);
			model.attachRelationship(pkTable,fkTable,true);
			
			Relationship r = new Relationship(pp, model);
			pp.addRelationship(r);
			r.revalidate();
		} catch (ArchitectException ex) {
			logger.error("Couldn't create relationship", ex);
			JOptionPane.showMessageDialog(pp, "Couldn't create relationship: "+ex.getMessage());
		} finally {
			fkTable.setMagicEnabled(true);
			pp.endCompoundEdit("Ending the creation of a relationship");
		}
	}

	// -------------------- SELECTION EVENTS --------------------
	
	public void itemSelected(SelectionEvent e) {
	
		if (!active) return;

		Selectable s = e.getSelectableSource();

		// don't care when tables (or anything else) are deselected
		if (!s.isSelected()) return;

		if (s instanceof TablePane) {
			if (pkTable == null) {
				pkTable = (TablePane) s;
				logger.debug("Creating relationship: PK Table is "+pkTable);
			} else {
				fkTable = (TablePane) s;
				logger.debug("Creating relationship: FK Table is "+fkTable);
				try {
					doCreateRelationship(pkTable.getModel(),fkTable.getModel(),playpen,identifying);  // this might fail, but still set things back to "normal"
				} finally {
					resetAction();
				}
			}
		} else {
			if (logger.isDebugEnabled())
				logger.debug("The user clicked on a non-table component: "+s);
		}
	}

	private void resetAction() {
		pkTable = null;
		fkTable = null;
		playpen.setCursor(null);
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
