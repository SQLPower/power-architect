package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import ca.sqlpower.architect.*;
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
			   ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
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
		logger.debug("Starting to create relationship!");
		active = true;
		pp.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		// gets over the "can't select a selected item"
		pp.selectNone();
	}

	protected void doCreateRelationship() {
		try {
			Relationship r = new Relationship(pp, pkTable, fkTable, identifying);
			pp.add(r);
			r.repaint();  // XXX: shouldn't be necessary, but it is.
		} catch (ArchitectException ex) {
			logger.error("Couldn't create relationship", ex);
			JOptionPane.showMessageDialog(pp, "Couldn't create relationship: "+ex.getMessage());
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
		// what address am I?
		logger.debug("00000000000 object hash code: " + super.hashCode());

	
		// ignore events unless active
		logger.debug("11111111ITEM SELECTED: " + e);

		if (!active) {
			logger.debug("222222222 not active.");
			return;
		}

		Selectable s = e.getSelectableSource();

		// don't care when tables (or anything else) are deselected
		if (!s.isSelected()) {
			logger.debug("333333333 not selected.");			
			return;
		}

		if (s instanceof TablePane) {
			logger.debug("4444444444444 instance of TablePane.");						
			if (pkTable == null) {
				pkTable = (TablePane) s;
				logger.debug("555555555555 Creating relationship: PK Table is "+pkTable);
			} else {
				fkTable = (TablePane) s;
				logger.debug("66666666666666 Creating relationship: FK Table is "+fkTable);
				doCreateRelationship();
				pp.setCursor(null);
				active = false;
			}
		} else {
			logger.debug("777777777777 not instance of TablePane.");						
			if (logger.isDebugEnabled())
				logger.debug("The user clicked on a non-table component: "+s);
		}
	}

	public void itemDeselected(SelectionEvent e) {
		// don't particularly care
	}
}
