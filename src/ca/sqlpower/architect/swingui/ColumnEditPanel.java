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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLType;
import ca.sqlpower.swingui.DataEntryPanel;

public class ColumnEditPanel extends JPanel
	implements SQLObjectListener, ActionListener, DataEntryPanel {

	private static final Logger logger = Logger.getLogger(ColumnEditPanel.class);

    /**
     * The column we're editing.
     */
    private SQLColumn column;
	
	/**
	 * The frame of the column edit dialog.
	 */
	private JDialog editDialog;
	
	private JLabel sourceDB;
	private JLabel sourceTableCol;
	private JTextField colName;
	private JComboBox colType;
	private JSpinner colScale;
	private JSpinner colPrec;
	private JCheckBox colNullable;
	private JTextField colRemarks;
	private JTextField colDefaultValue;
	private JCheckBox colInPK;
	private JCheckBox colAutoInc;
    private JTextField colAutoIncSequenceName;

    /**
     * The prefix string that comes before the current column name
     * in the sequence name.  This is set via the {@link #discoverSequenceNamePattern()}
     * method, which should be called automatically whenever the user
     * changes the sequence name.
     */
    private String seqNamePrefix;

    /**
     * The suffix string that comes after the current column name
     * in the sequence name.  This is set via the {@link #discoverSequenceNamePattern()}
     * method, which should be called automatically whenever the user
     * changes the sequence name.
     */
    private String seqNameSuffix;

	public ColumnEditPanel(SQLColumn col, ArchitectSwingSession session) throws ArchitectException {
		super(new BorderLayout(12,12));
		logger.debug("ColumnEditPanel called"); //$NON-NLS-1$
        buildUI();
		editColumn(col);
		ArchitectUtils.listenToHierarchy(this, session.getRootObject());
	}

    private void buildUI() {
        JPanel centerBox = new JPanel();
		centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
		centerBox.add(Box.createVerticalGlue());
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FormLayout(5, 5));

		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.sourceDb"))); //$NON-NLS-1$
		centerPanel.add(sourceDB = new JLabel());

		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.sourceDbColumn"))); //$NON-NLS-1$
		centerPanel.add(sourceTableCol = new JLabel());

		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.name"))); //$NON-NLS-1$
		centerPanel.add(colName = new JTextField());


		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.type"))); //$NON-NLS-1$
		centerPanel.add(colType = createColTypeEditor());
		colType.addActionListener(this);

		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.precision"))); //$NON-NLS-1$
		centerPanel.add(colPrec = createPrecisionEditor());
		
		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.scale"))); //$NON-NLS-1$
		centerPanel.add(colScale = createScaleEditor());

		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.inPrimaryKey"))); //$NON-NLS-1$
		centerPanel.add(colInPK = new JCheckBox());
		colInPK.addActionListener(this);

		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.allowsNulls"))); //$NON-NLS-1$
		centerPanel.add(colNullable = new JCheckBox());
		colNullable.addActionListener(this);

        centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.autoIncrement"))); //$NON-NLS-1$
        centerPanel.add(colAutoInc = new JCheckBox());
        colAutoInc.addActionListener(this);

        centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.sequenceName"))); //$NON-NLS-1$
        centerPanel.add(colAutoIncSequenceName = new JTextField());
        centerPanel.add(new JLabel("")); //$NON-NLS-1$
        centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.noteOnSequences"))); //$NON-NLS-1$
        
        // Listener to update the sequence name when the column name changes
        colName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { doSync(); }
            public void insertUpdate(DocumentEvent e) { doSync(); }
            public void removeUpdate(DocumentEvent e) { doSync(); }
            private void doSync() {
                syncSequenceName();
            }
        });

        // Listener to rediscover the sequence naming convention, and reset the sequence name
        // to its original (according to the column's own sequence name) naming convention when
        // the user clears the sequence name field
        colAutoIncSequenceName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (colAutoIncSequenceName.getText().trim().equals("")) { //$NON-NLS-1$
                    colAutoIncSequenceName.setText(column.getAutoIncrementSequenceName());
                    discoverSequenceNamePattern(column.getName());
                    syncSequenceName();
                } else {
                    discoverSequenceNamePattern(colName.getText());
                }
            }
        });
        
        
		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.remarks"))); //$NON-NLS-1$
		centerPanel.add(colRemarks = new JTextField());
	
		centerPanel.add(new JLabel(Messages.getString("ColumnEditPanel.defaultValue"))); //$NON-NLS-1$
		centerPanel.add(colDefaultValue = new JTextField());
		colDefaultValue.addActionListener(this);
		
		
		Dimension maxSize = centerPanel.getLayout().preferredLayoutSize(centerPanel);
		maxSize.width = Integer.MAX_VALUE;
		centerPanel.setMaximumSize(maxSize);
		centerBox.add(centerPanel);
		centerBox.add(Box.createVerticalGlue());
		add(centerBox, BorderLayout.CENTER);
    }

	private JSpinner createScaleEditor() {
		return new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
	} 

	private JSpinner createPrecisionEditor() {
		return createScaleEditor();  // looks better if both spinners are same size
	}

	private JComboBox createColTypeEditor() {
		return new JComboBox(SQLType.getTypes());
	}

	/**
	 * Updates all the UI components to reflect the given column's properties.
     * Also saves a reference to the given column so the changes made in the
     * UI can be written back into the column.
     * 
     * @param col The column to edit
	 */
	public void editColumn(SQLColumn col) throws ArchitectException {
		logger.debug("Edit Column '"+col+"' is being called"); //$NON-NLS-1$ //$NON-NLS-2$
        if (col == null) throw new NullPointerException("Edit null column is not allowed"); //$NON-NLS-1$
        column = col;
		if (col.getSourceColumn() == null) {
			sourceDB.setText(Messages.getString("ColumnEditPanel.noneSpecified")); //$NON-NLS-1$
			sourceTableCol.setText(Messages.getString("ColumnEditPanel.noneSpecified")); //$NON-NLS-1$
		} else {
			StringBuffer sourceDBSchema = new StringBuffer();
			SQLObject so = col.getSourceColumn().getParentTable().getParent();
			while (so != null) {
				sourceDBSchema.insert(0, so.getName());
				sourceDBSchema.insert(0, "."); //$NON-NLS-1$
				so = so.getParent();
			}
			sourceDB.setText(sourceDBSchema.toString().substring(1));
			sourceTableCol.setText(col.getSourceColumn().getParentTable().getName()
								   +"."+col.getSourceColumn().getName()); //$NON-NLS-1$
		}
		colName.setText(col.getName());
		colType.setSelectedItem(SQLType.getType(col.getType()));
		colScale.setValue(new Integer(col.getScale()));
		colPrec.setValue(new Integer(col.getPrecision()));
		colNullable.setSelected(col.getNullable() == DatabaseMetaData.columnNullable);
		colRemarks.setText(col.getRemarks());
		colDefaultValue.setText(col.getDefaultValue());
		colInPK.setSelected(col.getPrimaryKeySeq() != null);
		colAutoInc.setSelected(col.isAutoIncrement());
        colAutoIncSequenceName.setText(col.getAutoIncrementSequenceName());
		updateComponents();
        discoverSequenceNamePattern(col.getName());
		colName.requestFocus();
		colName.selectAll();
	}

    /**
     * Figures out what the sequence name prefix and suffix strings are,
     * based on the current contents of the sequence name and column name
     * fields.
     */
    private void discoverSequenceNamePattern(String colName) {
        String seqName = this.colAutoIncSequenceName.getText();
        int prefixEnd = seqName.indexOf(colName);
        if (prefixEnd >= 0 && colName.length() > 0) {
            seqNamePrefix = seqName.substring(0, prefixEnd);
            seqNameSuffix = seqName.substring(prefixEnd + colName.length());
        } else {
            seqNamePrefix = null;
            seqNameSuffix = null;
        }
    }
    
    /**
     * Modifies the contents of the "auto-increment sequence name" field to
     * match the naming scheme as it is currently understood. This modification
     * is only performed if the naming scheme has been successfully determined
     * by the {@link #discoverSequenceNamePattern(String)} method. The new
     * sequence name is written directly to the {@link #colAutoIncSequenceName}
     * field.
     */
    private void syncSequenceName() {
        if (seqNamePrefix != null && seqNameSuffix != null) {
            String newName = seqNamePrefix + colName.getText() + seqNameSuffix;
            colAutoIncSequenceName.setText(newName);
        }
    }
    
	/**
	 * Implementation of ActionListener.
	 */
	public void actionPerformed(ActionEvent e) {
		logger.debug("action event "+e); //$NON-NLS-1$
		updateComponents();
	}

	/**
	 * Implementation of ChangeListener.
	 */
	public void stateChanged(ChangeEvent e) {
		logger.debug("State change event "+e); //$NON-NLS-1$
	}
	
	/**
	 * Examines the components and makes sure they're in a consistent
	 * state (they are legal with respect to the model).
	 */
	private void updateComponents() {
		// allow nulls is free unless column is in PK 
		if (colInPK.isSelected()) {
			colNullable.setEnabled(false);
		} else {
			colNullable.setEnabled(true);
		}

		// primary key is free unless column allows nulls
		if (colNullable.isSelected()) {
			colInPK.setEnabled(false);
		} else {
			colInPK.setEnabled(true);
		}

		if (colInPK.isSelected() && colNullable.isSelected()) {
		    //this should not be physically possible
		    colNullable.setSelected(false);
		    colNullable.setEnabled(false);
		}
		
		if (colAutoInc.isSelected()) {
		    colDefaultValue.setText(""); //$NON-NLS-1$
		    colDefaultValue.setEnabled(false);
		} else {
		    colDefaultValue.setEnabled(true);
		}
        
        colAutoIncSequenceName.setEnabled(colAutoInc.isSelected());
	}
	
	/**
	 * Sets the properties of the current column in the model to match
	 * those on screen.
     * 
     * @return A list of error messages if the update was not successful.
	 */
    private List<String> updateModel() {
        logger.debug("Updating model"); //$NON-NLS-1$
        List<String> errors = new ArrayList<String>();
        try {
            column.startCompoundEdit("Column Edit Panel Changes"); //$NON-NLS-1$
            if (colName.getText().trim().length() == 0) {
                errors.add(Messages.getString("ColumnEditPanel.columnNameRequired")); //$NON-NLS-1$
            } else {
                column.setName(colName.getText());
            }
            column.setType(((SQLType) colType.getSelectedItem()).getType());
            column.setScale(((Integer) colScale.getValue()).intValue());
            column.setPrecision(((Integer) colPrec.getValue()).intValue());
            column.setNullable(colNullable.isSelected()
                    ? DatabaseMetaData.columnNullable
                            : DatabaseMetaData.columnNoNulls);
            column.setRemarks(colRemarks.getText());
            if (!(column.getDefaultValue() == null && colDefaultValue.getText().equals(""))) //$NON-NLS-1$
            {
                column.setDefaultValue(colDefaultValue.getText());
            }
            // Autoincrement has to go before the primary key or 
            // this column will never allow nulls
            column.setAutoIncrement(colAutoInc.isSelected());
            if (column.getPrimaryKeySeq() == null) {
                column.setPrimaryKeySeq(colInPK.isSelected() ? new Integer(column.getParentTable().getPkSize()) : null);
            } else {
                column.setPrimaryKeySeq(colInPK.isSelected() ? new Integer(column.getPrimaryKeySeq()) : null);
            }
            column.setAutoIncrementSequenceName(colAutoIncSequenceName.getText());
        } finally {
            column.endCompoundEdit("Column Edit Panel Changes"); //$NON-NLS-1$
        }
        return errors;
    }
	
	// ------------------ ARCHITECT PANEL INTERFACE ---------------------
	
	/**
	 * Calls updateModel since the user may have clicked "ok" before
	 * hitting enter on a text field.
	 */
	public boolean applyChanges() {
		List<String> errors = updateModel();
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this, errors.toString());
            return false;
        } else {
            return true;
        }
	}

	/**
	 * Does nothing.  The column's properties will not have been
     * modified.
	 */
	public void discardChanges() {
	}
	
	public JPanel getPanel() {
		return this;
	}
	
	// THESE GETTERS ARE TO BE USED FOR TESTING ONLY
	public JCheckBox getColAutoInc() {
		return colAutoInc;
	}

	public JTextField getColDefaultValue() {
		return colDefaultValue;
	}

	public JCheckBox getColInPK() {
		return colInPK;
	}

	public JTextField getColName() {
		return colName;
	}

	public JCheckBox getColNullable() {
		return colNullable;
	}

	public JSpinner getColPrec() {
		return colPrec;
	}

	public JTextField getColRemarks() {
		return colRemarks;
	}

	public JSpinner getColScale() {
		return colScale;
	}

	public JComboBox getColType() {
		return colType;
	}

	public JLabel getSourceDB() {
		return sourceDB;
	}

	public JLabel getSourceTableCol() {
		return sourceTableCol;
	}

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }
    
    // -----------------Methods from SQLObjectListener------------------- //

    /**
     * Listens for property changes in the model (tables
     * added).  If this change affects the appearance of
     * this widget, we will notify all change listeners (the UI
     * delegate) with a ChangeEvent.
     */
    public void dbChildrenInserted(SQLObjectEvent e) {
        logger.debug("SQLObject children got inserted: "+e); //$NON-NLS-1$
    }

    public void dbChildrenRemoved(SQLObjectEvent e) {
        
        logger.debug("SQLObject children got removed: "+e); //$NON-NLS-1$
        boolean itemDeleted = false;
        SQLObject[] c = e.getChildren();

        
        for (int i = 0; i < c.length; i++) {
            
            try {
                if(this.column.equals(c[i])) {
                    itemDeleted = true;
                    break;
                }
                else if(c[i] instanceof SQLTable) {
                    for(int j = 0; j < c[i].getChildCount(); j++) {
                        for (int k = 0; k < c[i].getChild(j).getChildCount(); k++){
                            if(this.column.equals(c[i].getChild(j).getChild(k))) {
                                itemDeleted = true;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Could not compare the removed sql objects.", ex); //$NON-NLS-1$
            }
        }
        if(itemDeleted) {
            if(this.editDialog != null) {
                this.editDialog.setVisible(false);
            }
            itemDeleted = false;
        }
    }

    public void dbObjectChanged(SQLObjectEvent e) {
        // TODO Auto-generated method stub
       
    }

    public void dbStructureChanged(SQLObjectEvent e) {
        // TODO Auto-generated method stub
       
    }
   
    /**
     * For others to pass in a reference of the jframe which column 
     * edit panel resides in.
     * @param editDialog
     */
    public void setEditDialog(JDialog editDialog) {
        this.editDialog = editDialog;
    }

}
  