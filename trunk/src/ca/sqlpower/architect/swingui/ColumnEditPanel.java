package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import ca.sqlpower.architect.*;
import java.util.*;
import java.sql.DatabaseMetaData;
import org.apache.log4j.Logger;

public class ColumnEditPanel extends JPanel
	implements ListSelectionListener, ActionListener, ChangeListener, ArchitectPanel {

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

	public ColumnEditPanel(SQLTable table, int idx) throws ArchitectException {
		columns = new JList();
		columns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columns.addListSelectionListener(this);

		setModel(table);

		setLayout(new BorderLayout(12,12));
		add(new JScrollPane(columns), BorderLayout.WEST);

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

		centerPanel.add(new JLabel("Type"));
		centerPanel.add(colType = createColTypeEditor());
		colType.addActionListener(this);

		centerPanel.add(new JLabel("Scale"));
		centerPanel.add(colScale = createScaleEditor());
		colScale.addChangeListener(this);

		centerPanel.add(new JLabel("Precision"));
		centerPanel.add(colPrec = createPrecisionEditor());
		colPrec.addChangeListener(this);

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
		if (model != null) {
			model.getColumnsFolder().removeSQLObjectListener(tableListModel);
		}
		model = newModel;
		tableListModel = new SQLTableListModel(model);
		model.getColumnsFolder().addSQLObjectListener(tableListModel);
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

	public void selectColumn(int index) {
		columns.setSelectedIndex(index);
	}

	/**
	 * Causes the edit panel to edit the properties of the column at
	 * <code>index</code> in the table's child list.
	 */
	protected void editColumn(int index) throws ArchitectException {
		if (index < 0) {
			return;
		}
		
		try {
			changingColumns = true;
			SQLColumn col = model.getColumn(index);
			if (col.getSourceColumn() == null) {
				sourceDB.setText("None Specified");
				sourceTableCol.setText("None Specified");
			} else {
				StringBuffer sb = new StringBuffer();
				SQLObject so = col.getSourceColumn().getParent().getParent();
				while (so != null) {
					sb.insert(0, so.getName());
					sb.insert(0, ".");
					so = so.getParent();
				}
				sourceDB.setText(sb.toString().substring(1));
				sourceTableCol.setText(col.getSourceColumn().getParent().getName()
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
			|| (colInPK.isSelected() && pkSize(model) == 1) ) {

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
			col.setPrimaryKeySeq(colInPK.isSelected() ? new Integer(pkSize(model)) : null);
			col.setAutoIncrement(colAutoInc.isSelected());
		} catch (ArchitectException ex) {
			JOptionPane.showMessageDialog(this, "Couldn't update column information");
			logger.error("Couldn't update model!", ex);
		}
	}
	
	protected int pkSize(SQLTable t) {
		int size = 0;
		try {
			Iterator it = t.getChildren().iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (o instanceof SQLColumn && ((SQLColumn) o).getPrimaryKeySeq() != null) {
					size++;
				}
			}
		} catch (ArchitectException e) {
			logger.error("Got exception calculating pk size", e);
		}
		return size;
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
	 * Does nothing because this version of ColumnEditPanel works
	 * directly on the live data.
	 */
	public void applyChanges() {
		cleanup();
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
}
