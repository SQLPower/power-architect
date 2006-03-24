package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.TreePath;
import ca.sqlpower.architect.*;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ColumnEditPanel;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.undo.SQLObjectUndoableEventAdapter;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

import org.apache.log4j.Logger;

public class EditColumnAction extends AbstractAction implements ActionListener {
	private static final Logger logger = Logger.getLogger(EditColumnAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected DBTree dbt; 


	protected JDialog editDialog;
	protected JButton okButton;
	protected JButton cancelButton;
	protected ActionListener okCancelListener;
	protected ColumnEditPanel columnEditPanel;

	public EditColumnAction() {
		super("Column Properties...",
			  ASUtils.createIcon("ColumnProperties",
								 "Column Properties",
								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Column Properties");
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = pp.getSelectedItems();
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(pp, "Select a column (by clicking on it) and try again.");
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(pp, "You have selected multiple items, but you can only edit one at a time.");
			} else if (selection.get(0) instanceof TablePane) {
				TablePane tp = (TablePane) selection.get(0);
				try {
					int idx = tp.getSelectedColumnIndex();
					if (idx < 0) { // header must have been selected
						JOptionPane.showMessageDialog(pp, "Please select the column you would like to edit.");						
					} else {				
						makeDialog(tp.getModel(),idx);
					}
				} catch (ArchitectException e) {
					JOptionPane.showMessageDialog(pp, "Error finding the selected column");
					logger.error("Error finding the selected column", e);
					cleanup();
				}
			} else {
				JOptionPane.showMessageDialog(pp, "Please select the column you would like to edit.");
				cleanup();
			}
		} else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
			TreePath [] selections = dbt.getSelectionPaths();
			logger.debug("selections length is: " + selections.length);
			if (selections.length != 1) {
				JOptionPane.showMessageDialog(dbt, "Please select the column you would like to edit.");
			} else {
				TreePath tp = selections[0];
				SQLObject so = (SQLObject) tp.getLastPathComponent();
				if (so instanceof SQLColumn) {
					SQLColumn sc = (SQLColumn) so;
					SQLTable st = sc.getParentTable();
					try {
						int idx = st.getColumnIndex(sc);
						if (idx < 0) {
							JOptionPane.showMessageDialog(dbt, "Error finding the selected column");
						} else {
							makeDialog(st,idx);
						}							
					} catch (ArchitectException ex) {
						JOptionPane.showMessageDialog(dbt, "Error finding the selected column");
						logger.error("Error finding the selected column", ex);
						cleanup();
					}										
				} else {
					JOptionPane.showMessageDialog(dbt, "Please select the column you would like to edit.");
				}
			}
		} else {
	  		// unknown action command source, do nothing
		}	
	}

	void makeDialog(SQLTable st, int colIdx) throws ArchitectException {
		if (editDialog != null) {
			columnEditPanel.setModel(st);
			columnEditPanel.selectColumn(colIdx);
			editDialog.setTitle("Column Properties of "+st.getName());
			editDialog.setVisible(true);
			editDialog.requestFocus();
		} else {
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(12,12));
			panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
			columnEditPanel = new ColumnEditPanel(st, colIdx);
			panel.add(columnEditPanel, BorderLayout.CENTER);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			okCancelListener = new OkCancelListener();
			okButton = new JButton("Ok");
			okButton.addActionListener(okCancelListener);
			buttonPanel.add(okButton);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(okCancelListener);
			buttonPanel.add(cancelButton);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			
			editDialog = new JDialog(ArchitectFrame.getMainInstance(),
									 "Column Properties of "+st.getName());
			panel.setOpaque(true);
			editDialog.setContentPane(panel);
			editDialog.pack();
			editDialog.setLocationRelativeTo(ArchitectFrame.getMainInstance());
			editDialog.setVisible(true);
		}
		
		
	}

	class OkCancelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				columnEditPanel.applyChanges();
				cleanup();
			} else if (e.getSource() == cancelButton) {
				columnEditPanel.discardChanges();
				cleanup();
			} else {
				logger.error("Recieved action event from unknown source: "+e);
			}
		}
	}

	/**
	 * Permanently closes the edit dialog.
	 */
	protected void cleanup() {
		if (editDialog != null) {
			editDialog.setVisible(false);
			editDialog.dispose();
			editDialog = null;
		}
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}

	public void setDBTree(DBTree newDBT) {
		this.dbt = newDBT;
		// do I need to add a selection listener here?
	}
	
	

}
