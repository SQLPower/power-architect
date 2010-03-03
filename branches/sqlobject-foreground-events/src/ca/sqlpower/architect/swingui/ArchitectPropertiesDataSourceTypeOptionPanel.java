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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.ddl.DB2DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.ddl.HSQLDBDDLGenerator;
import ca.sqlpower.architect.ddl.MySqlDDLGenerator;
import ca.sqlpower.architect.ddl.OracleDDLGenerator;
import ca.sqlpower.architect.ddl.PostgresDDLGenerator;
import ca.sqlpower.architect.ddl.SQLServer2000DDLGenerator;
import ca.sqlpower.architect.ddl.SQLServer2005DDLGenerator;
import ca.sqlpower.architect.ddl.SQLServerDDLGenerator;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileFunctionDescriptor;
import ca.sqlpower.architect.profile.RemoteDatabaseProfileCreator;
import ca.sqlpower.architect.profile.RemoteDatabaseProfileCreator.AverageSQLFunction;
import ca.sqlpower.architect.profile.RemoteDatabaseProfileCreator.CaseWhenNullSQLFunction;
import ca.sqlpower.architect.profile.RemoteDatabaseProfileCreator.StringLengthSQLFunction;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.swingui.AddRemoveIcon;
import ca.sqlpower.swingui.db.DataSourceTypeEditorTabPanel;
import ca.sqlpower.swingui.table.TableUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This properties panel will allow users to modify all of the architect specific properties in
 * one tab panel.
 *
 * This name beats the {@link KettleDataSourceTypeOptionPanel} name for length.
 */
public class ArchitectPropertiesDataSourceTypeOptionPanel implements DataSourceTypeEditorTabPanel {
    
    /**
     * This stores the known DDL generator types. This list will need to be increased 
     * as more DDL Generators are added if we want users to be able to select them
     * from this menu. 
     */
    private enum KnownDDLGenerators {
        GENERIC("Generic", GenericDDLGenerator.class.getName()),
        DB2("DB2", DB2DDLGenerator.class.getName()),
        HSQLDB("HSQLDB", HSQLDBDDLGenerator.class.getName()),
        MY_SQL("My SQL", MySqlDDLGenerator.class.getName()),
        ORACLE("Oracle", OracleDDLGenerator.class.getName()),
        POSTGRES("Postgres", PostgresDDLGenerator.class.getName()),
        SQL_SERVER_2000("SQL Server 2000", SQLServer2000DDLGenerator.class.getName()),
        SQL_SERVER_2005("SQL Server 2005", SQLServer2005DDLGenerator.class.getName()),
        SQL_SERVER("SQL Server", SQLServerDDLGenerator.class.getName());
        
        private String name;
        private String ddlClassName;
        
        private KnownDDLGenerators(String name, String ddlClassName) {
            this.name = name;
            this.ddlClassName = ddlClassName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDDLClassName() {
            return ddlClassName;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    private class ProfileFunctionTableModel implements TableModel {

        private final List<ProfileFunctionDescriptor> descriptors = new ArrayList<ProfileFunctionDescriptor>();
        private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();
        
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 1) {
                return String.class;
            } else if (columnIndex == 2) {
                return JComboBox.class;
            } else if (columnIndex >= 3 && columnIndex <= 10) {
                return Boolean.class;
            } else {
                return null;
            }
        }

        public int getColumnCount() {
            return 11;
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "Architect Name";
            } else if (columnIndex == 1) {
                return "Data Type Name";
            } else if (columnIndex == 2) {
                return "Java Code";
            } else if (columnIndex == 3) {
                return "Count Distinct";
            } else if (columnIndex == 4) {
                return "Max Value";
            } else if (columnIndex == 5) {
                return "Min Value";
            } else if (columnIndex == 6) {
                return "Avg Value";
            } else if (columnIndex == 7) {
                return "Max Length";
            } else if (columnIndex == 8) {
                return "Min Length";
            } else if (columnIndex == 9) {
                return "Avg Length";
            } else if (columnIndex == 10) {
                return "Sum Decode";
            } else {
                return null;
            }
        }

