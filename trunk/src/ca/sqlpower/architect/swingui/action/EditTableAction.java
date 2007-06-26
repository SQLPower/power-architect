package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.TableEditPanel;
import ca.sqlpower.architect.swingui.TablePane;

public class EditTableAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected final DBTree dbt; 
	
	public EditTableAction(ArchitectSwingSession session) {
	    super(session, "Table Properties...", "Table Properties", "TableProperties");
        dbt = frame.getDbTree();
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = playpen.getSelectedItems();
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(playpen, "Select a table (by clicking on it) and try again.");
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(playpen, "You have selected multiple items, but you can only edit one at a time.");
			} else if (selection.get(0) instanceof TablePane) {
				TablePane tp = (TablePane) selection.get(0);
				makeDialog(tp.getModel());				
			} else {
				JOptionPane.showMessageDialog(playpen, "The selected item type is not recognised");
			}

		} else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
			TreePath [] selections = dbt.getSelectionPaths();
			logger.debug("selections length is: " + selections.length);
			if (selections.length != 1) {
				JOptionPane.showMessageDialog(dbt, "To indicate which table you like edit, please select a single table header.");
			} else {
				TreePath tp = selections[0];
				SQLObject so = (SQLObject) tp.getLastPathComponent();
				SQLTable st = null;

				if (so instanceof SQLTable) {
					logger.debug("user clicked on table, so we shall try to edit the table properties.");
					st = (SQLTable) so;
					makeDialog(st);	
				} else {
					JOptionPane.showMessageDialog(dbt, "To indicate which table you like edit, please select a single table header.");
				}
			}
		} else {
	  		// unknown action command source, do nothing
		}	
	}

	private JDialog d;
	
	private void makeDialog(SQLTable table) {
		final TableEditPanel editPanel = new TableEditPanel(session, table);

		Action okAction = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				//We need to see if the operation is successful, if
                //successful, we close down the dialog, if not, we need 
                //to return the dialog (hence why it is setVisible(!success))
                boolean success = editPanel.applyChanges();
				// XXX: also apply changes on mapping tab                
                d.setVisible(!success);
			}
		};

		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				editPanel.discardChanges();
				// XXX: also discard changes on mapping tab
				d.setVisible(false);
			}
		};

		d = ArchitectPanelBuilder.createArchitectPanelDialog(
				editPanel, frame,
				"Table Properties", "OK", okAction, cancelAction);

		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}
}
