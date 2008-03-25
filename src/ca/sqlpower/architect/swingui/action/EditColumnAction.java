/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.Callable;

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
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ColumnEditPanel;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class EditColumnAction extends AbstractArchitectAction implements SelectionListener {
	private static final Logger logger = Logger.getLogger(EditColumnAction.class);

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected final DBTree dbt; 


	protected JDialog editDialog;			
	protected ColumnEditPanel columnEditPanel;

	public EditColumnAction(ArchitectSwingSession session) {
        super(session, "Column Properties...", "Column Properties", "edit_column");
		putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		setEnabled(false);
        
        playpen.addSelectionListener(this);
        setupAction(playpen.getSelectedItems());
        
        dbt = frame.getDbTree();
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = playpen.getSelectedItems();
			logger.debug("selections length is: " + selection.size());			
			if (selection.size() < 1) {
				setEnabled(false);
				JOptionPane.showMessageDialog(playpen, "Select a column (by clicking on it) and try again.");
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(playpen, "You have selected multiple items, but you can only edit one at a time.");
			} else if (selection.get(0) instanceof TablePane) {
				setEnabled(true);
				TablePane tp = (TablePane) selection.get(0);
				try {
					List<SQLColumn> selectedCols = tp.getSelectedColumns();
					if (selectedCols.size() != 1) {
						JOptionPane.showMessageDialog(playpen, "Please select one and only one column");
						logger.error("Please select one and only one column");
						cleanup();
						return;
					}
					int idx = tp.getSelectedColumnIndex();
					if (idx < 0) { // header must have been selected
						logger.error("CantHaplaypenen: idx < 0");
						JOptionPane.showMessageDialog(playpen, "Please select the column you would like to edit.");						
					} else {				
						makeDialog(tp.getModel(),idx);
					}
				} catch (ArchitectException e) {
					JOptionPane.showMessageDialog(playpen, "Error finding the selected column");
					logger.error("Error finding the selected column", e);
					cleanup();
				}
			} else {
				JOptionPane.showMessageDialog(playpen, "Please select the column you would like to edit.");
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
	
    protected void makeDialog(final SQLTable st, final int colIdx) throws ArchitectException {
        makeDialog(st, colIdx, false, null);
    }

	
	protected void makeDialog(final SQLTable st, final int colIdx, final boolean addToTable,
	        final TablePane tp) throws ArchitectException {
		if (editDialog != null) {
			columnEditPanel.editColumn(st.getColumn(colIdx));			
			editDialog.setTitle("Column Properties of "+st.getName());
			editDialog.setVisible(true);				
			//editDialog.requestFocus();
			
		} else {
				    
		    logger.debug("Creating new column editor panel");
		    
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout(12,12));
			panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

            final SQLColumn column;
			if (!addToTable) {
                column = st.getColumn(colIdx);
			} else {
                column = new SQLColumn();
                
                // XXX it sucks to do this here, but the column can't determine its correct
                //     sequence name until it has a parent. By then, it will be too late.
                column.setAutoIncrementSequenceName(st.getName() + "_" + column.getName() + "_seq");
			}
			columnEditPanel = new ColumnEditPanel(column);
			
			panel.add(columnEditPanel, BorderLayout.CENTER);
			
			editDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
					columnEditPanel,
					frame,
					 "Column Properties of "+st.getName(),
					 "OK",
					 new Callable<Boolean>(){
						public Boolean call() {
						    EditColumnAction.this.putValue(SHORT_DESCRIPTION, "Editing "+columnEditPanel.getColName().getText() );
						    if (addToTable) {
						        tp.getModel().startCompoundEdit("adding a new column '" + columnEditPanel.getColName().getText() + "'");
							    try {
							        tp.getModel().addColumn(colIdx, column);
							    } catch (ArchitectException e) {
							        ASUtils.showExceptionDialog(session, "Error Could not add column to table", e);
							    }
							}
	                        Boolean ret = new Boolean(columnEditPanel.applyChanges());
	                        if (addToTable) {
	                            tp.getModel().endCompoundEdit("adding a new column '" + columnEditPanel.getColName().getText() + "'");
						    }
							return ret;
						}
					}, 
					new Callable<Boolean>(){
                        public Boolean call() {
							columnEditPanel.discardChanges();
							return new Boolean(true);
						}
					});
			panel.setOpaque(true);
			editDialog.pack();
			editDialog.setLocationRelativeTo(frame);
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
		setupAction(playpen.getSelectedItems());
		
	}

	public void itemDeselected(SelectionEvent e) {
		setupAction(playpen.getSelectedItems());
	}
	
	

}