        public int getRowCount() {
            return descriptors.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            ProfileFunctionDescriptor pfd = descriptors.get(rowIndex);
            if (columnIndex == 0) {
                return pfd.getArchitectSpecificName();
            } else if (columnIndex == 1) {
                return pfd.getDataTypeName();
            } else if (columnIndex == 2) {
                return SQLType.getType(pfd.getDataTypeCode());
            } else if (columnIndex == 3) {
                return pfd.isCountDist();
            } else if (columnIndex == 4) {
                return pfd.isMaxValue();
            } else if (columnIndex == 5) {
                return pfd.isMinValue(); 
            } else if (columnIndex == 6) {
                return pfd.isAvgValue(); 
            } else if (columnIndex == 7) {
                return pfd.isMaxLength();
            } else if (columnIndex == 8) {
                return pfd.isMinLength();
            } else if (columnIndex == 9) {
                return pfd.isAvgLength();
            } else if (columnIndex == 10) {
                return pfd.isSumDecode();
            } else {
                return null; 
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            ProfileFunctionDescriptor pfd = descriptors.get(rowIndex);
            if (columnIndex == 0) {
                pfd.setArchitectSpecificName((String) value);
            } else if (columnIndex == 1) {
                pfd.setDataTypeName((String) value);
            } else if (columnIndex == 2) {
                pfd.setDataTypeCode(((SQLType) value).getType());
            } else if (columnIndex == 3) {
                pfd.setCountDist((Boolean) value);
            } else if (columnIndex == 4) {
                pfd.setMaxValue((Boolean) value);
            } else if (columnIndex == 5) {
                pfd.setMinValue((Boolean) value); 
            } else if (columnIndex == 6) {
                pfd.setAvgValue((Boolean) value); 
            } else if (columnIndex == 7) {
                pfd.setMaxLength((Boolean) value);
            } else if (columnIndex == 8) {
                pfd.setMinLength((Boolean) value);
            } else if (columnIndex == 9) {
                pfd.setAvgLength((Boolean) value);
            } else if (columnIndex == 10) {
                pfd.setSumDecode((Boolean) value);
            }
            for (int i = listeners.size() -1; i >= 0; i--) {
                listeners.get(i).tableChanged(new TableModelEvent(this, rowIndex));
            }
        }
        
        public void addProfileFunctionDescriptor(ProfileFunctionDescriptor pfd) {
            descriptors.add(pfd);
            for (int i = listeners.size() -1; i >= 0; i--) {
                listeners.get(i).tableChanged(new TableModelEvent(this));
            }
        }

        public ProfileFunctionDescriptor removeProfileFunctionDescriptor(int rowIndex) {
            ProfileFunctionDescriptor pfd = descriptors.remove(rowIndex);
            for (int i = listeners.size() -1; i >= 0; i--) {
                listeners.get(i).tableChanged(new TableModelEvent(this));
            }
            return pfd;
        }
        
        public List<ProfileFunctionDescriptor> getProfileFunctionDescriptors() {
            return Collections.unmodifiableList(descriptors);
        }
        
    }
    
    private JPanel panel;
    
    private JDBCDataSourceType currentDSType;
    
    private final JTextField averageSQLFunctionField = new JTextField();
    private final JTextField stringLengthSQLFuncField = new JTextField();
    private final JTextField caseWhenNullSQLFuncField = new JTextField();
    private final JCheckBox updatableRSField = new JCheckBox("Supports Updatable Result Sets");
    private final JComboBox ddlGeneratorCombo = new JComboBox(KnownDDLGenerators.values());
    
    /**
     * This is the table model displaying all of the profile function descriptors for editing.
     */
    private ProfileFunctionTableModel profileFunctionTableModel;

    /**
     * This is the table model displaying all of the index types for the current database type
     * for editing.
     */
    private DefaultTableModel indexTableModel;
    
    public ArchitectPropertiesDataSourceTypeOptionPanel() {
        panel = new JPanel(new BorderLayout());
    }

