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
package ca.sqlpower.architect.swingui;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.swingui.DataEntryPanel;

public class TableEditPanel extends JPanel implements SQLObjectListener, DataEntryPanel {
    
    private static final Logger logger = Logger.getLogger(TableEditPanel.class);

    /**
     * The frame which this table edit panel resides in.
     */
    private JDialog editDialog;
	protected SQLTable table;
	JTextField name;
	JTextField pkName;
	JTextArea remarks;
	private JComboBox bgColor;
	private JComboBox fgColor;
	private JCheckBox rounded;
	private JCheckBox dashed;
	
	private final ArchitectSwingSession session;
	private final TablePane tp;
	
	public TableEditPanel(ArchitectSwingSession session, SQLTable t) {
		super(new FormLayout());
		this.session = session;
		this.tp = session.getPlayPen().findTablePane(t);
		add(new JLabel(Messages.getString("TableEditPanel.tableNameLabel"))); //$NON-NLS-1$
		add(name = new JTextField("", 30)); //$NON-NLS-1$
		add(new JLabel(Messages.getString("TableEditPanel.primaryKeyNameLabel"))); //$NON-NLS-1$
		add(pkName = new JTextField("", 30)); //$NON-NLS-1$
		add(new JLabel(Messages.getString("TableEditPanel.remarksLabel"))); //$NON-NLS-1$
		add(new JScrollPane(remarks = new JTextArea(4, 30)));
		remarks.setLineWrap(true);
		remarks.setWrapStyleWord(true);
		
		add(new JLabel(Messages.getString("TableEditPanel.tableColourLabel"))); //$NON-NLS-1$
		ColorCellRenderer renderer = new ColorCellRenderer(40, 20);
		bgColor = new JComboBox(ColorScheme.BACKGROUND_COLOURS);
        bgColor.setRenderer(renderer);
        bgColor.addItem(new Color(240, 240, 240));
		add(bgColor);
		
		add(new JLabel(Messages.getString("TableEditPanel.textColourLabel"))); //$NON-NLS-1$
		fgColor = new JComboBox(ColorScheme.FOREGROUND_COLOURS);
        fgColor.setRenderer(renderer);
        fgColor.addItem(Color.BLACK);
        add(fgColor);
        
        add(new JLabel(Messages.getString("TableEditPanel.dashedLinesLabel"))); //$NON-NLS-1$
        add(dashed = new JCheckBox());
        add(new JLabel(Messages.getString("TableEditPanel.roundedCornersLabel"))); //$NON-NLS-1$
        add(rounded = new JCheckBox());        
        
		editTable(t);
	}

	private void editTable(SQLTable t) {
		table = t;
		name.setText(t.getName());
        try {
            if (t.getPrimaryKeyIndex() == null) {
                pkName.setEnabled(false);
            } else {
                pkName.setText(t.getPrimaryKeyName());
                pkName.setEnabled(true);
            }
            ArchitectUtils.listenToHierarchy(this, session.getRootObject());
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
		remarks.setText(t.getRemarks());
		name.selectAll();
		
		if (tp != null) {
    		bgColor.setSelectedItem(tp.getBackgroundColor());
    		fgColor.setSelectedItem(tp.getForegroundColor());
    		dashed.setSelected(tp.isDashed());
    		rounded.setSelected(tp.isRounded());
		}
	}

	// --------------------- ArchitectPanel interface ------------------
	public boolean applyChanges() {
	    try {
            ArchitectUtils.unlistenToHierarchy(this, session.getRootObject());
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
		table.startCompoundEdit(Messages.getString("TableEditPanel.compoundEditName"));		 //$NON-NLS-1$
        try {	
		    StringBuffer warnings = new StringBuffer();
            //We need to check if the table name and/or primary key name is empty or not
            //if they are, we need to warn the user since it will mess up the SQLScripts we create
            if (name.getText().trim().length() == 0) {
                warnings.append(Messages.getString("TableEditPanel.blankTableNameWarning")); //$NON-NLS-1$
                
            }
            if (pkName.isEnabled() &&
                    pkName.getText().trim().length() == 0) {
                warnings.append(Messages.getString("TableEditPanel.blankPkNameWarning"));                 //$NON-NLS-1$
            }

            if (warnings.toString().length() == 0) {
                
                // important: set the primary key name first, because if the primary
                // key was called (for example) new_table_pk, and the table was called
                // new_table, then the user changes the table name to cow_table, the
                // table itself will notice this pattern and automatically change its
                // primary key name to cow_table_pk.  If we set the table name first,
                // the magic still happens, but then we would overwrite the new pk name
                // with the old one from the pk name text field in this panel.
                if (pkName.isEnabled() && table.getPrimaryKeyIndex() != null) {
                    table.getPrimaryKeyIndex().setName(pkName.getText());
                }
                
                table.setName(name.getText());
                table.setRemarks(remarks.getText());   
                
                if (tp != null) {
                    if (!tp.getBackgroundColor().equals((Color)bgColor.getSelectedItem())) {
                        tp.setBackgroundColor((Color)bgColor.getSelectedItem());
                    } 
                    if (!tp.getForegroundColor().equals((Color)fgColor.getSelectedItem())) {
                        tp.setForegroundColor((Color)fgColor.getSelectedItem());
                    } 
                    if (tp.isDashed() != dashed.isSelected()) {
                        tp.setDashed(dashed.isSelected());
                    } 
                    if (tp.isRounded() != rounded.isSelected()) {
                        tp.setRounded(rounded.isSelected());
                    }
                }
                return true;
            } else {
                JOptionPane.showMessageDialog(this,warnings.toString());
                //this is done so we can go back to this dialog after the error message
                return false;
            }            
		} catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        } finally {
			table.endCompoundEdit("Ending new compound edit event in table edit panel"); //$NON-NLS-1$
		}
	}

	public void discardChanges() {
	    try {
            ArchitectUtils.unlistenToHierarchy(this, session.getRootObject());
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
	}
	
	public JPanel getPanel() {
		return this;
	}

    public String getNameText() {
        return name.getText();
    }

    public void setNameText(String newName) {
        name.setText(newName);
    }

    public String getPkNameText() {
        return pkName.getText();
    }

    public void setPkNameText(String newPkName) {
        pkName.setText(newPkName);
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

    public void dbChildrenInserted(SQLObjectEvent e) {

    }

    /**
     * Checks to see if its respective table is removed from playpen. If yes,
     * exit the editing dialog window.
     */
    public void dbChildrenRemoved(SQLObjectEvent e) {
        logger.debug("SQLObject children got removed: " + e); //$NON-NLS-1$
        SQLObject[] c = e.getChildren();

        for (SQLObject obj : c) {
            try {
                if (table.equals(obj)) {
                    ArchitectUtils.unlistenToHierarchy(this, session.getRootObject());
                    if (editDialog != null) {
                        editDialog.dispose();
                    }
                    break;
                }
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
        }
    }

    public void dbObjectChanged(SQLObjectEvent e) {

    }

    public void dbStructureChanged(SQLObjectEvent e) {

    }

    public void setEditDialog(JDialog editDialog) {
        this.editDialog = editDialog;
    }
}