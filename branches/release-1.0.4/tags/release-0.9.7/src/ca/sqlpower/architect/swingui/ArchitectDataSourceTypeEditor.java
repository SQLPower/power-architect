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
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.swingui.AddRemoveIcon;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ArchitectDataSourceTypeEditor implements DataEntryPanel {
    
    private static final Logger logger = Logger.getLogger(ArchitectDataSourceTypeEditor.class);
    
    /**
     * The panel that this editor's GUI lives in.
     */
    private final JPanel panel;
    
    private final DataSourceCollection dataSourceCollection;
    
    /**
     * The list of data source types.
     */
    private final JList dsTypeList;
    
    /**
     * Button for adding a new data source type.
     */
    private final JButton addDsTypeButton;
    
    /**
     * Button for deleting the selected data source type.
     */
    private final JButton removeDsTypeButton;
    
    /**
     * The panel that edits the currently-selected data source type.
     */
    private final ArchitectDataSourceTypePanel dsTypePanel;
    
    /**
     * The panel that maintains the currently-selected data source type's
     * classpath.
     */
    private final JDBCDriverPanel jdbcPanel;
    
    public ArchitectDataSourceTypeEditor(DataSourceCollection dataSourceCollection) {
        this.dataSourceCollection = dataSourceCollection;
        
        DefaultListModel dsTypeListModel = new DefaultListModel();
        for (SPDataSourceType type : dataSourceCollection.getDataSourceTypes()) {
            dsTypeListModel.addElement(type);
        }
        dsTypeList = new JList(dsTypeListModel);
        dsTypeList.setCellRenderer(new ArchitectDataSourceTypeListCellRenderer());
        
        addDsTypeButton = new JButton(new AddRemoveIcon(AddRemoveIcon.Type.ADD));
        addDsTypeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addDsType(new SPDataSourceType());
            }
        });
        
        removeDsTypeButton = new JButton(new AddRemoveIcon(AddRemoveIcon.Type.REMOVE));
        removeDsTypeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedDsType();
            }
        });
        
        dsTypePanel = new ArchitectDataSourceTypePanel();
        
        jdbcPanel = new JDBCDriverPanel();
        
        dsTypeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                try {
                    if (!e.getValueIsAdjusting()) {
                        SPDataSourceType dst =
                            (SPDataSourceType) dsTypeList.getSelectedValue();
                        switchToDsType(dst);
                    }
                } catch (ArchitectException ex) {
                    // XXX: DANGER!!! we aren't sure if this is the best parent component
                    // for the dialog.
                    ASUtils.showExceptionDialogNoReport(jdbcPanel,
                            "Can't edit this database type due to an unexpected error", ex);
                }
            }
        });
        panel = createPanel();
    }
    
    /**
     * Removes the selected data source type from the list.  If there is
     * no selected type, does nothing.
     */
    private void removeSelectedDsType() {
        SPDataSourceType type = (SPDataSourceType) dsTypeList.getSelectedValue();
        if (type != null) {
            ((DefaultListModel) dsTypeList.getModel()).removeElement(type);
            dataSourceCollection.removeDataSourceType(type);
        }
    }

    private void addDsType(SPDataSourceType type) {
        if (type == null) {
            throw new NullPointerException("Don't add null data source types, silly!");
        }
        dataSourceCollection.addDataSourceType(type);
        ((DefaultListModel) dsTypeList.getModel()).addElement(type);
        dsTypeList.setSelectedValue(type, true);
    }

    /**
     * Creates the panel layout. Requires that the GUI components have already
     * been created. Does not fill in any values into the components. See
     * {@link #switchToDsType()} for that.
     */
    private JPanel createPanel() {
        FormLayout layout = new FormLayout("60dlu, 6dlu, pref:grow", "pref, 6dlu, pref:grow, 3dlu, pref");
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        fb.setDefaultDialogBorder();
        
        JComponent addRemoveBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRemoveBar.add(addDsTypeButton);
        addRemoveBar.add(removeDsTypeButton);
        
        fb.add(new JScrollPane(dsTypeList), "1, 1, 1, 3");
        fb.add(addRemoveBar,                "1, 5");
        fb.add(dsTypePanel.getPanel(),      "3, 1");
        fb.add(jdbcPanel,                   "3, 3, 1, 3");
        
        return fb.getPanel();
    }
    
    /**
     * Returns this editor's GUI.
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Copies all the data source types and their properties back to the
     * DataSourceCollection we're editing.
     */
    public boolean applyChanges() {
        logger.debug("Applying changes to all data source types");
        applyCurrentChanges();
        ListModel lm = dsTypeList.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            SPDataSourceType dst = (SPDataSourceType) lm.getElementAt(i);
            dataSourceCollection.mergeDataSourceType(dst);
        }
        return true;
    }

    /**
     * This method is a no-op implementation, since all we have to do to discard the
     * changes is not copy them back to the model.
     */
    public void discardChanges() {
        // nothing to do
    }
    
    /**
     * Causes this editor to set up all its GUI components to edit the given data source type.
     * Null is an acceptable value, and means to make no DS Type the current type.
     */
    public void switchToDsType(SPDataSourceType dst) throws ArchitectException {
        applyCurrentChanges();
        dsTypeList.setSelectedValue(dst, true);
        dsTypePanel.editDsType(dst);
        jdbcPanel.editDsType(dst);
    }
    
    private void applyCurrentChanges() {
        dsTypePanel.applyChanges();
        jdbcPanel.applyChanges();
    }
}
