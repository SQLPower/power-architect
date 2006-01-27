package ca.sqlpower.architect.swingui;

import java.util.List;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.TreePath;
import ca.sqlpower.architect.*;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

import org.apache.log4j.Logger;

public class DeleteSelectedAction extends AbstractAction implements SelectionListener {
	private static final Logger logger = Logger.getLogger(DeleteSelectedAction.class);

	/**
	 * The PlayPen instance that is associated with this Action.
	 */
	protected PlayPen pp;

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected DBTree dbt; 
	
	public DeleteSelectedAction() {
		super("Delete Selected",
			  ASUtils.createJLFIcon("general/Delete",
								 "Delete Selected",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Delete Selected");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)); // XXX: how to attach to components?
		putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		setEnabled(false);
	}
	
	
	/*
	 * This action takes care of handling Delete requests from the DBTree and Playpen.
	 * 
	 * Delete Policy (Playpen):
	 * - if more than 1 item is selected in the playpen, it does not try to delete individual columns
	 * - if only a single item is selected, it will attempt to delete columns (if that item was Table)
	 * 
	 * Delete Policy (DBTree): 
	 * 
	 * 
	 */
	public void actionPerformed(ActionEvent evt) {
		logger.debug("delete action detected!");
		logger.debug("ACTION COMMAND: " + evt.getActionCommand());

		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {

			logger.debug("delete action came from playpen");
			List items = pp.getSelectedItems();			

			if (items.size() < 1) {
				JOptionPane.showMessageDialog(pp, "No items to delete!");
			}				

			if (items.size() > 1) {
				// count how many relationships and tables there are
				int tCount = pp.getSelectedTables().size();
				int rCount = pp.getSelectedRelationShips().size();
				
				int decision = JOptionPane.showConfirmDialog(pp,
															 "Are you sure you want to delete these "
															 +tCount+" tables and "+rCount+" relationships?",
															 "Multiple Delete",
															 JOptionPane.YES_NO_OPTION);
				if (decision == JOptionPane.NO_OPTION) {
					return;
				}
			} else { // single selection, so we might be deleting columns
				boolean deletingColumns = false;
				Selectable item = (Selectable) items.get(0);
				if (item instanceof TablePane) {
					// make a list of columns to delete
					TablePane tp = (TablePane) item;
					ArrayList selectedColumns = new ArrayList();
					try {
						for (int i=0; i < tp.getModel().getColumns().size(); i++) {
							if (tp.isColumnSelected(i)) {
								deletingColumns = true; // don't fall through into Table/Relationship delete logic
								selectedColumns.add(tp.getModel().getColumn(i));
							}
						}
					} catch (ArchitectException ae) {
						JOptionPane.showMessageDialog(pp, ae.getMessage());
						return;
					}	
				
					pp.fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.MULTI_SELECT_START,"Starting multi-select"));
					
					// now, delete the columns
					Iterator it2 = selectedColumns.iterator();
					while (it2.hasNext()) {
						SQLColumn sc = (SQLColumn) it2.next();
						try {
							tp.getModel().removeColumn(sc);
						} catch (LockedColumnException ex) {
							int decision = JOptionPane.showConfirmDialog(pp,
									"Could not delete the column " + sc.getName()  
									+ " because it is part of a relationship key.  Continue"
									+ " deleting of other selected columns?",
									"Column is Locked",
									JOptionPane.YES_NO_OPTION);
							if (decision == JOptionPane.NO_OPTION) {
								return;
							}
						}						
					}
					
					pp.fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.MULTI_SELECT_END,"Ending multi-select"));
					
				}
				if (deletingColumns) { // we tried to delete 1 or more columns, so don't try to delete the table
					return;
				}
			}
			
			
			pp.fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.MULTI_SELECT_START,"Starting multi-select"));
			
			
			// items.size() > 0, user has OK'ed the delete
			Iterator it = items.iterator();
			while (it.hasNext()) {
				Selectable item = (Selectable) it.next();
				logger.debug("next item for delete is: " + item.getClass().getName());
				if (item instanceof TablePane) {
					TablePane tp = (TablePane) item;
					tp.setSelected(false);
					pp.db.removeChild(tp.getModel());
					if (logger.isDebugEnabled()) {
						logger.debug("removing element from tableNames set: " + tp.getModel().getTableName());
						logger.debug("before delete: " + pp.tableNames.toArray());
					}
					pp.tableNames.remove(tp.getModel().getTableName().toLowerCase());
					if (logger.isDebugEnabled()) {
						logger.debug("after delete: " + pp.tableNames.toArray());
					}
				} else if (item instanceof Relationship) {
					Relationship r = (Relationship) item;
					logger.debug("trying to delete relationship " + r);
					r.setSelected(false);
					SQLRelationship sr = r.getModel();
					sr.getPkTable().removeExportedKey(sr);
					sr.getFkTable().removeImportedKey(sr);
				} else {
					JOptionPane.showMessageDialog((JComponent) item,
												  "The selected item type is not recognised");
				}
			}
			
			pp.fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.MULTI_SELECT_END,"Ending multi-select"));
			
		} else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
			logger.debug("delete action came from dbtree");
			TreePath [] selections = dbt.getSelectionPaths();
			if (selections.length > 1) {
				int decision = JOptionPane.showConfirmDialog(dbt,
															 "Are you sure you want to delete the "
															 +selections.length+" selected items?",
															 "Multiple Delete",
															 JOptionPane.YES_NO_OPTION);
				if (decision == JOptionPane.NO_OPTION) {
					return;
				}
			}	
			
			pp.fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.MULTI_SELECT_START,"Starting multi-select"));
			
			Iterator it = Arrays.asList(selections).iterator();
			while (it.hasNext()) {
				TreePath tp = (TreePath) it.next();
				SQLObject so = (SQLObject) tp.getLastPathComponent();
				if (so instanceof SQLTable) {
					SQLTable st = (SQLTable) so;
					pp.db.removeChild(st);
					pp.tableNames.remove(st.getTableName().toLowerCase());
				} else if (so instanceof SQLColumn) {
					SQLColumn sc = (SQLColumn)so;
					SQLTable st = sc.getParentTable();
					try {
						st.removeColumn(sc); 
					} catch (LockedColumnException ex) {
						int decision = JOptionPane.showConfirmDialog(dbt,
													 "Could not delete the column " + sc.getName() 
                                                           + " because it is part of a relationship key.  Continue"
                                                           + " deleting of other selected items?",
													 "Column is Locked",
													 JOptionPane.YES_NO_OPTION);
						if (decision == JOptionPane.NO_OPTION) {
							return;
						}
					}
				} else if (so instanceof SQLRelationship) {
					SQLRelationship sr = (SQLRelationship) so;
					sr.getPkTable().removeExportedKey(sr);
					sr.getFkTable().removeImportedKey(sr);
				} else {
					JOptionPane.showMessageDialog(dbt, "The selected SQLObject type is not recognised: " + so.getClass().getName());
				}
			}
			
			pp.fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.MULTI_SELECT_END,"Ending multi-select"));
			
		} else {
			logger.debug("delete action came from unknown source, so we do nothing.");
	  		// unknown action command source, do nothing
		}	
	}
	
	public void setPlayPen(PlayPen newPP) {
		if (pp != null) {
			pp.removeSelectionListener(this);
		}
		pp = newPP;
		pp.addSelectionListener(this);
	}

	public void setDBTree(DBTree newDBT) {
		this.dbt = newDBT;
		// do I need to add a selection listener here?
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
