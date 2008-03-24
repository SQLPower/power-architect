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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class IndexEditPanel extends JPanel implements DataEntryPanel {
    protected SQLIndex index;

    protected SQLTable parent;

    protected SQLIndex indexCopy;

    JTextField name;

    JCheckBox unique;

    JCheckBox primaryKey;

    JComboBox indexType;

    JCheckBox clustered;

    IndexColumnTable columnsTable;

    /**
     * This session that contains this index panel.
     */
    ArchitectSwingSession session;

    /**
     * Identifier for the default index type.
     */
    private static String DEFAULT_INDEX_TYPE = "<Platform Default>";

    public IndexEditPanel(SQLIndex index, ArchitectSwingSession session) throws ArchitectException {
        super(new FormLayout("pref,4dlu,pref,4dlu,pref:grow,4dlu,pref",
                "pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref:grow,4dlu,pref,4dlu"));
        this.session = session;
        createGUI(index, index.getParentTable(), session);
    }

    public IndexEditPanel(SQLIndex index, SQLTable parent, ArchitectSwingSession session) throws ArchitectException {
        super(new FormLayout("pref,4dlu,pref,4dlu,pref:grow,4dlu,pref",
                "pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref:grow,4dlu,pref,4dlu"));
        this.session = session;
        createGUI(index, parent, session);
    }

    private void createGUI(SQLIndex index, SQLTable parent, ArchitectSwingSession session) throws ArchitectException {
        this.parent = parent;
        addUndoEventListener(session.getUndoManager().getEventAdapter());
        PanelBuilder pb = new PanelBuilder((FormLayout) this.getLayout(), this);
        CellConstraints cc = new CellConstraints();
        pb.add(new JLabel("Index Name"), cc.xy(1, 1));
        pb.add(name = new JTextField("", 30), cc.xyw(3, 1, 4));
        unique = new JCheckBox("Unique");
        pb.add(unique, cc.xy(3, 3));
        primaryKey = new JCheckBox("Primary Key");
        pb.add(primaryKey, cc.xy(3, 5));
        clustered = new JCheckBox("Clustered");
        clustered.setSelected(index.isClustered());
        pb.add(clustered, cc.xy(3, 7));
        pb.add(new JLabel("Index Type"), cc.xy(1, 9));

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
        upDownPanel.add(new JButton(new AbstractAction(null, SPSUtils.createIcon("chevrons_up1", null)) {

            public void actionPerformed(ActionEvent e) {
                columnsTable.moveRow(true);
            }

        }));
        upDownPanel.add(new JButton(new AbstractAction(null, SPSUtils.createIcon("chevrons_down1", null)) {
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
            for (int i = 0; i < parent.getIndicesFolder().getChildCount(); i++) {
                if (name.equals(parent.getIndicesFolder().getChild(i).getName())) {
                    return true;
                }
            }
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
        return false;
    }

    /**
     * This will generate an index name if the format: $tablename_idx(#)
     */
    private String generateName(int number) {
        if (number == 0) {
            return new String(parent.getName() + "_idx");
        } else {
            return new String(parent.getName() + "_idx" + Integer.toString(number));
        }
    }

    /**
     *This will return a list of Index Types that are found in the pl.ini file
     */
    private List<String> getIndexTypes() {
        List<String> indexTypes = new ArrayList<String>();
        List<SPDataSourceType> dsTypes = this.session.getContext().getPlDotIni().getDataSourceTypes();
        for (SPDataSourceType dsType : dsTypes) {
            for (int dataTypeCount = 0;; dataTypeCount += 1) {
                String supportedType = dsType.getProperty(SQLIndex.INDEX_TYPE_DESCRIPTOR + "_" + dataTypeCount);
                if (supportedType == null)
                    break;
                if (!indexTypes.contains(supportedType)) {
                    indexTypes.add(supportedType);
                }
            }
        }
        return indexTypes;
    }

    public void editIndex(SQLIndex index) throws ArchitectException {
        this.index = index;
        name.setText(index.getName());
        indexCopy = new SQLIndex(index);
        indexCopy.setPrimaryKeyIndex(false);
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
        columnsTable.cleanUp();
        columnsTable.finalizeIndex();
        startCompoundEdit("Index Properties Change");
        try {
            StringBuffer warnings = new StringBuffer();
            //We need to check if the index name and/or primary key name is empty or not
            //if they are, we need to warn the user since it will mess up the SQLScripts we create
            if (name.getText().trim().length() == 0) {
                warnings.append("The index cannot be assigned a blank name \n");

            }
            if (index.isPrimaryKeyIndex()) {
                for (Column c : (List<Column>) indexCopy.getChildren()) {
                    if (c.getColumn() == null) {
                        warnings.append("Can only add columns to the primary key\n");
                        break;
                    }
                }
            }

            if (indexType.getSelectedItem() == null) {
                warnings.append("An index type must be selected\n");
            }

            if (warnings.toString().length() == 0) {
                //The operation is successful
                index.makeColumnsLike(indexCopy);
                SQLTable parentTable = parent;
                if (index.isPrimaryKeyIndex()) {
                    try {
                        parentTable.setMagicEnabled(false);
                        for (SQLColumn c : parentTable.getColumns()) {
                            c.setPrimaryKeySeq(null);
                        }
                        int i = 0;
                        for (Column c : (List<Column>) index.getChildren()) {
                            SQLColumn column = c.getColumn();
                            if (column != null) {
                                column.setPrimaryKeySeq(Integer.MAX_VALUE);
                                parentTable.removeColumn(column);
                                parentTable.addColumn(i, column);
                                i++;
                            }
                        }
                    } finally {
                        parentTable.setMagicEnabled(true);
                    }
                }
                index.setName(name.getText());
                index.setUnique(unique.isSelected());
                index.setClustered(clustered.isSelected());
                if (indexType.getSelectedItem().toString().equals(DEFAULT_INDEX_TYPE)) {
                    index.setType(null);
                } else {
                    index.setType(indexType.getSelectedItem().toString());
                }
                Folder<SQLIndex> indicesFolder = parentTable.getIndicesFolder();
                List children = indicesFolder.getChildren();
                if (!children.contains(index)) {
                    indicesFolder.addChild(index);
                }
                index.cleanUp();
                return true;
            } else {
                JOptionPane.showMessageDialog(this, warnings.toString());
                //this is done so we can go back to this dialog after the error message
                return false;
            }
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        } finally {
            endCompoundEdit("Ending new compound edit event in index edit panel");
        }
    }

    public void discardChanges() {
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

    public void startCompoundEdit(String message) {
        fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_START, message));
    }

    public void endCompoundEdit(String message) {
        fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_END, message));
    }

    public JPanel getPanel() {
        return this;
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
}