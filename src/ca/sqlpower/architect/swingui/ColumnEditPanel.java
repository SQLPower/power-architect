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

public class ColumnEditPanel extends JPanel implements ListSelectionListener, ActionListener {

	private static final Logger logger = Logger.getLogger(ColumnEditPanel.class);

	protected JList columns;
	protected SQLTable model;
	protected SQLTableListModel tableListModel;

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
		model = table;

		setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		setLayout(new BorderLayout(12,12));
		tableListModel = new SQLTableListModel(model);
		model.addSQLObjectListener(tableListModel);
		columns = new JList(tableListModel);
		columns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columns.addListSelectionListener(this);
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
		centerPanel.add(new JLabel("Type"));
		centerPanel.add(colType = createColTypeEditor());
		centerPanel.add(new JLabel("Scale"));
		centerPanel.add(colScale = createScaleEditor());
		centerPanel.add(new JLabel("Precision"));
		centerPanel.add(colPrec = createPrecisionEditor());
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

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout());
		southPanel.add(new JButton("Ok"));
		southPanel.add(new JButton("Cancel"));
		add(southPanel, BorderLayout.SOUTH);

		// select the default column
		columns.setSelectedIndex(idx);
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

	/**
	 * Causes the edit panel to edit the properties of the column at
	 * <code>index</code> in the table's child list.
	 */
	public void editColumn(int index) throws ArchitectException {
		SQLColumn col = model.getColumn(index);
		if (col.getSourceColumn() == null) {
			sourceDB.setText("None Specified");
			sourceTableCol.setText("None Specified");
		} else {
			sourceDB.setText(col.getSourceColumn().getParent().getParent().getName());
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

		updateComponents();
	}

	/**
	 * Implementation of ChangeListener.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == colInPK) {
			updateComponents();
		} else if (e.getSource() == colAutoInc) {
			updateComponents();
		} else if (e.getSource() == colNullable) {
			updateComponents();
		} else if (e.getSource() == colDefaultValue) {
			updateComponents();
		} else {
			logger.warn("Got an unexpected action event: "+e);
		}
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
		model.removeSQLObjectListener(tableListModel);
	}
}
