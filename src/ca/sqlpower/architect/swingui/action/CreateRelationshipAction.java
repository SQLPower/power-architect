package ca.sqlpower.architect.swingui.action;

import java.awt.event.*;
import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import ca.sqlpower.architect.*;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

import org.apache.log4j.Logger;

public class CreateRelationshipAction extends AbstractAction
	implements ActionListener, SelectionListener {

	private static final Logger logger = Logger.getLogger(CreateRelationshipAction.class);

	protected boolean identifying;
	protected PlayPen pp;
	protected TablePane pkTable;
	protected TablePane fkTable;

	/**
	 * This property is true when we are actively creating a
	 * relationship.  The original implementation was to add and
	 * remove this action from the playpen selection listener list,
	 * but it caused ConcurrentModificationException.
	 */
	protected boolean active;

	public CreateRelationshipAction(boolean identifying) {
		super(identifying ? "New Identifying Relationship" : "New Non-Identifying Relationship",
			  ASUtils.createIcon
			  (identifying ? "NewIdentifyingRelationship" : "NewNonIdentifyingRelationship",
			   "New Relationship",
			   ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		if (identifying) {
			putValue(SHORT_DESCRIPTION, "New Identifying Relationship");
		} else {
			putValue(SHORT_DESCRIPTION, "New Non-Identifying Relationship");
		}
		this.identifying = identifying;
		setEnabled(false);
		logger.debug("(constructor) hashcode is: " + super.hashCode());
	}

	public void actionPerformed(ActionEvent evt) {
		logger.debug("the hashcode is: " + super.hashCode());
		pkTable = null;
		fkTable = null;
		logger.debug("Starting to create relationship, setting active to TRUE!");
		active = true;
		pp.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		// gets over the "can't select a selected item"
		pp.selectNone();
	}

	static public void doCreateRelationship(SQLTable pkTable,SQLTable fkTable,PlayPen pp, boolean identifying) {
		try {
			pp.fireUndoCompoundEvent(new UndoCompoundEvent(pkTable,EventTypes.RELATIONSHIP_START,"Starting the creation of a relationship"));
			SQLRelationship model = new SQLRelationship();
			// XXX: need to ensure uniqueness of setName(), but 
			// to_identifier should take care of this...			
			model.setName(pkTable.getName()+"_"+fkTable.getName()+"_fk"); 
			model.setIdentifying(identifying);
			fkTable.setSecondaryChangeMode(true);
			model.attachRelationship(pkTable,fkTable,true);
			
			Relationship r = new Relationship(pp, model);
			pp.addRelationship(r);
			r.revalidate();
		} catch (ArchitectException ex) {
			
			logger.error("Couldn't create relationship", ex);
			JOptionPane.showMessageDialog(pp, "Couldn't create relationship: "+ex.getMessage());
		} finally {
			fkTable.setSecondaryChangeMode(false);
			pp.fireUndoCompoundEvent(new UndoCompoundEvent(pkTable,EventTypes.RELATIONSHIP_END,"Ending the creation of a relationship"));
		}
	}
	
	
	
	public void setPlayPen(PlayPen playpen) {
		if (pp != null) {
			pp.removeSelectionListener(this);
		}
		pp = playpen;
		if (pp != null) {
			pp.addSelectionListener(this);
			setEnabled(true);
		} else {
			setEnabled(false);
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
				doCreateRelationship(pkTable.getModel(),fkTable.getModel(),pp,identifying);  // this might fail, but still set things back to "normal"
				pp.setCursor(null);
				active = false;
			}
		} else {
			if (logger.isDebugEnabled())
				logger.debug("The user clicked on a non-table component: "+s);
		}
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
}
