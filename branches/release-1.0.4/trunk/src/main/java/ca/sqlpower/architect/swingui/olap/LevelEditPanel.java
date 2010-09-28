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

package ca.sqlpower.architect.swingui.olap;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.olap.MondrianModel.Property;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.architect.swingui.SQLObjectComboBoxModel;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.table.EditableJTable;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.NotNullValidator;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidatableDataEntryPanel;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class LevelEditPanel implements ValidatableDataEntryPanel {

    /**
     * An enumeration of possible values for the levelType attribute.
     * 
     * There is a 'Regular' levelType that can only be used for Levels in a
     * StandardDimension, but since it's currently the only option for
     * StandardDimension Levels, I've left it out.
     * 
     * Note that the 'Time' values can only be used for Levels in a
     * TimeDimension.
     * 
     * Currently, we are not simply using the LevelType enumeration in
     * Mondrian so we do not have to include Mondrian as a dependency.
     * However, if the need to include Mondrian as a dependency arises, then
     * we can probably dispose of this enumeration and use the Mondrian one.
     */
    public enum LevelType {
        TimeYears,
        TimeQuarters,
        TimeMonths,
        TimeWeeks,
        TimeDays,
    }
    
    private final Level level;
    private final JPanel panel;
    private JTextField name;
    private JTextField captionField;
    private JComboBox columnChooser;
    private JCheckBox uniqueMembers;
    
    /**
     * A combo box used to select level type. Since the LevelType attribute is
     * only currently being used in Mondrian for Time Dimensions, it will only
     * appear if the parent Dimension is of type TimeDimension.
     */
    private JComboBox levelType;
    
    private PropertiesEditPanel propertiesPanel;

    /**
     * Validation handler for errors in the dialog
     */
    private FormValidationHandler handler;
    private StatusComponent status = new StatusComponent();
    
    /**
     * Creates a new property editor for the given level of a hierarchy.
     * 
     * @param cube
     *            The data model of the Level to edit
     * @throws SQLObjectException
     *             if digging up the source table results in a database error
     */
    public LevelEditPanel(Level level) throws SQLObjectException {
        this.level = level;
        
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append(status, 3);
        builder.append("Name", name = new JTextField(level.getName()));
        builder.append("Caption", captionField = new JTextField(level.getCaption()));
        builder.append("Column", columnChooser = new JComboBox());
        
        if (level.getUniqueMembers() != null) {
            builder.append("Unique Members", uniqueMembers = new JCheckBox("", level.getUniqueMembers()));
        } else {
            builder.append("Unique Members", uniqueMembers = new JCheckBox(""));
        }
 
        Hierarchy hierarchy = (Hierarchy) level.getParent();

        Dimension dimension = (Dimension) hierarchy.getParent();
        
        // Currently, the levelType attribute appears to only apply to Time Dimensions
        if (dimension.getType() != null && dimension.getType().equals("TimeDimension")) {
            builder.append("Level Type", levelType = new JComboBox(LevelType.values()));
            if (level.getLevelType() != null) {
                levelType.setSelectedItem(LevelType.valueOf(level.getLevelType()));
            } else {
                levelType.setSelectedItem(LevelType.values()[0]);
            }
        }
        
        SQLTable dimensionTable = OLAPUtil.tableForHierarchy(hierarchy);
        
        // if the hierarchy's table was not set, then we try to find it from elsewhere.
        // we'll look through the cubes that reference the hierarchy's parent dimension
        // and if the fact table and foreign keys of those are same through out, we set
        // those values for the hierarchy.
        if (dimensionTable == null) {
            OLAPSession oSession = OLAPUtil.getSession(level);
            Schema sch = oSession.getSchema();
            String parentDimName = hierarchy.getParent().getName();
            
            boolean valid = true;
            Table fact = null;
            String foreignKey = null;
            for (int i = 0; i < sch.getCubes().size() && valid; i++) {
                Cube c = sch.getCubes().get(i);
                if (c.getFact() == null) continue;
                for (CubeDimension cd : c.getDimensions()) {
                    if (cd instanceof DimensionUsage) {
                        DimensionUsage du = (DimensionUsage) cd;
                        if (du.getSource().equalsIgnoreCase(parentDimName)) {
                            Table rel = (Table) c.getFact();
                            String fk = du.getForeignKey();

                            if (fk != null) {
                                if (fact == null && foreignKey == null) {
                                    fact = rel;
                                    foreignKey = fk;
                                } else {
                                    if (!fact.getSchema().equalsIgnoreCase(rel.getSchema()) ||
                                            !fact.getName().equalsIgnoreCase(rel.getName()) ||
                                            !foreignKey.equalsIgnoreCase(fk)) {
                                        valid = false;
                                    }
                                }
                            }

                            break;
                        }
                    }
                }
            }
            
            if (valid && fact != null && foreignKey != null) {
                dimensionTable = OLAPUtil.getSQLTableFromOLAPTable(oSession.getDatabase(), fact);
                Table tab = new Table();
                tab.setSchema(fact.getSchema());
                tab.setName(fact.getName());
                hierarchy.setRelation(tab);
                hierarchy.setPrimaryKey(foreignKey);
            }
        }
        
        if (dimensionTable == null) {
            columnChooser.addItem("Parent hierarchy has no table");
            columnChooser.setEnabled(false);
        } else if (dimensionTable.getColumns().isEmpty()) {
            columnChooser.addItem("Parent hierarchy table has no columns");
            columnChooser.setEnabled(false);
        } else {
            columnChooser.setModel(new SQLObjectComboBoxModel(dimensionTable, SQLColumn.class));
            for (SQLColumn col : dimensionTable.getColumns()) {
                if (col.getName().equalsIgnoreCase(level.getColumn())) {
                    columnChooser.setSelectedItem(col);
                }
            }
        }
        
        handler = new FormValidationHandler(status, true);
        Validator validator = new OLAPObjectNameValidator((OLAPObject) level.getParent(), level, false);
        handler.addValidateObject(name, validator);
        handler.addValidateObject(columnChooser, new NotNullValidator("Column"));
        
        builder.appendSeparator("Properties");
        propertiesPanel = new PropertiesEditPanel(dimensionTable, handler);
        builder.append(propertiesPanel, 3);
        
        panel = builder.getPanel();
    }
    
    public boolean applyChanges() {
        level.begin("Modify Level Properties");
        level.setName(name.getText());
  
        if (columnChooser.isEnabled()) {
            SQLColumn col = (SQLColumn) columnChooser.getSelectedItem();
            level.setColumn(col == null ? null : col.getName());
        }
        if (!(captionField.getText().equals(""))) {
            level.setCaption(captionField.getText());
        } else {
            level.setCaption(null);
        }
        if (uniqueMembers.isSelected()) {
            level.setUniqueMembers(true);
        }else{
            level.setUniqueMembers(false);        
        }
        if (levelType != null) {
            LevelType newType = (LevelType) levelType.getSelectedItem();
            if (newType != null) {
                level.setLevelType(newType.toString());
            } else {
                level.setLevelType(LevelType.values()[0].toString());
            }
        } else {
            level.setLevelType(null);
        }
        level.commit();
        return true;
    }

    public void discardChanges() {
        // nothing to do
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }

    public ValidationHandler getValidationHandler() {
        return handler;
    }
    
    /**
     * Checks through the table model for duplicate property names.
     * 
     */
    private class PropertiesTableNameValidator implements Validator {
        public ValidateResult validate(Object contents) {
            TableModel model = (TableModel) contents;
            List<String> propNames = new ArrayList<String>();
            for ( int i = 0; i < model.getRowCount(); i++) {
                String propName = (String) model.getValueAt(i, 0);
                if (propNames.contains(propName)) {
                    return ValidateResult.createValidateResult(Status.FAIL, "Duplicate Property names.");
                } else {
                    propNames.add(propName); 
                }
            }
            return ValidateResult.createValidateResult(Status.OK, "");
        }
    }

    private class PropertiesEditPanel extends JPanel {
        
        private final JTable propertiesTab;
        
        /**
         * Default name for a new Property.
         */
        private final String defaultPropName = "New Property";
        
        private final Action newPropertyAction = new AbstractAction("New...") { 
            public void actionPerformed(ActionEvent e) {
                Property prop = new Property();
                prop.setName(defaultPropName);
                level.addProperty(prop);
            }
        };
        
        private final Action removePropertyAction = new AbstractAction("Remove...") { 
            public void actionPerformed(ActionEvent e) {
                int selectedRow = propertiesTab.getSelectedRow();
                if (selectedRow > -1) {
                    level.removeProperty(selectedRow);
                    setEnabled(false);
                }
            }
        };
        
        /**
         * Creates a panel for editing a level's Properties with a JTable.
         * 
         * @param table
         *            Should contain the columns that a Property could use, can
         *            be null.
         * @param handler
         *            Used for adding the {@link PropertiesTableNameValidator}
         *            on the JTable.
         * @throws SQLObjectException
         *             If retrieving the columns from the table fails.
         */
        public PropertiesEditPanel(SQLTable table, ValidationHandler handler) throws SQLObjectException {
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setLayout(new BorderLayout(10, 10));
            
            propertiesTab = new PropertiesTable(table);
            handler.addValidateObject(propertiesTab, new PropertiesTableNameValidator());
            
            JScrollPane sp = new JScrollPane(propertiesTab);
            sp.setPreferredSize(new java.awt.Dimension(200, 200));
            add(sp, BorderLayout.CENTER);
            
            ButtonStackBuilder bsb = new ButtonStackBuilder();
            bsb.addGridded(new JButton(newPropertyAction));
            bsb.addRelatedGap();
            bsb.addGridded(new JButton(removePropertyAction));
            bsb.addRelatedGap();
            
            removePropertyAction.setEnabled(false);
            
            add(bsb.getPanel(), BorderLayout.EAST);
        }
        
        private class PropertiesTable extends EditableJTable {
            
            /**
             * List of the SQLColumns available for Properties to use.
             */
            private final List<SQLColumn> columns;
            
            /**
             * Creates an EditableJTable for modifying Properties of the level.
             * @param table
             *            Should contain the columns that a Property could use,
             *            can be null.
             * @throws SQLObjectException
             *             If retrieving the columns from the table fails.
             */
            public PropertiesTable(SQLTable table) throws SQLObjectException {
                super();
                this.columns = table == null ? null : table.getColumns();
                
                boolean disableColumns = columns == null || columns.isEmpty();
                setModel(new PropertiesTableModel(disableColumns));
                setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        boolean enableAction = propertiesTab.getSelectedRow() > -1;
                        removePropertyAction.setEnabled(enableAction);
                    }
                });
            }
            
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 1) {
                    // columns could be null, but then it would not be editable, so we're save.
                    return new DefaultCellEditor(new JComboBox(columns.toArray()));
                } else {
                    return super.getCellEditor(row, column);
                }
            }
            
        }
        
        private class PropertiesTableModel extends AbstractTableModel implements SPListener {
            
            /**
             * Determines whether the column attributes can be modified.
             */
            private final boolean disableColumns;

            public PropertiesTableModel(boolean disableColumns) {
                this.disableColumns = disableColumns;
                level.addSPListener(this);
            }
            
            public int getRowCount() {
                return level.getProperties().size();
            }

            public int getColumnCount() {
                return 2;
            }

            @Override
            public String getColumnName(int columnIndex) {
                if (columnIndex == 0) {
                    return "Name";
                } else if (columnIndex == 1) {
                    return "Column";
                } else {
                    throw new IllegalArgumentException("getColumnName: Unknow column index: " + columnIndex);
                }
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 1) {
                    return String.class;
                } else {
                    throw new IllegalArgumentException("getColumnClass: Unknow column index: " + columnIndex);
                }
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    return true;
                } else if (columnIndex == 1) {
                    return !disableColumns;
                } else {
                    throw new IllegalArgumentException("isCellEditable: Unknow column index: " + columnIndex);
                }
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    return level.getProperties().get(rowIndex).getName();
                } else if (columnIndex == 1) {
                    if (disableColumns) {
                        return "Not Applicable";
                    } else {
                        return level.getProperties().get(rowIndex).getColumn();
                    }
                } else {
                    throw new IllegalArgumentException("getValueAt: Unexcepted column index: " + columnIndex);
                }   
            }
            
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                Property prop = level.getProperties().get(rowIndex);
                if (columnIndex == 0) {
                    prop.setName((String) aValue);
                } else if (columnIndex == 1) {
                    if (aValue == null) {
                        prop.setColumn(null);
                    } else {
                        String colName = ((SQLColumn) aValue).getName();
                        // if the Property has not been named, name it similar to its chosen column.
                        if (defaultPropName.equals(prop.getName())) {
                            prop.setName(underscoreToCamelCaps(colName));
                        }
                        prop.setColumn(colName);
                    }
                } else {
                    throw new IllegalArgumentException("setValueAt: Unexcepted column index:"+columnIndex);
                }
                fireTableChanged(new TableModelEvent(this, rowIndex));
            }
            
            /**
             * Converts underscores in given text to spaces and return in camel caps.
             * 
             */
            private String underscoreToCamelCaps(String text) {
                StringBuffer result = new StringBuffer(text.length() * 2);
                char[] chars = text.toLowerCase().toCharArray();
                for (int i = 0; i < text.length(); i++) {
                    if ('_' == chars[i]) {
                        result.append(" ");
                        i++;
                        if (i >= chars.length) break;
                        result.append(Character.toUpperCase(chars[i]));
                    } else {
                        result.append(chars[i]);
                    }
                }
                if (chars.length > 0) {
                    result.setCharAt(0, Character.toUpperCase(chars[0]));
                }
                return result.toString();
            }

            public void childAdded(SPChildEvent e) {
                fireTableDataChanged();
            }

            public void childRemoved(SPChildEvent e) {
                fireTableDataChanged();
            }

            public void propertyChanged(PropertyChangeEvent evt) {
                //no-op
            }

            public void transactionEnded(TransactionEvent e) {
                //no-op                
            }

            public void transactionRollback(TransactionEvent e) {
                //no-op                
            }

            public void transactionStarted(TransactionEvent e) {
                //no-op                
            }
        }
    }
}
