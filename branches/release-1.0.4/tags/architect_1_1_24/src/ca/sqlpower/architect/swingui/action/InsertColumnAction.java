package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class InsertColumnAction extends AbstractAction implements SelectionListener {
	private static final Logger logger = Logger.getLogger(InsertColumnAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected DBTree dbt; 

	
	public InsertColumnAction() {
		super("New Column",
			  ASUtils.createIcon("NewColumn",
								 "New Column",
								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "New Column");
		putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent evt) {
		try {
			if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
				List selection = pp.getSelectedItems();
				if (selection.size() < 1) {
					JOptionPane.showMessageDialog(pp, "Select a table (by clicking on it) and try again.");
				} else if (selection.size() > 1) {
					JOptionPane.showMessageDialog(pp, "You have selected multiple items, but you can only edit one at a time.");
				} else if (selection.get(0) instanceof TablePane) {
					TablePane tp = (TablePane) selection.get(0);
					int idx = tp.getSelectedColumnIndex();
					try {
						if (idx < 0) idx = tp.getModel().getColumnsFolder().getChildCount();
					} catch (ArchitectException e) {
						idx = 0;
					}
					tp.getModel().addColumn(idx, new SQLColumn());
				} else {
					JOptionPane.showMessageDialog(pp, "The selected item type is not recognised");
				}
			} else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
				TreePath [] selections = dbt.getSelectionPaths();
				if (selections == null || selections.length != 1) {
					JOptionPane.showMessageDialog(dbt, "To indicate where you would like to insert a column, please select a single item.");
				} else {
					TreePath tp = selections[0];
					SQLObject so = (SQLObject) tp.getLastPathComponent();
					SQLTable st = null;
					int idx = 0;
					if (so instanceof SQLTable) {
						logger.debug("user clicked on table, so we shall try to add a column to the end of the table.");
						try {
							st = (SQLTable) so;
							idx = st.getColumnsFolder().getChildCount();
						} catch (ArchitectException ex) {
							idx = 0;
						}
						logger.debug("SQLTable click -- idx set to: " + idx);						
					} else if (so instanceof SQLColumn) {
						// iterate through the column list to figure out what position we are in...
						logger.debug("trying to determine insertion index for table.");
						SQLColumn sc = (SQLColumn) so;
						st = sc.getParentTable();
						idx = st.getColumnIndex(sc);
						if (idx == -1)  {
							// not found
							logger.debug("did not find column, inserting at start of table.");
							idx = 0;
						}
					} else {
						idx = 0;
					}
					st.addColumn(idx, new SQLColumn());
				}
			} else {
				JOptionPane.showMessageDialog(
						null, "InsertColumnAction: Unknown Action Command \"" + 
						evt.getActionCommand() + "\"",
						"Internal Architect Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (ArchitectException ex) {
			JOptionPane.showMessageDialog(
					null, "Column could not be inserted:\n" +	ex.getMessage() +
					 "\n\nThere is more detail in the application log.",
					"Architect Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setPlayPen(PlayPen pp) {
		if (this.pp != null) {
			this.pp.removeSelectionListener(this);
		} 
		this.pp = pp;
		pp.addSelectionListener(this);
		
		setupAction(pp.getSelectedItems());
	}

	public void setDBTree(DBTree newDBT) {
		this.dbt = newDBT;
		// do I need to add a selection listener here?
	}
	
	
	public void setupAction(List selectedItems) {
		if (selectedItems.size() == 0) {
			setEnabled(false);
			logger.debug("Disabling Insert Column Action");
			putValue(SHORT_DESCRIPTION, "Insert Column");
		} else {
			Selectable item = (Selectable) selectedItems.get(0);
			if (item instanceof TablePane)				
				setEnabled(true);
		}
	}
		
	public void itemSelected(SelectionEvent e) {
		setupAction(pp.getSelectedItems());
		
	}

	public void itemDeselected(SelectionEvent e) {
		setupAction(pp.getSelectedItems());
	}

}
