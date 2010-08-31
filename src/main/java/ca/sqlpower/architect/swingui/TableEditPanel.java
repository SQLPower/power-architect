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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.ChangeListeningDataEntryPanel;
import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.swingui.ColourScheme;

public class TableEditPanel extends ChangeListeningDataEntryPanel {
    
    private static final Logger logger = Logger.getLogger(TableEditPanel.class);
    
    /**
     * The frame which this table edit panel resides in.
     */
    private JPanel panel;
	private SQLTable table;
	private JTextField logicalName;
	private JTextField physicalName;
	private JTextField pkName;
	private JTextArea remarks;
	private JComboBox bgColor;
	private JComboBox fgColor;
	private JCheckBox rounded;
	private JCheckBox dashed;
	
	private TablePane tablePane;
	
	final HashMap<String, PropertyChangeEvent> propertyConflicts = new HashMap<String, PropertyChangeEvent>();
	
	final HashMap<String, JComponent> propertyFields = new HashMap<String, JComponent>();

    /**
     * A constructor that uses the given table pane to set parameters on instead
     * of looking one up in the play pen.
     * 
     * @param session
     *            The session the table and table pane belong to or will belong
     *            to.
     * @param tp
     *            The table which has a model that this panel will edit.
     */
	public TableEditPanel(ArchitectSwingSession session, TablePane tp) {
	    this(session, tp.getModel());
	    tablePane = tp;
	}
	
	public TableEditPanel(ArchitectSwingSession session, SQLTable t) {
		this.panel = new JPanel(new FormLayout());
		this.tablePane = session.getPlayPen().findTablePane(t);
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
        SQLIndex primaryKeyIndex = t.getPrimaryKeyIndex();
        if (primaryKeyIndex == null) {
            pkName.setEnabled(false);
        } else {
            pkName.setText(primaryKeyIndex.getName());
            pkName.setEnabled(true);
        }
		remarks.setText(t.getRemarks());
		logicalName.selectAll();
		
		if (tablePane != null) {
    		bgColor.setSelectedItem(tablePane.getBackgroundColor());
    		fgColor.setSelectedItem(tablePane.getForegroundColor());
    		dashed.setSelected(tablePane.isDashed());
    		rounded.setSelected(tablePane.isRounded());
		}
	}

	// --------------------- ArchitectPanel interface ------------------
	public boolean applyChanges() {
	    String warnings = generateWarnings();

	    if (warnings.length() == 0) {
	        table.begin(Messages.getString("TableEditPanel.compoundEditName"));		 //$NON-NLS-1$

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

	        table.setPhysicalName(physicalName.getText());
	        table.setName(logicalName.getText());
	        table.setRemarks(remarks.getText());   

	        if (tablePane != null) {
	            tablePane.begin("TableEditPanel.compoundEditName");
	            if (!tablePane.getBackgroundColor().equals((Color)bgColor.getSelectedItem())) {
	                tablePane.setBackgroundColor((Color)bgColor.getSelectedItem());
	            } 
	            if (!tablePane.getForegroundColor().equals((Color)fgColor.getSelectedItem())) {
	                tablePane.setForegroundColor((Color)fgColor.getSelectedItem());
	            } 
	            if (tablePane.isDashed() != dashed.isSelected()) {
	                tablePane.setDashed(dashed.isSelected());
	            } 
	            if (tablePane.isRounded() != rounded.isSelected()) {
	                tablePane.setRounded(rounded.isSelected());
	            }
	            tablePane.commit();
	        }
	        table.commit();
	        return true;
	    } else {
	        JOptionPane.showMessageDialog(panel,warnings);
	        //this is done so we can go back to this dialog after the error message
	        return false;
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
	    // No operation.
	}
	
	public JPanel getPanel() {
		return panel;
	}

    /**
     * Sets the table's logical name field for this panel.
     * 
     * @param newName
     *            new logical name for the table
     */
    public void setNameText(String newName) {
        logicalName.setText(newName);
    }

    /**
     * Sets the table's primary key name field for this panel.
     * 
     * @param newPKName
     *            new primaryKeyName for the table
     */
    public void setPkNameText(String newPkName) {
        pkName.setText(newPkName);
    }

    /**
     * Sets the table's physical name field for this panel.
     * 
     * @param newPhysicalName
     *            new physical name for the table
     */
    public void setPhysicalNameText(String newPhysicalName) {
        physicalName.setText(newPhysicalName);
    }
    
    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

    /**
     * Gets the {@link SQLTable} this panel is editing.
     * 
     * @return The {@link SQLTable}.
     */
    public SQLTable getTable() {
        return table;
    }

    /**
     * Gets the {@link TablePane} for the {@link SQLTable} this panel is
     * editing.
     * 
     * @return The {@link TablePane}. If the table is being created for the
     *         first time, there is no {@link TablePane} and null is returned.
     */
    public TablePane getTablePane() {
        return tablePane;
    }
    
    /**
     * Returns the {@link JComboBox} that picks the table's background colour.
     */
    public JComboBox getBgColor() {
        return bgColor;
    }
    
    /**
     * Returns the {@link JCheckBox} that determines if dashed lines are used. 
     */
    public JCheckBox getDashed() {
        return dashed;
    }

    /**
     * Returns the {@link JComboBox} that picks the table's foreground colour.
     */
    public JComboBox getFgColor() {
        return fgColor;
    }

    /**
     * Returns the {@link JTextField} containing the logical name of the table.
     */
    public JTextField getLogicalName() {
        return logicalName;
    }
    
    /**
     * Returns the {@link JTextField} containing the physical name of the table.
     */
    public JTextField getPhysicalName() {
        return physicalName;
    }

    /**
     * Returns the {@link JTextField} containing the primary key name of the
     * table.
     */
    public JTextField getPkName() {
        return pkName;
    }
    
    /**
     * Returns the {@link JTextArea} containing the table's remarks.
     */
    public JTextArea getRemarks() {
        return remarks;
    }

    /**
     * Returns the {@link JCheckBox} that determines whether the table uses
     * rounded lines.
     */
    public JCheckBox getRounded() {
        return rounded;
    }
    
}