package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
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
	}

	public void actionPerformed(ActionEvent evt) {
		pkTable = null;
		fkTable = null;
		logger.debug("Starting to create relationship!");
		active = true;
		pp.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
		// ignore events unless active
		if (!active) return;

		Selectable s = e.getSelectedItem();

		// don't care when tables (or anything else) are deselected
		if (!s.isSelected()) return;

		if (s instanceof TablePane) {
			if (pkTable == null) {
				pkTable = (TablePane) s;
				logger.debug("Creating relationship: PK Table is "+pkTable);
			} else {
				fkTable = (TablePane) s;
				logger.debug("Creating relationship: FK Table is "+fkTable);
				doCreateRelationship();
				pp.setCursor(null);
				active = false;
			}
		} else {
			logger.debug("The user clicked on a non-table component: "+s);
		}
	}
}
