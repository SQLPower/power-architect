package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class DeleteSelectedAction extends AbstractAction implements SelectionListener {
	private static final Logger logger = Logger.getLogger(DeleteSelectedAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public DeleteSelectedAction() {
		super("Delete Selected",
			  ASUtils.createJLFIcon("general/Delete",
								 "Delete Selected",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Delete Selected");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)); // XXX: how to attach to components?
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelection();
		if (invoker instanceof TablePane) {
			TablePane tp = (TablePane) invoker;
			int colidx;
			if ( (colidx = tp.getSelectedColumnIndex()) >= 0) {
				// a column in the selected table
				try {
					tp.getModel().removeColumn(colidx);
				} catch (LockedColumnException ex) {
					JOptionPane.showMessageDialog((JComponent) invoker, ex.getMessage());
				}
			} else {
				// the whole table
				pp.db.removeChild(tp.getModel());
			}
		} else if (invoker instanceof Relationship) {
			Relationship r = (Relationship) invoker;
			SQLRelationship sr = r.getModel();
			sr.getPkTable().removeExportedKey(sr);
			sr.getFkTable().removeImportedKey(sr);
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
		}
	}
	
	public void setPlayPen(PlayPen newPP) {
		if (pp != null) {
			pp.removeSelectionListener(this);
		}
		pp = newPP;
		pp.addSelectionListener(this);
	}
	
	public void itemSelected(SelectionEvent e) {
		Selectable item = e.getSelectedItem();
		if (item.isSelected()) {
			setEnabled(true);
			String name = "Selected";
			if (item instanceof TablePane) {
				TablePane tp = (TablePane) item;
				if (tp.getSelectedColumnIndex() >= 0) {
					try {
						name = tp.getModel().getColumn(tp.getSelectedColumnIndex()).getName();
					} catch (ArchitectException ex) {
						logger.error("Couldn't get selected column name", ex);
					}
				} else {
					name = tp.getModel().toString();
				}
			} else if (item instanceof Relationship) {
				name = ((Relationship) item).getModel().getName();
			}
			putValue(SHORT_DESCRIPTION, "Delete "+name);
			putValue(NAME, "Delete "+name);
		} else {
			setEnabled(false);
			putValue(SHORT_DESCRIPTION, "Delete Selected");
			putValue(NAME, "Delete Selected");
		}
	}
}
