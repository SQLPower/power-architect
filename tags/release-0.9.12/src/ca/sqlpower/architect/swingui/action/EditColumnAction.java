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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ColumnEditPanel;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class EditColumnAction extends AbstractArchitectAction implements SelectionListener {
	private static final Logger logger = Logger.getLogger(EditColumnAction.class);

	private final ArchitectSwingSession session;

	public EditColumnAction(ArchitectSwingSession session) {
        super(session, Messages.getString("EditColumnAction.name"), Messages.getString("EditColumnAction.description"), "edit_column"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.session = session;
		setEnabled(false);
        
        playpen.addSelectionListener(this);
        setupAction(playpen.getSelectedItems());
	}

	public void actionPerformed(ActionEvent evt) {
	    List<PlayPenComponent> selection = playpen.getSelectedItems();
	    logger.debug("selections length is: " + selection.size());			 //$NON-NLS-1$
	    if (selection.size() < 1) {
	        JOptionPane.showMessageDialog(playpen, Messages.getString("EditColumnAction.noColumnSelected")); //$NON-NLS-1$
	    } else if (selection.size() > 1) {
	        JOptionPane.showMessageDialog(playpen, Messages.getString("EditColumnAction.multipleItemsSelected")); //$NON-NLS-1$
	    } else if (selection.get(0) instanceof TablePane) {
	        TablePane tp = (TablePane) selection.get(0);
	        try {
	            List<SQLColumn> selectedCols = tp.getSelectedItems();
	            if (selectedCols.size() != 1) {
	                JOptionPane.showMessageDialog(playpen, Messages.getString("EditColumnAction.pleaseSelectOnlyOneColumn")); //$NON-NLS-1$
	                return;
	            }
	            int idx = tp.getSelectedItemIndex();
	            if (idx < 0) { // header must have been selected
	                throw new IllegalStateException("There was one selected column but the selected item index was negative");
	            } else {				
	                showDialog(tp.getModel(),idx);
	            }
	        } catch (ArchitectException e) {
	            JOptionPane.showMessageDialog(playpen, Messages.getString("EditColumnAction.errorFindingSelectedColumn")); //$NON-NLS-1$
	        }
	    } else {
	        // One thing was selected, but it wasn't a tablepane
	        JOptionPane.showMessageDialog(playpen, Messages.getString("EditColumnAction.pleaseSelectColumn")); //$NON-NLS-1$
	    }
	}
	
    protected void showDialog(final SQLTable st, final int colIdx) throws ArchitectException {
        showDialog(st, colIdx, false, null);
    }

	
	protected void showDialog(final SQLTable st, final int colIdx, final boolean addToTable,
	        final TablePane tp) throws ArchitectException {
				    
		    logger.debug("Creating new column editor panel"); //$NON-NLS-1$
		    
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
                column.setAutoIncrementSequenceName(st.getName() + "_" + column.getName() + "_seq"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			final ColumnEditPanel columnEditPanel = new ColumnEditPanel(column, session);
			panel.add(columnEditPanel, BorderLayout.CENTER);
			
			JDialog editDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
					columnEditPanel,
					frame,
					 Messages.getString("EditColumnAction.columnPropertiesDialogTitle", st.getName()), //$NON-NLS-1$
					 Messages.getString("EditColumnAction.okOption"), //$NON-NLS-1$
					 new Callable<Boolean>(){
						public Boolean call() {
						    EditColumnAction.this.putValue(SHORT_DESCRIPTION, Messages.getString("EditColumnAction.specificColumnShortDescription", columnEditPanel.getColName().getText())); //$NON-NLS-1$
						    if (addToTable) {
						        tp.getModel().startCompoundEdit("adding a new column '" + columnEditPanel.getColName().getText() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
							    try {
							        tp.getModel().addColumn(colIdx, column);
							    } catch (ArchitectException e) {
							        ASUtils.showExceptionDialog(session, "Error Could not add column to table", e); //$NON-NLS-1$
							    }
							}
	                        Boolean ret = Boolean.valueOf(columnEditPanel.applyChanges());
	                        if (addToTable) {
	                            tp.getModel().endCompoundEdit("adding a new column '" + columnEditPanel.getColName().getText() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
						    }
							return ret;
						}
					}, 
					new Callable<Boolean>(){
                        public Boolean call() {
							columnEditPanel.discardChanges();
							return Boolean.TRUE;
						}
					});
			columnEditPanel.setEditDialog(editDialog);
			panel.setOpaque(true);
			editDialog.pack();
			editDialog.setLocationRelativeTo(frame);
			editDialog.setVisible(true);
	}

	private void setupAction(List<PlayPenComponent> selectedItems) {
		if (selectedItems.size() == 0) {
			setEnabled(false);
			logger.debug("Disabling EditColumnAction"); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, Messages.getString("EditColumnAction.shortDescription")); //$NON-NLS-1$
		} else {
			Selectable item = selectedItems.get(0);
			String name = Messages.getString("EditColumnAction.selected"); //$NON-NLS-1$
			logger.debug("Selected Table"); //$NON-NLS-1$
			if (item instanceof TablePane) {				
				TablePane tp = (TablePane) item;
				
				if (tp.getSelectedItemIndex() > ContainerPane.ITEM_INDEX_TITLE ) {
					try {						
						logger.debug ("Enabling EditColumnAction"); //$NON-NLS-1$
						setEnabled(true);
						name = tp.getModel().getColumn(tp.getSelectedItemIndex()).getName();
					} catch (ArchitectException ex) {
						logger.error("Couldn't get selected column name", ex); //$NON-NLS-1$
					}
				} else {
					name = tp.getModel().toString();
					setEnabled(false);
					logger.debug("Disabling EditColumnAction"); //$NON-NLS-1$
				}
			} 
			putValue(SHORT_DESCRIPTION, Messages.getString("EditColumnAction.specificColumnShortDescription", name)); //$NON-NLS-1$
		}
	}
		
	public void itemSelected(SelectionEvent e) {
		setupAction(playpen.getSelectedItems());
		
	}

	public void itemDeselected(SelectionEvent e) {
		setupAction(playpen.getSelectedItems());
	}
	
	

}
