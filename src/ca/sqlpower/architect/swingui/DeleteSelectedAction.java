package ca.sqlpower.architect.swingui;

import java.util.List;
import java.util.Iterator;
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
		List items = pp.getSelectedItems();
		if (items.size() > 1) {
			int decision = JOptionPane.showConfirmDialog(pp,
														 "Are you sure you want to delete the "
														 +items.size()+" selected items?",
														 "Multiple Delete",
														 JOptionPane.YES_NO_OPTION);
			if (decision == JOptionPane.NO_OPTION) {
				return;
			}
		}

		Iterator it = items.iterator();
		while (it.hasNext()) {
			Selectable item = (Selectable) it.next();
			if (item instanceof TablePane) {
				TablePane tp = (TablePane) item;
				int colidx;
				if ( (colidx = tp.getSelectedColumnIndex()) >= 0) {
					// a column in the selected table
					try {
						tp.getModel().removeColumn(colidx);
					} catch (LockedColumnException ex) {
						JOptionPane.showMessageDialog((JComponent) item, ex.getMessage());
					}
				} else {
					// the whole table
					pp.db.removeChild(tp.getModel());
				}
			} else if (item instanceof Relationship) {
				Relationship r = (Relationship) item;
				SQLRelationship sr = r.getModel();
				sr.getPkTable().removeExportedKey(sr);
				sr.getFkTable().removeImportedKey(sr);
			} else {
				JOptionPane.showMessageDialog((JComponent) item,
											  "The selected item type is not recognised");
			}
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
		changeToopTip(pp.getSelectedItems());
	}

	public void itemDeselected(SelectionEvent e) {
		changeToopTip(pp.getSelectedItems());
	}

	/**
	 * Updates the tooltip and enabledness of this action based on how
	 * many items are in the selection list.  If there is only one
	 * selected item, tries to put its name in the tooltip too!
	 */
	protected void changeToopTip(List selectedItems) {
		if (selectedItems.size() == 0) {
			setEnabled(false);
			putValue(SHORT_DESCRIPTION, "Delete Selected");
		} else if (selectedItems.size() == 1) {
			Selectable item = (Selectable) selectedItems.get(0);
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
		} else {
			setEnabled(true);
			putValue(SHORT_DESCRIPTION, "Delete "+selectedItems.size()+" items");
		}
	}
}
