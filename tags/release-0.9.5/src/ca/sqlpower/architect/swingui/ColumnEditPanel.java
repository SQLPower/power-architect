/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;

public class ColumnEditPanel extends JPanel
	implements ActionListener, ArchitectPanel {

	private static final Logger logger = Logger.getLogger(ColumnEditPanel.class);

    /**
     * The column we're editing.
     */
    private SQLColumn column;
    
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

	public ColumnEditPanel(SQLColumn col) throws ArchitectException {
		super(new BorderLayout(12,12));
		logger.debug("ColumnEditPanel called");

        buildUI();
		editColumn(col);		
	}

    private void buildUI() {
        JPanel centerBox = new JPanel();
		centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
		centerBox.add(Box.createVerticalGlue());
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FormLayout(5, 5));
		centerPanel.setBorder(BorderFactory.createTitledBorder("Column Properties"));

		centerPanel.add(new JLabel("Source Database"));
		centerPanel.add(sourceDB = new JLabel());

		centerPanel.add(new JLabel("Source Table.Column"));
		centerPanel.add(sourceTableCol = new JLabel());

		centerPanel.add(new JLabel("Name"));
		centerPanel.add(colName = new JTextField());


		centerPanel.add(new JLabel("Type"));
		centerPanel.add(colType = createColTypeEditor());
		colType.addActionListener(this);

		centerPanel.add(new JLabel("Precision"));
		centerPanel.add(colPrec = createPrecisionEditor());
		
		centerPanel.add(new JLabel("Scale"));
		centerPanel.add(colScale = createScaleEditor());

		centerPanel.add(new JLabel("In Primary Key"));
		centerPanel.add(colInPK = new JCheckBox());
		colInPK.addActionListener(this);

		centerPanel.add(new JLabel("Allows Nulls"));
		centerPanel.add(colNullable = new JCheckBox());
		colNullable.addActionListener(this);

		centerPanel.add(new JLabel("Auto Increment"));
		centerPanel.add(colAutoInc = new JCheckBox());
		colAutoInc.addActionListener(this);

		centerPanel.add(new JLabel("Remarks"));
		centerPanel.add(colRemarks = new JTextField());
	
		centerPanel.add(new JLabel("Default Value"));
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
		return new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
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
	 */
	public void editColumn(SQLColumn col) throws ArchitectException {
		logger.debug("Edit Column '"+col+"' is being called");
        if (col == null) throw new NullPointerException("Edit null column is not allowed");
        column = col;
		if (col.getSourceColumn() == null) {
			sourceDB.setText("None Specified");
			sourceTableCol.setText("None Specified");
		} else {
			StringBuffer sourceDBSchema = new StringBuffer();
			SQLObject so = col.getSourceColumn().getParentTable().getParent();
			while (so != null) {
				sourceDBSchema.insert(0, so.getName());
				sourceDBSchema.insert(0, ".");
				so = so.getParent();
			}
			sourceDB.setText(sourceDBSchema.toString().substring(1));
			sourceTableCol.setText(col.getSourceColumn().getParentTable().getName()
								   +"."+col.getSourceColumn().getName());
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
		updateComponents();
		colName.requestFocus();
	}

	/**
	 * Implementation of ActionListener.
	 */
	public void actionPerformed(ActionEvent e) {
		logger.debug("action event "+e);
		updateComponents();
	}

	/**
	 * Implementation of ChangeListener.
	 */
	public void stateChanged(ChangeEvent e) {
		logger.debug("State change event "+e);
	}
	
	/**
	 * Examines the components and makes sure they're in a consistent
	 * state (they are legal with respect to the model).
	 */
	private void updateComponents() {
		// allow nulls is free unless column is in PK or is autoincrement
		if (colInPK.isSelected() || colAutoInc.isSelected()) {
			colNullable.setEnabled(false);
		} else {
			colNullable.setEnabled(true);
		}

		// primary key is free unless column allows nulls and isn't autoinc
		if (colNullable.isSelected() && !colAutoInc.isSelected()) {
			colInPK.setEnabled(false);
		} else {
			colInPK.setEnabled(true);
		}

		// default value is free unless column is autoinc or the only column in PK
		if (colAutoInc.isSelected() 
			|| (colInPK.isSelected()
              && (column.getParentTable() != null && column.getParentTable().getPkSize() == 1) )) {

			colDefaultValue.setEnabled(false);
			colDefaultValue.setText(null);
		} else {
			colDefaultValue.setEnabled(true);
		}

		// auto increment is free unless column has default value or disallows nulls
		// or it's in the PK and allows nulls
		if ( (colDefaultValue.getText() != null && colDefaultValue.getText().length() > 0)
			 || !colNullable.isSelected()
			 || (colInPK.isSelected() && colNullable.isSelected())) {

			colAutoInc.setEnabled(false);
		} else {
			colAutoInc.setEnabled(true);
		}

		// cleanup inconsistent state
		if (colAutoInc.isSelected()) {
			colNullable.setSelected(true);
		}

		if (colInPK.isSelected() && !colNullable.isSelected()) {
			colAutoInc.setEnabled(true);
		}
	}
	
	/**
	 * Sets the properties of the current column in the model to match
	 * those on screen.
     * 
     * @return A list of error messages if the update was not successful.
	 */
    private List<String> updateModel() {
        logger.debug("Updating model");
        List<String> errors = new ArrayList<String>();
        try {
            column.startCompoundEdit("Column Edit Panel Changes");
            if (colName.getText().trim().length() == 0) {
                errors.add("Column name is required");
            } else {
                column.setName(colName.getText());
            }
            column.setType(((SQLType) colType.getSelectedItem()).type);
            column.setScale(((Integer) colScale.getValue()).intValue());
            column.setPrecision(((Integer) colPrec.getValue()).intValue());
            column.setNullable(colNullable.isSelected()
                    ? DatabaseMetaData.columnNullable
                            : DatabaseMetaData.columnNoNulls);
            column.setRemarks(colRemarks.getText());
            if (!(column.getDefaultValue() == null && colDefaultValue.getText().equals("")))
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
        } finally {
            column.endCompoundEdit("Column Edit Panel Changes");
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
}
  