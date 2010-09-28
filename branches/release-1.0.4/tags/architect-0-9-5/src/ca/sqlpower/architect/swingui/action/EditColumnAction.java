package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ColumnEditPanel;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class EditColumnAction extends AbstractAction implements SelectionListener {
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
	protected ColumnEditPanel columnEditPanel;

	public EditColumnAction() {
		super("Column Properties...",
			  ASUtils.createIcon("ColumnProperties",
								 "Column Properties",
								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Column Properties");
		putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = pp.getSelectedItems();
			logger.debug("selections length is: " + selection.size());			
			if (selection.size() < 1) {
				setEnabled(false);
				JOptionPane.showMessageDialog(pp, "Select a column (by clicking on it) and try again.");
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(pp, "You have selected multiple items, but you can only edit one at a time.");
			} else if (selection.get(0) instanceof TablePane) {
				setEnabled(true);
				TablePane tp = (TablePane) selection.get(0);
				try {
					List<SQLColumn> selectedCols = tp.getSelectedColumns();
					if (selectedCols.size() != 1) {
						JOptionPane.showMessageDialog(pp, "Please select one and only one column");
						logger.error("Please select one and only one column");
						cleanup();
						return;
					}
					int idx = tp.getSelectedColumnIndex();
					if (idx < 0) { // header must have been selected
						logger.error("CantHappen: idx < 0");
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

	protected void makeDialog(SQLTable st, int colIdx) throws ArchitectException {
		if (editDialog != null) {
			columnEditPanel.editColumn(st.getColumn(colIdx));			
			editDialog.setTitle("Column Properties of "+st.getName());
			editDialog.setVisible(true);				
			//editDialog.requestFocus();
			
		} else {
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(12,12));
			panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
			columnEditPanel = new ColumnEditPanel(st.getColumn(colIdx));
			panel.add(columnEditPanel, BorderLayout.CENTER);
			
			editDialog = ArchitectPanelBuilder.createArchitectPanelDialog(
					columnEditPanel,
					ArchitectFrame.getMainInstance(),
					 "Column Properties of "+st.getName(),
					 "OK",
					 new AbstractAction(){
						public void actionPerformed(ActionEvent e) {
							columnEditPanel.applyChanges();
							EditColumnAction.this.putValue(SHORT_DESCRIPTION, "Editting "+columnEditPanel.getColName().getText() );
						}
					}, 
					new AbstractAction(){
						public void actionPerformed(ActionEvent e) {
							columnEditPanel.discardChanges();
						}
					});
			panel.setOpaque(true);
			editDialog.pack();
			editDialog.setLocationRelativeTo(ArchitectFrame.getMainInstance());
			editDialog.setVisible(true);
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

	public void setPlayPen(PlayPen newPP) {
		if (pp != null) {
			pp.removeSelectionListener(this);
		} 
		pp = newPP;
		pp.addSelectionListener(this);
		
		setupAction(pp.getSelectedItems());
	}

	
	public void setDBTree(DBTree newDBT) {
		this.dbt = newDBT;
		// do I need to add a selection listener here?
	}
	
	private void setupAction(List selectedItems) {
		if (selectedItems.size() == 0) {
			setEnabled(false);
			logger.debug("Disabling EditColumnAction");
			putValue(SHORT_DESCRIPTION, "Edit Selected Column");
		} else {
			Selectable item = (Selectable) selectedItems.get(0);
			String name = "Selected";
			logger.debug("Selected Table");
			if (item instanceof TablePane) {				
				TablePane tp = (TablePane) item;
				
				if (tp.getSelectedColumnIndex() > TablePane.COLUMN_INDEX_TITLE ) {
					try {						
						logger.debug ("Enabling EditColumnAction");
						setEnabled(true);
						name = tp.getModel().getColumn(tp.getSelectedColumnIndex()).getName();
					} catch (ArchitectException ex) {
						logger.error("Couldn't get selected column name", ex);
					}
				} else {
					name = tp.getModel().toString();
					setEnabled(false);
					logger.debug("Disabling EditColumnAction");
				}
			} 
			putValue(SHORT_DESCRIPTION, "Editting "+name);
		}
	}
		
	public void itemSelected(SelectionEvent e) {
		setupAction(pp.getSelectedItems());
		
	}

	public void itemDeselected(SelectionEvent e) {
		setupAction(pp.getSelectedItems());
	}
	
	

}