    public void editDsType(JDBCDataSourceType dsType) {
        if (dsType == null) {
            panel.removeAll();
            return;
        }

        averageSQLFunctionField.setText("");
        stringLengthSQLFuncField.setText("");
        caseWhenNullSQLFuncField.setText("");
        updatableRSField.setSelected(false);
        ddlGeneratorCombo.setSelectedItem(KnownDDLGenerators.GENERIC);
        
        currentDSType = dsType;
        profileFunctionTableModel = new ProfileFunctionTableModel();
        final JTable profileFunctionTable = new JTable(profileFunctionTableModel);
        TableUtils.fitColumnWidths(profileFunctionTable, 0);
        profileFunctionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        profileFunctionTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(SQLType.getTypes())));
        
        indexTableModel = new DefaultTableModel();
        indexTableModel.addColumn("Index Type");
        final JTable indexTypeJTable = new JTable(indexTableModel);
        indexTypeJTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        for (String property : dsType.getPropertyNames()) {
            if (property.contains("architect")) {
                if (property.contains("ca.sqlpower.architect.etl.kettle")) {
                    //Kettle has it's own tab so we will let those properties be defined there.
                } else if (property.contains(ProfileFunctionDescriptor.class.getName())) {
                    String descriptorString = dsType.getProperty(property);
                    final ProfileFunctionDescriptor pfd = ProfileFunctionDescriptor.parseDescriptorString(descriptorString);
                    profileFunctionTableModel.addProfileFunctionDescriptor(pfd);
                } else if (property.contains(ColumnProfileResult.class.getName())) {
                    if (property.equals(RemoteDatabaseProfileCreator.propName(AverageSQLFunction.class))) {
                        averageSQLFunctionField.setText(dsType.getProperty(property));
                    } else if (property.equals(RemoteDatabaseProfileCreator.propName(StringLengthSQLFunction.class))) {
                        stringLengthSQLFuncField.setText(dsType.getProperty(property));
                    } else if (property.equals(RemoteDatabaseProfileCreator.propName(CaseWhenNullSQLFunction.class))) {
                        caseWhenNullSQLFuncField.setText(dsType.getProperty(property));
                    } else {
                        throw new IllegalStateException("No editor defined for the data source type property " + property);
                    }
                } else if (property.contains(SQLIndex.INDEX_TYPE_DESCRIPTOR)) {
                    indexTableModel.addRow(new String[] {dsType.getProperty(property)});
                } else {
                    throw new IllegalStateException("No editor defined for the data source type property " + property); 
                }
            } else if (property.equals(JDBCDataSourceType.SUPPORTS_UPDATEABLE_RESULT_SETS)) {
                updatableRSField.setSelected(Boolean.parseBoolean(dsType.getProperty(property)));
            } else if (property.equals(JDBCDataSourceType.DDL_GENERATOR)) {
                ddlGeneratorCombo.setSelectedItem(KnownDDLGenerators.GENERIC);
                for (KnownDDLGenerators ddlg : KnownDDLGenerators.values()) {
                    if (dsType.getProperty(property).equals(ddlg.getDDLClassName())) {
                        ddlGeneratorCombo.setSelectedItem(ddlg);
                        break;
                    }
                }
            }
        }
        
        JComponent addRemoveProfileFunctionBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRemoveProfileFunctionBar.add(new JButton(new AbstractAction("", new AddRemoveIcon(AddRemoveIcon.Type.ADD)) {
            public void actionPerformed(ActionEvent e) {
                ProfileFunctionDescriptor pfd = new ProfileFunctionDescriptor("", SQLType.getTypes()[0].getType(), false, false, false, false, false, false, false, false);
                pfd.setArchitectSpecificName("");
                profileFunctionTableModel.addProfileFunctionDescriptor(pfd);
            }
        }));
        addRemoveProfileFunctionBar.add(new JButton(new AbstractAction("", new AddRemoveIcon(AddRemoveIcon.Type.REMOVE)) {
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = profileFunctionTable.getSelectedRows();
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    profileFunctionTableModel.removeProfileFunctionDescriptor(selectedRows[i]);
                }
            }
        }));
        
        JComponent addRemoveIndexBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRemoveIndexBar.add(new JButton(new AbstractAction("", new AddRemoveIcon(AddRemoveIcon.Type.ADD)){
            public void actionPerformed(ActionEvent e) {
                indexTableModel.addRow(new String[] {""});
            }
        }));
        addRemoveIndexBar.add(new JButton(new AbstractAction("", new AddRemoveIcon(AddRemoveIcon.Type.REMOVE)) {
            public void actionPerformed(ActionEvent e) {
                int [] selectedRows = indexTypeJTable.getSelectedRows();
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    indexTableModel.removeRow(selectedRows[i]);
                }
            }
        }));
        
        panel.removeAll();
        DefaultFormBuilder fb = new DefaultFormBuilder(new FormLayout("4dlu, pref, 4dlu, pref:grow, 4dlu", 
                "pref, 4dlu, pref, 4dlu, pref, 2dlu, pref, 2dlu, pref, 4dlu, fill:min:grow, 2dlu, pref, 4dlu, pref, 2dlu, pref"));
        fb.nextColumn();
        fb.append("", updatableRSField);
        fb.nextLine();
        fb.nextLine();
        fb.nextColumn();
        fb.append("DDL Generator", ddlGeneratorCombo);
        fb.nextLine();
        fb.nextLine();
        fb.nextColumn();
        fb.append("Average SQL Function", averageSQLFunctionField);
        fb.nextLine();
        fb.nextLine();
        fb.nextColumn();
        fb.append("String Length SQL Function", stringLengthSQLFuncField);
        fb.nextLine();
        fb.nextLine();
        fb.nextColumn();
        fb.append("Case When Null SQL Function", caseWhenNullSQLFuncField);
        fb.nextLine();
        fb.nextLine();
        fb.nextColumn();
        fb.append(new JScrollPane(profileFunctionTable), 3);
        fb.nextLine();
        fb.nextLine();
        fb.nextColumn();
        fb.append(addRemoveProfileFunctionBar, 3);
        fb.nextLine();
        fb.nextLine();
        fb.nextColumn();
        JScrollPane indexScrollPane = new JScrollPane(indexTypeJTable);
        indexScrollPane.setPreferredSize(new Dimension((int) indexScrollPane.getPreferredSize().getWidth(), indexTypeJTable.getRowHeight() * 5));
        fb.append(indexScrollPane, 3);
        fb.nextLine();
        fb.nextLine();
        fb.nextColumn();
        fb.append(addRemoveIndexBar, 3);
        panel.add(fb.getPanel(), BorderLayout.CENTER);
    }

    public boolean applyChanges() {
        if (currentDSType == null) {
            return true;
        }

        currentDSType.putProperty(JDBCDataSourceType.DDL_GENERATOR, ((KnownDDLGenerators) ddlGeneratorCombo.getSelectedItem()).getDDLClassName());
        currentDSType.putProperty(JDBCDataSourceType.SUPPORTS_UPDATEABLE_RESULT_SETS, String.valueOf(updatableRSField.isSelected()));
        currentDSType.putProperty(RemoteDatabaseProfileCreator.propName(AverageSQLFunction.class), averageSQLFunctionField.getText());
        currentDSType.putProperty(RemoteDatabaseProfileCreator.propName(StringLengthSQLFunction.class), stringLengthSQLFuncField.getText());
        currentDSType.putProperty(RemoteDatabaseProfileCreator.propName(CaseWhenNullSQLFunction.class), caseWhenNullSQLFuncField.getText());
        
        for (int i = 0; i < profileFunctionTableModel.getProfileFunctionDescriptors().size(); i++) {
            currentDSType.putProperty(ProfileFunctionDescriptor.class.getName() + "_" + i, ProfileFunctionDescriptor.createDescriptorString(profileFunctionTableModel.getProfileFunctionDescriptors().get(i)));
        }
        
        for (int i = 0; i < indexTableModel.getRowCount(); i++) {
            currentDSType.putProperty(SQLIndex.INDEX_TYPE_DESCRIPTOR + "_" + i, (String) indexTableModel.getValueAt(i, 0));
        }
        
        return true;
    }

    public void discardChanges() {
        editDsType(currentDSType);
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }

}
