package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import ca.sqlpower.architect.*;
import java.sql.DatabaseMetaData;
import org.apache.log4j.Logger;

public class ColumnEditPanel extends JPanel
	implements ListSelectionListener, ListDataListener, ActionListener, ChangeListener, ArchitectPanel, DocumentListener {

	private static final Logger logger = Logger.getLogger(ColumnEditPanel.class);

	protected JList columns;
	protected SQLTable model;
	protected SQLTableListModel tableListModel;

	/**
	 * This is set to true while we change columns so that events can be ignored.
	 */
	protected boolean changingColumns;


	protected JLabel sourceDB;
	protected JLabel sourceTableCol;
	protected JTextField colName;
	protected JComboBox colType;
	protected JSpinner colScale;
	protected JSpinner colPrec;
	protected JCheckBox colNullable;
	protected JTextField colRemarks;
	protected JTextField colDefaultValue;
	protected JCheckBox colInPK;
	protected JCheckBox colAutoInc;

	protected JButton addColumnButton;
	protected JButton deleteColumnButton;

	public ColumnEditPanel(SQLTable table, int idx) throws ArchitectException {
		super(new BorderLayout(12,12));
		
		JPanel westPanel = new JPanel(new BorderLayout());
		columns = new JList();
		columns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columns.addListSelectionListener(this);
		westPanel.add(new JScrollPane(columns), BorderLayout.CENTER);

		setModel(table);

		JPanel addDelPanel = new JPanel(new FlowLayout());
		addDelPanel.add(addColumnButton = new JButton("Add"));
		addColumnButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int idx = columns.getSelectedIndex();
					if (idx < 0) {
						try {
							idx = model.getColumns().size();
						} catch (ArchitectException ex) {
							logger.error("Couldn't count number of columns", ex);
							JOptionPane.showMessageDialog
								(ColumnEditPanel.this,
								 "Couldn't count number of columns: "+ex.getMessage());
							idx = 0;
						}
					} else {
						idx++; // add after selected column
					}
					SQLColumn col = new SQLColumn();
					col.setColumnName("new column");
					
					try {
						model.addColumn(idx, col);
						columns.setSelectedIndex(idx);
					} catch (ArchitectException ex) {
						logger.error("Couldn't add column "+col+" to table "+model.getName(), ex);
						JOptionPane.showMessageDialog
						(ColumnEditPanel.this,
						 "Couldn't add column to table: "+ex.getMessage());
					}
				}
			});
		addDelPanel.add(deleteColumnButton = new JButton("Del"));
		deleteColumnButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int idx = columns.getSelectedIndex();
					if (idx < 0) {
						JOptionPane.showMessageDialog(ColumnEditPanel.this,
													  "Please select a column, then click delete");
					} else {
						try {
							int size = model.getColumns().size();
							if (idx == size - 1) {
								columns.setSelectedIndex(size-2);
								model.removeColumn(idx);
							} else {
								model.removeColumn(idx);
								columns.setSelectedIndex(idx);
							}
						} catch (LockedColumnException ex) {
							JOptionPane.showMessageDialog
								(ColumnEditPanel.this,
								 ex.getMessage());
						} catch (ArchitectException ex) {
							logger.error("Couldn't count number of columns", ex);
							JOptionPane.showMessageDialog
								(ColumnEditPanel.this,
								 "Couldn't count number of columns: "+ex.getMessage());
						}
					}
				}
			});
		westPanel.add(addDelPanel, BorderLayout.SOUTH);

		add(westPanel, BorderLayout.WEST);
		
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
		colName.addActionListener(this);
		colName.getDocument().addDocumentListener(this);

		centerPanel.add(new JLabel("Type"));
		centerPanel.add(colType = createColTypeEditor());
		colType.addActionListener(this);

		centerPanel.add(new JLabel("Precision"));
		centerPanel.add(colPrec = createPrecisionEditor());
		colPrec.addChangeListener(this);

		centerPanel.add(new JLabel("Scale"));
		centerPanel.add(colScale = createScaleEditor());
		colScale.addChangeListener(this);

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
		colRemarks.addActionListener(this);

		centerPanel.add(new JLabel("Default Value"));
		centerPanel.add(colDefaultValue = new JTextField());
		colDefaultValue.addActionListener(this);
		
		Dimension maxSize = centerPanel.getLayout().preferredLayoutSize(centerPanel);
		maxSize.width = Integer.MAX_VALUE;
		centerPanel.setMaximumSize(maxSize);
		centerBox.add(centerPanel);
		centerBox.add(Box.createVerticalGlue());
		add(centerBox, BorderLayout.CENTER);

		// select the default column
		columns.setSelectedIndex(idx);
	}

	/**
	 * You should call selectColumn with a nonnegative index after calling setModel.
	 */
	public void setModel(SQLTable newModel) {
		// detach old model
		if (model != null) {
			model.getColumnsFolder().removeSQLObjectListener(tableListModel);
			try {
				for (int i = 0; i < model.getColumnsFolder().getChildCount(); i++) {
					model.getColumnsFolder().getChild(i).removeSQLObjectListener(tableListModel);
				}
			} catch (ArchitectException e) {
				logger.error("Caught exception removing treemodel from column listener list");
			}
		}
		if (tableListModel != null) {
			tableListModel.removeListDataListener(this);
		}

		// create and attach new model
		model = newModel;
		tableListModel = new SQLTableListModel(model);
		model.getColumnsFolder().addSQLObjectListener(tableListModel);
		tableListModel.addListDataListener(this);
		try {
			for (int i = 0; i < model.getColumnsFolder().getChildCount(); i++) {
				model.getColumnsFolder().getChild(i).addSQLObjectListener(tableListModel);
			}
		} catch (ArchitectException e) {
			logger.error("Caught exception adding treemodel to column listener list");
		}
		columns.setModel(tableListModel);
	}

	protected JSpinner createScaleEditor() {
		return new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
	}

	protected JSpinner createPrecisionEditor() {
		return createScaleEditor();  // looks better if both spinners are same size
	}

	protected JComboBox createColTypeEditor() {
		return new JComboBox(SQLType.getTypes());
	}

	/**
	 * Implementation of ListSelectionListener.  Makes calls to
	 * editColumn when a new list item (SQLColumn) is selected by the
	 * user.
	 */
	public void valueChanged(ListSelectionEvent e) {
		logger.debug("valueChanged event "+e);
		if (!e.getValueIsAdjusting()) {
			try {
				editColumn(((JList) e.getSource()).getSelectedIndex());
			} catch (ArchitectException ex) {
				JOptionPane.showMessageDialog(this, "Can't retrieve column definition");
				logger.error("Exception while trying to edit different column", ex);
			}
		}
	}

	// ------------------------ LIST DATA LISTENER -----------------------
	public void contentsChanged(ListDataEvent e) {
        // we don't care
	}

	public void intervalAdded(ListDataEvent e) {
        // we don't care
	}

	public void intervalRemoved(ListDataEvent e) {
		try {
			if (model.getColumns().size() == 0) {
				Component c = getParent();
				while ( ! (c instanceof Window)) {
					c = c.getParent();
				}
				c.setVisible(false);
			} else {
				editColumn(columns.getSelectedIndex());
			}
		} catch (ArchitectException ex) {
			JOptionPane.showMessageDialog(this, "Can't edit the selected column");
			logger.error("Can't edit the selected column", ex);
		}
	}


	public void selectColumn(int index) {
		columns.setSelectedIndex(index);
	}

	/**
	 * Causes the edit panel to update the properties of the column at
	 * <code>index</code> in the table's child list.
	 */
	protected void editColumn(int index) throws ArchitectException {
		if (index < 0) {
			return;
		}
		
		try {
			changingColumns = true;
			
			// syncronize the selected column with the source of the data
			// for the edit column properties fields
			columns.setSelectedIndex(index);
			SQLColumn col = model.getColumn(index);
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
		} finally {
			changingColumns = false;
		}
		updateComponents();
		colName.requestFocus();
	}

	/**
	 * Implementation of ActionListener.
	 */
	public void actionPerformed(ActionEvent e) {
		if (changingColumns) return;
		logger.debug("action event "+e);
		updateComponents();
		updateModel();
	}

	/**
	 * Implementation of ChangeListener.
	 */
	public void stateChanged(ChangeEvent e) {
		if (changingColumns) return;
		logger.debug("State change event "+e);
		updateModel();
	}
	
	/**
	 * Examines the components and makes sure they're in a consistent
	 * state (they are legal with respect to the model).
	 */
	public void updateComponents() {
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
			|| (colInPK.isSelected() && model.getPkSize() == 1) ) {

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
	 */
	protected void updateModel() {
		logger.debug("Updating model");
		try {
			SQLColumn col = model.getColumn(columns.getSelectedIndex());
			col.setColumnName(colName.getText());
			col.setType(((SQLType) colType.getSelectedItem()).type);
			col.setScale(((Integer) colScale.getValue()).intValue());
			col.setPrecision(((Integer) colPrec.getValue()).intValue());
			col.setNullable(colNullable.isSelected()
							? DatabaseMetaData.columnNullable
							: DatabaseMetaData.columnNoNulls);
			col.setRemarks(colRemarks.getText());
			col.setDefaultValue(colDefaultValue.getText());
			col.setPrimaryKeySeq(colInPK.isSelected() ? new Integer(model.getPkSize()) : null);
			col.setAutoIncrement(colAutoInc.isSelected());

			// update selected index in case the column moved (add/remove PK)
			int index = model.getColumns().indexOf(col);
			selectColumn(index);
		} catch (ArchitectException ex) {
			JOptionPane.showMessageDialog(this, "Couldn't update column information");
			logger.error("Couldn't update model!", ex);
		}
	}
	
	/**
	 * Make sure to call this when this component goes away: it
	 * removes this component from listener lists!
	 */
	protected void cleanup() {
		try {
			for (int i = 0; i < model.getColumnsFolder().getChildCount(); i++) {
				model.getColumnsFolder().getChild(i).removeSQLObjectListener(tableListModel);
			}
		} catch (ArchitectException e) {
			logger.error("Caught exception removing treemodel from column listener list");
		}
		model.getColumnsFolder().removeSQLObjectListener(tableListModel);
	}

	// ------------------ ARCHITECT PANEL INTERFACE ---------------------
	
	/**
	 * Calls updateModel since the user may have clicked "ok" before
	 * hitting enter on a text field.
	 */
	public boolean applyChanges() {
		updateModel();
		cleanup();
		return true;
	}

	/**
	 * Does nothing because this version of ColumnEditPanel works
	 * directly on the live data.
	 *
	 * <p>XXX: in architect version 2, this will undo the changes to
	 * the model.
	 */
	public void discardChanges() {
		cleanup();
	}

	// -------------------- Document Listener ----------------

	/**
	 * Gets updates from column name JTextField and updates the
	 * model's column name accordingly.
	 */
	public void insertUpdate(DocumentEvent e) {
		try {
			if (columns.getSelectedIndex() >= 0) {
				SQLColumn col = model.getColumn(columns.getSelectedIndex());
				col.setColumnName(colName.getText());
			}
		} catch (ArchitectException ex) {
			logger.error("Couldn't update column name", ex);
			JOptionPane.showMessageDialog(this, "Can't update column name: "+ex.getMessage());
		}
	}
		
	/**
	 * Gets updates from column name JTextField and updates the
	 * model's column name accordingly.
	 */
	public void removeUpdate(DocumentEvent e) {
		try {
			if (columns.getSelectedIndex() >= 0) {
				SQLColumn col = model.getColumn(columns.getSelectedIndex());
				col.setColumnName(colName.getText());
			}
		} catch (ArchitectException ex) {
			logger.error("Couldn't update column name", ex);
			JOptionPane.showMessageDialog(this, "Can't update column name: "+ex.getMessage());
		}
	}

	/**
	 * Gets updates from column name JTextField and updates the
	 * model's column name accordingly.
	 */
	public void changedUpdate(DocumentEvent e) {
		try {
			if (columns.getSelectedIndex() >= 0) {
				SQLColumn col = model.getColumn(columns.getSelectedIndex());
				col.setColumnName(colName.getText());
			}
		} catch (ArchitectException ex) {
			logger.error("Couldn't update column name", ex);
			JOptionPane.showMessageDialog(this, "Can't update column name: "+ex.getMessage());
		}
	}
}
