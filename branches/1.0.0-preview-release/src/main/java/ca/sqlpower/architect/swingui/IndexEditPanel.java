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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.Column;
import ca.sqlpower.swingui.ChangeListeningDataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelChangeUtil;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class IndexEditPanel extends ChangeListeningDataEntryPanel implements SPListener {
    protected SQLIndex index;

    protected SQLTable parent;

    protected SQLIndex indexCopy;

    JTextField name;

    JCheckBox unique;

    JCheckBox primaryKey;

    JComboBox indexType;

    JCheckBox clustered;

    IndexColumnTable columnsTable;

    private JPanel panel;
    
    /**
     * This session that contains this index panel.
     */
    ArchitectSwingSession session;    

    /**
     * Identifier for the default index type.
     */
    private static String DEFAULT_INDEX_TYPE = Messages.getString("IndexEditPanel.defaultIndexType"); //$NON-NLS-1$

    public IndexEditPanel(SQLIndex index, ArchitectSwingSession session) throws SQLObjectException {
        this(index, index.getParent(), session);
    }

    public IndexEditPanel(SQLIndex index, SQLTable parent, ArchitectSwingSession session) throws SQLObjectException {
        panel = new JPanel(new FormLayout("pref,4dlu,pref,4dlu,pref:grow,4dlu,pref", //$NON-NLS-1$
                "pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref:grow,4dlu,pref,4dlu")); //$NON-NLS-1$
        this.session = session;
        SQLPowerUtils.listenToHierarchy(index, this);
        createGUI(index, parent, session);
    }

    private void createGUI(SQLIndex index, SQLTable parent, ArchitectSwingSession session) throws SQLObjectException {
        this.parent = parent;
        PanelBuilder pb = new PanelBuilder((FormLayout) panel.getLayout(), panel);
        CellConstraints cc = new CellConstraints();
        pb.add(new JLabel(Messages.getString("IndexEditPanel.indexName")), cc.xy(1, 1)); //$NON-NLS-1$
        pb.add(name = new JTextField("", 30), cc.xyw(3, 1, 4)); //$NON-NLS-1$
        unique = new JCheckBox(Messages.getString("IndexEditPanel.uniqueIndex")); //$NON-NLS-1$
        pb.add(unique, cc.xy(3, 3));
        primaryKey = new JCheckBox(Messages.getString("IndexEditPanel.primaryKeyIndex")); //$NON-NLS-1$
        pb.add(primaryKey, cc.xy(3, 5));
        clustered = new JCheckBox(Messages.getString("IndexEditPanel.clusteredIndex")); //$NON-NLS-1$
        clustered.setSelected(index.isClustered());
        pb.add(clustered, cc.xy(3, 7));
        pb.add(new JLabel(Messages.getString("IndexEditPanel.indexType")), cc.xy(1, 9)); //$NON-NLS-1$

        indexType = new JComboBox();
        //add the platform default type
        indexType.addItem(DEFAULT_INDEX_TYPE);
        for (String type : getIndexTypes()) {
            indexType.addItem(type);
        }
        pb.add(indexType, cc.xyw(3, 9, 4));

        editIndex(index);
        columnsTable = new IndexColumnTable(parent, indexCopy, index);

        pb.add(new JScrollPane(columnsTable.getTable()), cc.xyw(1, 13, 6));

        // we want the buttons at their natural sizes, and the buttonbarbuilder wasn't doing that
        JPanel upDownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        upDownPanel.add(new JButton(new AbstractAction(null, SPSUtils.createIcon("chevrons_up1", null)) { //$NON-NLS-1$

            public void actionPerformed(ActionEvent e) {
                columnsTable.moveRow(true);
            }

        }));
        upDownPanel.add(new JButton(new AbstractAction(null, SPSUtils.createIcon("chevrons_down1", null)) { //$NON-NLS-1$
            public void actionPerformed(ActionEvent e) {
                columnsTable.moveRow(false);
            }
        }));

        pb.add(upDownPanel, cc.xyw(1, 15, 6));
        loadIndexIntoPanel();
    }

    /**
     * Returns an unique index name;
     */
    private String getIndexName() {
        int i = 0;
        String name = null;
        do {
            name = generateName(i);
            i++;
        } while (indexNameAlreadyExists(name));
        return name;
    }

    /**
     * This will check if an index name already exits on the table.
     * @return True if index already exists, false otherwise
     */
    private boolean indexNameAlreadyExists(String name) {
        try {
            for (SQLIndex index : parent.getIndices()) {
                if (name.equals(index.getName())) {
                    return true;
                }
            }
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        }
        return false;
    }

    /**
     * This will generate an index name if the format: $tablename_idx(#)
     */
    private String generateName(int number) {
        if (number == 0) {
            return new String(parent.getName() + "_idx"); //$NON-NLS-1$
        } else {
            return new String(parent.getName() + "_idx" + Integer.toString(number)); //$NON-NLS-1$
        }
    }

    /**
     *This will return a list of Index Types that are found in the pl.ini file
     */
    private List<String> getIndexTypes() {
        List<String> indexTypes = new ArrayList<String>();
        List<JDBCDataSourceType> dsTypes = this.session.getDataSources().getDataSourceTypes();
        for (JDBCDataSourceType dsType : dsTypes) {
            for (int dataTypeCount = 0;; dataTypeCount += 1) {
                String supportedType = dsType.getProperty(SQLIndex.INDEX_TYPE_DESCRIPTOR + "_" + dataTypeCount); //$NON-NLS-1$
                if (supportedType == null)
                    break;
                if (!indexTypes.contains(supportedType)) {
                    indexTypes.add(supportedType);
                }
            }
        }
        return indexTypes;
    }

    public void editIndex(SQLIndex index) throws SQLObjectException {
        this.index = index;
        name.setText(index.getName());
        indexCopy = new SQLIndex(index);
        indexCopy.setParent(null);
    }

    private void loadIndexIntoPanel() {
        if (index.getName() != null) {
            name.setText(index.getName());
        } else {
            name.setText(getIndexName());
        }
        primaryKey.setSelected(index.isPrimaryKeyIndex());
        primaryKey.setEnabled(false);
        unique.setSelected(index.isUnique());
        clustered.setSelected(index.isClustered());
        if (index.getType() == null) {
            indexType.setSelectedItem(DEFAULT_INDEX_TYPE);
        } else {
            indexType.setSelectedItem(index.getType());
        }
        name.selectAll();
    }

    protected SQLIndex getIndexCopy() {
        return indexCopy;
    }

    // --------------------- ArchitectPanel interface ------------------
    /**
     * Applies the changes to the panel if the data is valid
     *
     * the data is valid if it has a name, an index type and all
     * of the columns contain primary keys
     *
     * returns true if saved, false otherwise
     */
    public boolean applyChanges() {
        SQLPowerUtils.unlistenToHierarchy(index, this);
        columnsTable.cleanUp();
        columnsTable.finalizeIndex();        
        // if this was done on the index, listeners would only start listening after the index has
        // been added to its parent and compound edit would not work. Compound edits belong to the parent. 
        parent.begin(Messages.getString("IndexEditPanel.compoundEditName")); //$NON-NLS-1$
        try {
            StringBuffer warnings = new StringBuffer();
            //We need to check if the index name and/or primary key name is empty or not
            //if they are, we need to warn the user since it will mess up the SQLScripts we create
            if (name.getText().trim().length() == 0) {
                warnings.append(Messages.getString("IndexEditPanel.blankIndexName")); //$NON-NLS-1$

            }
            if (index.isPrimaryKeyIndex()) {
                for (Column c : indexCopy.getChildren(Column.class)) {
                    if (c.getColumn() == null) {
                        warnings.append(Messages.getString("IndexEditPanel.onlyAddColumnsToPK")); //$NON-NLS-1$
                        break;
                    }
                }
            }

            if (indexType.getSelectedItem() == null) {
                warnings.append(Messages.getString("IndexEditPanel.mustSelectIndexType")); //$NON-NLS-1$
            }

            if (warnings.toString().length() == 0) {
                //The operation is successful
                index.makeColumnsLike(indexCopy);
                SQLTable parentTable = parent;
                index.setName(name.getText().trim());

				// make the physical name identitical as long as there  is
				// no separate input field for it
				index.setPhysicalName(name.getText().trim());
				index.setUnique(unique.isSelected());
                index.setClustered(clustered.isSelected());
                if (indexType.getSelectedItem().toString().equals(DEFAULT_INDEX_TYPE)) {
                    index.setType(null);
                } else {
                    index.setType(indexType.getSelectedItem().toString());
                }
                List<SQLIndex> children = parentTable.getIndices();
                if (!children.contains(index)) {
                    parentTable.addIndex(index);
                }
                index.cleanUpIfChildless();
                return true;
            } else {
                JOptionPane.showMessageDialog(panel, warnings.toString());
                //this is done so we can go back to this dialog after the error message
                return false;
            }
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (ObjectDependentException e) {
            throw new RuntimeException(e);
        } finally {
            parent.commit();
        }
    }

    public void discardChanges() {
        SQLPowerUtils.unlistenToHierarchy(index, this);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getNameText() {
        return name.getText();
    }

    public void setNameText(String newName) {
        name.setText(newName);
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

    public void childAdded(SPChildEvent e) {
        // XXX Make this actually check for a conflict or not.
        if (e.getSource() == index) {
            columnsTable.getTable().setBackground(DataEntryPanelChangeUtil.NONCONFLICTING_COLOR);
            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
        }
    }

    public void childRemoved(SPChildEvent e) {
        // XXX Make this actually check for a conflict or not.
        if (e.getSource() == index) {
            columnsTable.getTable().setBackground(DataEntryPanelChangeUtil.NONCONFLICTING_COLOR);
            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
        }     
    }

    public void propertyChanged(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        boolean error = false;
        if (e.getSource() == index) {            
            if (property.equals("name")) {
                error = DataEntryPanelChangeUtil.incomingChange(name, e);
            } else if (property.equals("unique")) {
                error = DataEntryPanelChangeUtil.incomingChange(unique, e);
            } else if (property.equals("clustered")) {
                error = DataEntryPanelChangeUtil.incomingChange(clustered, e);
            } else if (property.equals("type")) {
                Object oldValue = e.getOldValue();
                Object newValue = e.getNewValue();
                if (oldValue == null || oldValue.equals("")) oldValue = "Platform Default";
                if (newValue == null || oldValue.equals("")) newValue = "Platform Default"; 
                error = DataEntryPanelChangeUtil.incomingChange(indexType, new PropertyChangeEvent(
                        e.getSource(), e.getPropertyName(), oldValue, newValue));
            }
        } else if (e.getSource() instanceof Column) {            
            if (property.equals("ascendingOrDescending")) {
                // XXX Make this find the appropriate checkbox and highlight that.
                columnsTable.getTable().setBackground(DataEntryPanelChangeUtil.NONCONFLICTING_COLOR);
                error = true;
            }
        }
        if (error) {
            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
        }
    }

    public void transactionEnded(TransactionEvent e) {
        // no-op
    }

    public void transactionRollback(TransactionEvent e) {
        // no-op
    }

    public void transactionStarted(TransactionEvent e) {
        // no-op
    }
}