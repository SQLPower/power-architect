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
import java.beans.PropertyChangeEvent;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.ChangeListeningDataEntryPanel;
import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.swingui.DataEntryPanelChangeUtil;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

public class TableEditPanel extends ChangeListeningDataEntryPanel implements SPListener {
    
    private static final Logger logger = Logger.getLogger(TableEditPanel.class);

    /**
     * The frame which this table edit panel resides in.
     */
    private JDialog editDialog;
    private JPanel panel;
	protected SQLTable table;
	JTextField logicalName;
	JTextField physicalName;
	JTextField pkName;
	JTextArea remarks;
	private JComboBox bgColor;
	private JComboBox fgColor;
	private JCheckBox rounded;
	private JCheckBox dashed;
	
	private final ArchitectSwingSession session;
	private final TablePane tp;
	
	final HashMap<String, PropertyChangeEvent> propertyConflicts = new HashMap<String, PropertyChangeEvent>();
	
	final HashMap<String, JComponent> propertyFields = new HashMap<String, JComponent>();

	
	public TableEditPanel(ArchitectSwingSession session, SQLTable t) {
		this.panel = new JPanel(new FormLayout());
		this.session = session;
		this.tp = session.getPlayPen().findTablePane(t);
		if (tp != null) tp.addSPListener(this);
        panel.add(new JLabel(Messages.getString("TableEditPanel.tableLogicalNameLabel"))); //$NON-NLS-1$
        panel.add(logicalName = new JTextField("", 30)); //$NON-NLS-1$        
        panel.add(new JLabel(Messages.getString("TableEditPanel.tablePhysicalNameLabel"))); //$NON-NLS-1$
        panel.add(physicalName = new JTextField("", 30)); //$NON-NLS-1$
		panel.add(new JLabel(Messages.getString("TableEditPanel.primaryKeyNameLabel"))); //$NON-NLS-1$
		panel.add(pkName = new JTextField("", 30)); //$NON-NLS-1$
		panel.add(new JLabel(Messages.getString("TableEditPanel.remarksLabel"))); //$NON-NLS-1$
		panel.add(new JScrollPane(remarks = new JTextArea(4, 30)));
		remarks.setLineWrap(true);
		remarks.setWrapStyleWord(true);
		
		panel.add(new JLabel(Messages.getString("TableEditPanel.tableColourLabel"))); //$NON-NLS-1$		
		ColorCellRenderer renderer = new ColorCellRenderer(40, 20);
		bgColor = new JComboBox(ColourScheme.BACKGROUND_COLOURS);
        bgColor.setRenderer(renderer);
        bgColor.addItem(new Color(240, 240, 240));
		panel.add(bgColor);
		
		panel.add(new JLabel(Messages.getString("TableEditPanel.textColourLabel"))); //$NON-NLS-1$
		fgColor = new JComboBox(ColourScheme.FOREGROUND_COLOURS);
        fgColor.setRenderer(renderer);
        fgColor.addItem(Color.BLACK);
        panel.add(fgColor);
        
        panel.add(new JLabel(Messages.getString("TableEditPanel.dashedLinesLabel"))); //$NON-NLS-1$
        panel.add(dashed = new JCheckBox());
        panel.add(new JLabel(Messages.getString("TableEditPanel.roundedCornersLabel"))); //$NON-NLS-1$
        panel.add(rounded = new JCheckBox());
        
		editTable(t);
	}

    private void editTable(SQLTable t) {
		table = t;
		logicalName.setText(t.getName());
		physicalName.setText(t.getPhysicalName());
        try {
            if (t.getPrimaryKeyIndex() == null) {
                pkName.setEnabled(false);
            } else {
                pkName.setText(t.getPrimaryKeyName());
                pkName.setEnabled(true);
            }
            SQLPowerUtils.listenToHierarchy(session.getRootObject(), this);            
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        }
		remarks.setText(t.getRemarks());
		logicalName.selectAll();
		
		if (tp != null) {
    		bgColor.setSelectedItem(tp.getBackgroundColor());
    		fgColor.setSelectedItem(tp.getForegroundColor());
    		dashed.setSelected(tp.isDashed());
    		rounded.setSelected(tp.isRounded());
		}
	}

