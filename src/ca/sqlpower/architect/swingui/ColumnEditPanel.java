package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DatabaseMetaData;
import java.util.Iterator;
import java.util.LinkedList;

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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

public class ColumnEditPanel extends JPanel
	implements ListDataListener, ActionListener,ArchitectPanel {

	private static final Logger logger = Logger.getLogger(ColumnEditPanel.class);

	private int index;
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
		super(new BorderLayout(12,12));
		logger.debug("ColumnEditPanel called");
		index =idx;		
		addUndoEventListener(ArchitectFrame.getMainInstance().getUndoManager().getEventAdapter());
		setModel(table);
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
		editColumn(index);		
	}

	/**
	 * You should call selectColumn with a nonnegative index after calling setModel.
	 */
	public void setModel(SQLTable newModel) {
		fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.PROPERTY_CHANGE_GROUP_START,"Starting new compound edit event in column edit panel"));
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
				editColumn(index);
			}
		} catch (ArchitectException ex) {
			JOptionPane.showMessageDialog(this, "Can't edit the selected column");
			logger.error("Can't edit the selected column", ex);
		}
	}


	public void selectColumn(int index) throws ArchitectException {
		this.index = index;
		editColumn(index);
	}

	/**
	 * Causes the edit panel to update the properties of the column at
	 * <code>index</code> in the table's child list.
	 */
	private void editColumn(int index) throws ArchitectException {
		if (index < 0) {
			return;
		}
		logger.debug("Edit Column is being called");
		// syncronize the selected column with the source of the data
		// for the edit column properties fields
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
			SQLColumn col = model.getColumn(index);
			col.setName(colName.getText());
			col.setType(((SQLType) colType.getSelectedItem()).type);
			col.setScale(((Integer) colScale.getValue()).intValue());
			col.setPrecision(((Integer) colPrec.getValue()).intValue());
			col.setNullable(colNullable.isSelected()
							? DatabaseMetaData.columnNullable
							: DatabaseMetaData.columnNoNulls);
			col.setRemarks(colRemarks.getText());
			if (!(col.getDefaultValue() == null && colDefaultValue.getText().equals("")))
			{
				col.setDefaultValue(colDefaultValue.getText());
			}
			// Autoincrement has to go before the primary key or 
			// this column will never allow nulls
			col.setAutoIncrement(colAutoInc.isSelected());
			if (col.getPrimaryKeySeq() == null) {
				col.setPrimaryKeySeq(colInPK.isSelected() ? new Integer(model.getPkSize()) : null);
			} else {
				col.setPrimaryKeySeq(colInPK.isSelected() ? new Integer(col.getPrimaryKeySeq()) : null);
			}

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
		fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.PROPERTY_CHANGE_GROUP_END,"Ending compound edit event in column edit panel"));
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
	
	public JPanel getPanel() {
		return this;
	}

	/**
	 * The list of SQLObject property change event listeners
	 * used for undo
	 */
	protected LinkedList<UndoCompoundEventListener> undoEventListeners = new LinkedList<UndoCompoundEventListener>();

	
	public void addUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.add(l);
	}

	public void removeUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.remove(l);
	}
	
	protected void fireUndoCompoundEvent(UndoCompoundEvent e) {
		Iterator it = undoEventListeners.iterator();
		
		
		if (e.getType().isStartEvent()) {
			while (it.hasNext()) {
				((UndoCompoundEventListener) it.next()).compoundEditStart(e);
			}
		} else {
			while (it.hasNext()) {
				((UndoCompoundEventListener) it.next()).compoundEditEnd(e);
			}
		} 
		
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

	public SQLTable getModel() {
		return model;
	}

	public JLabel getSourceDB() {
		return sourceDB;
	}

	public JLabel getSourceTableCol() {
		return sourceTableCol;
	}

	public SQLTableListModel getTableListModel() {
		return tableListModel;
	}

}
  