/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.enterprise;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.architect.swingui.action.enterprise.RefreshProjectAction;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The panel that allows editing permissions on a specific project. This may
 * look like a {@link DataEntryPanel}, may implement {@link DataEntryPanel} but
 * don't be fooled, it does not use apply changes, discard changes, or can be
 * put into
 * {@link DataEntryPanelBuilder#createDataEntryPanelDialog(DataEntryPanel, Component, String, String)}
 * . Instead this panel does a nasty hack of recreating the work of {@link DataEntryPanelBuilder}
 * and makes its own buttons to call apply changes on inner classes that are
 * {@link DataEntryPanel}s.
 */
public class ProjectSecurityPanel implements DataEntryPanel{

    private static final ImageIcon ADD_ICON = new ImageIcon(RefreshProjectAction.class.getResource("/icons/famfamfam/add.png"));
    private static final ImageIcon REMOVE_ICON = new ImageIcon(RefreshProjectAction.class.getResource("/icons/famfamfam/delete.png"));
    
    private final JPanel panel;
    private final JLabel panelLabel;
    
    private final Action closeAction;
    private final ArchitectSwingProject securityWorkspace;
    private final SPObject subject;
    private final Class<?> type;
    
    private final String username;
    
    private GroupOrUserTableModel userModel;
    private GroupOrUserTableModel groupModel;
    
    private boolean hasUnsavedChanges = false;
    
    private final Dialog d;
    
    public ProjectSecurityPanel(ArchitectSwingProject securityWorkspace, SPObject subject, Class<?> type, String username, Dialog d, Action closeAction) {
        this.securityWorkspace = securityWorkspace;
        this.subject = subject;
        this.type = type;
        this.username = username;
        this.closeAction = closeAction;
        this.d = d;
        
        panelLabel = new JLabel("Permissions for '" + (subject != null? subject.getName() : type.getSimpleName()) + "'");
        panelLabel.setFont(new Font(panelLabel.getFont().getFontName(), Font.BOLD, panelLabel.getFont().getSize() + 1));
        
        panel = new JPanel();
        refreshPanel();
    }
    
    /**
     * This rebuilds the panel based on the {@link #userModel} and {@link #groupModel}.
     * To do this it also removes all of the panels from the main {@link #panel} and
     * adds new ones to it.
     */
    private void refreshPanel() {
        
        userModel = new GroupOrUserTableModel(User.class);
        groupModel = new GroupOrUserTableModel(Group.class);
        
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref", "pref:grow, 5dlu, pref:grow, pref:grow, 5dlu, pref:grow, pref:grow, 5dlu, pref:grow"));
        builder.add(panelLabel, cc.xy(1,1));
        
        // User list and headers
        JLabel userPermissions = new JLabel("User Permissions");
        userPermissions.setFont(new Font(userPermissions.getFont().getFontName(), Font.BOLD, userPermissions.getFont().getSize()));
        builder.add(userPermissions, cc.xy(1, 3));
        builder.add(userModel.getPanel(), cc.xy(1, 4));
        
        // Group list and headers
        JLabel groupPermissions = new JLabel("Group Permissions");
        groupPermissions.setFont(userPermissions.getFont());
        builder.add(groupPermissions, cc.xy(1, 6));
        builder.add(groupModel.getPanel(), cc.xy(1, 7));
        
        JButton okButton = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                userModel.applyChanges();
                groupModel.applyChanges();
                closeAction.actionPerformed(e);
            }
        });
        
        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                userModel.discardChanges();
                groupModel.discardChanges();
                closeAction.actionPerformed(e);
            }
        });
        
        ButtonBarBuilder buttonBuilder = ButtonBarBuilder.createLeftToRightBuilder();
        buttonBuilder.addGlue();
        buttonBuilder.addGridded(okButton);
        buttonBuilder.addRelatedGap();
        buttonBuilder.addGridded(cancelButton);
        buttonBuilder.setDefaultButtonBarGapBorder();
        
        builder.add(buttonBuilder.getPanel(), cc.xy(1, 9));
        builder.setDefaultDialogBorder();
        
        panel.removeAll();
        panel.add(builder.getPanel());
        panel.revalidate();
        disableIfNecessary();
    }
    
    public boolean applyChanges() {
        return true;
    }

    public void discardChanges() {
    }


    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    public void disableIfNecessary() {
        User user = null;
        List<Grant> grantsForUser = new ArrayList<Grant>();
        for (User aUser : securityWorkspace.getChildren(User.class)) {
            if (aUser.getUsername().equals(username)) {
                user = aUser;
            }
        }
        
        if (user == null) throw new IllegalStateException("User cannot possibly be null");
    
        for (Grant g : user.getChildren(Grant.class)) {
            grantsForUser.add(g);
        }
        
        for (Group g : securityWorkspace.getChildren(Group.class)) {
            for (GroupMember gm : g.getChildren(GroupMember.class)) {
                if (gm.getUser().getUUID().equals(user.getUUID())) {
                    for (Grant gr : g.getChildren(Grant.class)) {
                        grantsForUser.add(gr);
                    }
                }
            }
        }
        
        boolean disable = true;
        for (Grant g : grantsForUser) {
            if ((!g.isSystemLevel() && subject != null && g.getSubject().equals(subject.getUUID())) 
                    || (g.isSystemLevel() && g.getType().equals(type.getName()))) {
                if (g.isGrantPrivilege()) {
                    disable = false;
                }
            }
        }
        
        if (disable) {
            for (Component [] componentArray : userModel.getComponents()) {
                for (Component component : componentArray) {
                    component.setEnabled(false);
                }
            }
            for (Component [] componentArray : groupModel.getComponents()) {
                for (Component component : componentArray) {
                    component.setEnabled(false);
                }
            }
        }
    }
    
    private class RowCellRenderer implements TableCellRenderer {
        private GroupOrUserTableModel model;
        
        public RowCellRenderer(GroupOrUserTableModel model) {
            this.model = model;
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (column == 0) {
                table.setToolTipText(((JLabel) model.getValueAt(row, column)).getText());
            }
            
            return (Component) model.getValueAt(row, column);
        }
    }
    
    private class RowCellEditor implements TableCellEditor {

        private GroupOrUserTableModel model;
        
        public RowCellEditor(GroupOrUserTableModel model) {
            this.model = model;
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return (Component) model.getValueAt(row, column);
        }

        public void addCellEditorListener(CellEditorListener l) {
        }

        public void cancelCellEditing() {
        }

        public Object getCellEditorValue() {
            return null;
        }

        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        public void removeCellEditorListener(CellEditorListener l) {
        }

        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        public boolean stopCellEditing() {
            return true;
        }
        
    }
    
    private class GroupOrUserTableModel implements TableModel, DataEntryPanel {

        private final Component[] headerRow;
        private final List<Component[]> rows;
        private final int numColumns = 7;
        private final List<DataEntryPanel> panels;
        
        private final List<SPObject> objectsWithSpecificGrants;
        private final List<SPObject> objectsWithoutSpecificGrants;
        private final List<SPObject> objectsWithGlobalGrants;
        private final List<SPObject> objectsWithoutGlobalGrants;
        
        public GroupOrUserTableModel(final Class<? extends SPObject> groupOrUserClass) {
            panels = new ArrayList<DataEntryPanel>();
            rows = new ArrayList<Component[]>();
            objectsWithoutSpecificGrants = new ArrayList<SPObject>();
            objectsWithSpecificGrants = new ArrayList<SPObject>();
            objectsWithGlobalGrants = new ArrayList<SPObject>();
            objectsWithoutGlobalGrants = new ArrayList<SPObject>();
            
            Map<SPObject, Grant> specificGrants = new HashMap<SPObject, Grant>();
            Map<SPObject, Grant> globalGrants = new HashMap<SPObject, Grant>();
            
            for (SPObject object : securityWorkspace.getChildren(groupOrUserClass)) {
                for (Grant grant : object.getChildren(Grant.class)) {
                    if (grant.isSystemLevel() && grant.getType().equals(type.getName())) {
                        globalGrants.put(object, grant);
                    }
                    if (subject != null) {
                        if (!grant.isSystemLevel() && grant.getSubject().equals(subject.getUUID())) {
                            specificGrants.put(object, grant);
                        }
                    }
                }
            }
            
            for (SPObject obj : securityWorkspace.getChildren(groupOrUserClass)) {
                objectsWithoutSpecificGrants.add(obj);
                objectsWithoutGlobalGrants.add(obj);
            }
            for (SPObject obj : specificGrants.keySet()) {
                objectsWithSpecificGrants.add(obj);
            }
            for (SPObject obj : globalGrants.keySet()) {
                objectsWithGlobalGrants.add(obj);
            }
            objectsWithoutSpecificGrants.removeAll(objectsWithSpecificGrants);
            objectsWithoutGlobalGrants.removeAll(objectsWithGlobalGrants);
            
            Comparator<SPObject> comparator = new Comparator<SPObject>() {
                public int compare(SPObject o1, SPObject o2) {
                    return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
                }
            };
            
            Collections.sort(objectsWithSpecificGrants, comparator);
            Collections.sort(objectsWithoutSpecificGrants, comparator);
            Collections.sort(objectsWithGlobalGrants, comparator);
            Collections.sort(objectsWithoutGlobalGrants, comparator);
            
            headerRow = new Component[numColumns];
            headerRow[0] = new JLabel("Name");
            headerRow[1] = new JLabel("View");
            headerRow[2] = new JLabel("Create");
            headerRow[3] = new JLabel("Modify");
            headerRow[4] = new JLabel("Delete");
            headerRow[5] = new JLabel("Grant");
            JButton addButton = new JButton(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    
                    JList list = new JList(new DefaultListModel());
                    DefaultListModel model = (DefaultListModel) list.getModel();
                    for (SPObject obj : (subject != null ? objectsWithoutSpecificGrants : objectsWithoutGlobalGrants)) {
                        model.addElement(obj);
                    }
                    
                    Object [] message = new Object[] {
                            "Select " + groupOrUserClass.getSimpleName() + "s to add", 
                            new JScrollPane(list)
                    };
                    Object [] selections = new Object[] {
                            "OK", "Cancel"
                    };
                    
                    Object selection = JOptionPane.showOptionDialog(d, message, "", JOptionPane.DEFAULT_OPTION, 
                            JOptionPane.INFORMATION_MESSAGE, null, selections, selections[0]);
                    
                    if (((Integer) selection).intValue() == 0) {
                        
                        int [] selectedIndices = list.getSelectedIndices();
                        for (int i = 0; i < selectedIndices.length; i++) {
                            SPObject obj = (SPObject) model.get(selectedIndices[i]);
                            if (subject != null) {
                                obj.addChild(new Grant(subject, false, false, false, false, false),
                                        obj.getChildren(Grant.class).size());
                            } else {
                                obj.addChild(new Grant(type.getName(), false, false, false, false, false),
                                        obj.getChildren(Grant.class).size());
                            }
                        }
                        
                        refreshPanel();
                    }
                }
            });
            addButton.setIcon(ADD_ICON);
            addButton.setBorderPainted(false);
            addButton.setContentAreaFilled(false);
            
            headerRow[6] = addButton;
            rows.add(headerRow);
            
            if (subject != null) {
                for (final SPObject object : objectsWithSpecificGrants) {
                    // can pass in null, it will just be empty
                    final PrivilegesEditorPanel specific = new PrivilegesEditorPanel(specificGrants.get(object), object, subject.getUUID(), type.getName(), username, securityWorkspace);
                    final PrivilegesEditorPanel global = new PrivilegesEditorPanel(globalGrants.get(object), object, subject.getUUID(), type.getName(), username, securityWorkspace);
                    
                    panels.add(specific);
                    panels.add(global);
                    
                    global.getCreatePrivilege().setEnabled(false);
                    global.getModifyPrivilege().setEnabled(false);
                    global.getDeletePrivilege().setEnabled(false);
                    global.getViewPrivilege().setEnabled(false);
                    global.getGrantPrivilege().setEnabled(false);
                    
                    global.getCreatePrivilege().setText(null);
                    global.getModifyPrivilege().setText(null);
                    global.getDeletePrivilege().setText(null);
                    global.getViewPrivilege().setText(null);
                    global.getGrantPrivilege().setText(null);
                    
                    specific.getCreatePrivilege().setText(null);
                    specific.getModifyPrivilege().setText(null);
                    specific.getDeletePrivilege().setText(null);
                    specific.getViewPrivilege().setText(null);
                    specific.getGrantPrivilege().setText(null);
                    
                    JButton removeButton = new JButton(new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                object.removeChild(specific.getGrant());
                            } catch (Exception ex) {
                                throw new RuntimeException("Unable to remove grant from object", ex);
                            }
                            
                            refreshPanel();
                        }
                    });
                    removeButton.setIcon(REMOVE_ICON);
                    removeButton.setBorderPainted(false);
                    removeButton.setContentAreaFilled(false);
                    
                    Component [] rowComponents = new Component[numColumns]; 
                    rowComponents[0] = new JLabel(object.getName());
                    rowComponents[1] = global.getViewPrivilege().isSelected() ? global.getViewPrivilege() : specific.getViewPrivilege();
                    rowComponents[2] = global.getCreatePrivilege().isSelected() ? global.getCreatePrivilege() : specific.getCreatePrivilege();
                    rowComponents[3] = global.getModifyPrivilege().isSelected() ? global.getModifyPrivilege() : specific.getModifyPrivilege();
                    rowComponents[4] = global.getDeletePrivilege().isSelected() ? global.getDeletePrivilege() : specific.getDeletePrivilege();
                    rowComponents[5] = global.getGrantPrivilege().isSelected() ? global.getGrantPrivilege() : specific.getGrantPrivilege();
                    rowComponents[6] = removeButton;
                    
                    for (Component component : rowComponents) component.setBackground(Color.white);
                    
                    rows.add(rowComponents);
                }
            } else {
                for (final SPObject object : objectsWithGlobalGrants) {
                    // can pass in null, it will just be empty
                    final PrivilegesEditorPanel global = new PrivilegesEditorPanel(globalGrants.get(object), object, null, type.getName(), username, securityWorkspace);
                    
                    panels.add(global);
                    
                    global.getCreatePrivilege().setText(null);
                    global.getModifyPrivilege().setText(null);
                    global.getDeletePrivilege().setText(null);
                    global.getViewPrivilege().setText(null);
                    global.getGrantPrivilege().setText(null);
                    
                    JButton removeButton = new JButton(new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                object.removeChild(global.getGrant());
                            } catch (Exception ex) {
                                throw new RuntimeException("Unable to remove grant from object", ex);
                            }
                            
                            refreshPanel();
                        }
                    });
                    removeButton.setIcon(REMOVE_ICON);
                    removeButton.setBorderPainted(false);
                    removeButton.setContentAreaFilled(false);
                    
                    Component [] rowComponents = new Component[numColumns]; 
                    rowComponents[0] = new JLabel(object.getName());
                    rowComponents[1] = global.getViewPrivilege();
                    rowComponents[2] = global.getCreatePrivilege();
                    rowComponents[3] = global.getModifyPrivilege();
                    rowComponents[4] = global.getDeletePrivilege();
                    rowComponents[5] = global.getGrantPrivilege();
                    rowComponents[6] = removeButton;
                    
                    for (Component component : rowComponents) component.setBackground(Color.white);
                    
                    rows.add(rowComponents);
                }
            }
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            return headerRow[columnIndex].getClass();
        }

        public int getColumnCount() {
            return numColumns;
        }

        public int getRowCount() {
            return rows.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return rows.get(rowIndex)[columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
        
        public List<Component[]> getComponents() {
            return rows;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
        public void addTableModelListener(TableModelListener l) {}
        public void removeTableModelListener(TableModelListener l) {}
        public String getColumnName(int columnIndex) {return null;}

        public void discardChanges() {
            for (DataEntryPanel dep : panels) {
                dep.discardChanges();
            }
        }

        public JComponent getPanel() {
            JTable table = new JTable(this);
            table.setTableHeader(null);
            
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn col = table.getColumnModel().getColumn(i);
                col.setCellRenderer(new RowCellRenderer(this));
                col.setCellEditor(new RowCellEditor(this));
            }
            
            table.getColumnModel().getColumn(6).setPreferredWidth(table.getRowHeight());
            table.setShowHorizontalLines(false);
            table.setShowVerticalLines(false);
            
            JScrollPane scrollpane = new JScrollPane(table);
            scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            
            int preferredHeight = table.getRowHeight() * 7;
            int preferredWidth = scrollpane.getVerticalScrollBar().getWidth();
            for (int i = 0; i < table.getColumnCount(); i++) {
                preferredWidth += table.getColumnModel().getColumn(i).getWidth();
            }
            
            scrollpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
            scrollpane.getViewport().setBackground(Color.WHITE);
            
            return scrollpane;
        }

        public boolean applyChanges() {
            boolean success = true;
            
            for (DataEntryPanel dep : panels) {
                if (!dep.applyChanges()) {
                    success = false;
                }
            }
            
            return success;
        }
        
        public boolean hasUnsavedChanges() {
            // TODO Auto-generated method stub
            return false;
        }
    }
    
    public static Action createShowAction(final ArchitectSwingProject securityWorkspace, final SPObject subject, final Class<?> type,
            final String username, final JComponent panel) {
       return new AbstractAction("Manage Security Settings...") {
                public void actionPerformed(ActionEvent e) {
                    final JDialog d = SPSUtils.makeOwnedDialog(panel, "Security Manager");
                    
                    Action closeAction = new AbstractAction("Close") {
                        public void actionPerformed(ActionEvent e) {
                            d.dispose();
                        }
                    };
                        
                    ProjectSecurityPanel spm = new ProjectSecurityPanel(
                            securityWorkspace, subject, type, username, d, closeAction);
                    d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    d.setContentPane(spm.getPanel());
                        
                    SPSUtils.makeJDialogCancellable(d, null);
                    d.pack();
                    d.setLocationRelativeTo(panel);
                    d.setVisible(true);
                }
            };
    }
}