	// --------------------- ArchitectPanel interface ------------------
	public boolean applyChanges() {
	    SQLPowerUtils.unlistenToHierarchy(session.getRootObject(), this);
	    if (tp != null) tp.removeSPListener(this);
		table.begin(Messages.getString("TableEditPanel.compoundEditName"));		 //$NON-NLS-1$
        try {	
		    String warnings = generateWarnings();

            if (warnings.length() == 0) {
                
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
                
                table.setName(logicalName.getText());
                table.setPhysicalName(physicalName.getText());
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
                JOptionPane.showMessageDialog(panel,warnings);
                //this is done so we can go back to this dialog after the error message
                return false;
            }            
		} catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        } finally {
			table.commit();
		}
	}

    /**
     * Returns a String of warning messages if the table name or primary key
     * name is empty. An empty String is returned if both names are non-empty.
     */
    protected String generateWarnings() {
        StringBuffer warnings = new StringBuffer();
        //We need to check if the table name and/or primary key name is empty or not
        //if they are, we need to warn the user since it will mess up the SQLScripts we create
        if (logicalName.getText().trim().length() == 0) {
            warnings.append(Messages.getString("TableEditPanel.blankTableNameWarning")); //$NON-NLS-1$
            
        }
        if (pkName.isEnabled() &&
                pkName.getText().trim().length() == 0) {
            warnings.append(Messages.getString("TableEditPanel.blankPkNameWarning"));                 //$NON-NLS-1$
        }
        return warnings.toString();
    }

	public void discardChanges() {
	    SQLPowerUtils.unlistenToHierarchy(session.getRootObject(), this);
	    if (tp != null) tp.removeSPListener(this);
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	 /**
     * For testing only
     * @return the String currently in the logicalName textField
     */
    public String getNameText() {
        return logicalName.getText();
    }
    
    /**
     * For testing only or when initially creating a table.
     * @param newName new logical name for the table
     */
    public void setNameText(String newName) {
        logicalName.setText(newName);
    }
    
    /**
     * For testing only
     * @return the String currently in the pkName textField
     */
    public String getPkNameText() {
        return pkName.getText();
    }
    
    /**
     * For testing only or when initially creating a table.
     * @param newPKName new primaryKeyName for the table
     */
    public void setPkNameText(String newPkName) {
        pkName.setText(newPkName);
    }
    
    /**
     * For testing only
     * @return the String currently in the physicalName textField
     */
    public String getPhysicalNameTest() {
        return physicalName.getText();
    }
    
    /**
     * For testing only or when initially creating a table.
     * @param newPhysicalName new physical name for the table
     */
    public void setPhysicalNameText(String newPhysicalName) {
        physicalName.setText(newPhysicalName);
    }
    
    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

    public void childAdded(SPChildEvent e) {
        // no-op
    }

    /**
     * Checks to see if its respective table is removed from playpen. If yes,
     * exit the editing dialog window.
     */
    public void childRemoved(SPChildEvent e) {
        logger.debug("SQLObject children got removed: " + e); //$NON-NLS-1$
        if (table.equals(e.getChild())) {
            SQLPowerUtils.unlistenToHierarchy(session.getRootObject(), this);
            tp.removeSPListener(this);
            if (editDialog != null) {
                editDialog.dispose();
            }
        }
    }

    public void propertyChanged(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        
        boolean foundError = false;
        
        if (e.getSource() == table) {
            if (property.equals("name")) {
                foundError = DataEntryPanelChangeUtil.incomingChange(logicalName, e);
            } else if (property.equals("physicalName")) {
                foundError = DataEntryPanelChangeUtil.incomingChange(physicalName, e);
            } else if (property.equals("pkName")) {
                foundError = DataEntryPanelChangeUtil.incomingChange(pkName, e);
            } else if (property.equals("remarks")) {   
                foundError = DataEntryPanelChangeUtil.incomingChange(remarks, e);
            }
        } else if (e.getSource() == tp) {
            if (property.equals("backgroundColor")) {
                foundError = DataEntryPanelChangeUtil.incomingChange(bgColor, e);
            } else if (property.equals("foregroundColor")) {
                foundError = DataEntryPanelChangeUtil.incomingChange(fgColor, e);
            } else if (property.equals("rounded")) {
                foundError = DataEntryPanelChangeUtil.incomingChange(rounded, e);
            } else if (property.equals("dashed")) {
                foundError = DataEntryPanelChangeUtil.incomingChange(dashed, e);
            }
        }
        if (foundError) {
            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
        }
    }
    
    public void transactionStarted(TransactionEvent e) {
        // no-op
    }
    
    public void transactionEnded(TransactionEvent e) {
        // no-op
    }
    
    public void transactionRollback(TransactionEvent e) {
        // no-op
    }

    public void setEditDialog(JDialog editDialog) {
        this.editDialog = editDialog;
    }
}