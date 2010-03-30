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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ProjectSecurityPanel implements DataEntryPanel{

    private final JPanel panel;
    private final JLabel panelLabel;
    
    private final Action closeAction;
    private final ArchitectProject securityWorkspace;
    private final ArchitectProject workspace;
    
    private List<PrivilegesEditorPanel> panels;
    
    private List<User> users;
    private List<Group> groups;
    
    private boolean hasUnsavedChanges = false;
    
    private final Dialog parent;
    
    private final Dimension buttonDim = new Dimension(16, 16);
    
    public ProjectSecurityPanel(ArchitectProject securityWorkspace, ArchitectProject workspace, Dialog parent, Action closeAction) {
        this.securityWorkspace = securityWorkspace;
        this.workspace = workspace;
        this.closeAction = closeAction;
        this.parent = parent;
        
        panelLabel = new JLabel("Permissions for '" + workspace.getName() + "'");
        panelLabel.setFont(new Font(panelLabel.getFont().getFontName(), Font.BOLD, panelLabel.getFont().getSize() + 1));
        
        panel = new JPanel();
        createPanel();
    }
    
    private void createPanel() {
        Map<User, Grant> globalGrantsForUsers = new HashMap<User, Grant>();
        Map<Group, Grant> globalGrantsForGroups = new HashMap<Group, Grant>();
        Map<User, Grant> specificGrantsForUsers = new HashMap<User, Grant>();
        Map<Group, Grant> specificGrantsForGroups = new HashMap<Group, Grant>();
        
        // Populate maps ...
        for (User user : securityWorkspace.getChildren(User.class)) {
            for (Grant grant : user.getChildren(Grant.class)) {
                if (grant.getType() != null && grant.getType().equals(ArchitectProject.class.getName())) {
                    globalGrantsForUsers.put(user, grant);
                }
                if (grant.getSubject() != null && grant.getSubject().equals(workspace.getUUID())) {
                    specificGrantsForUsers.put(user, grant);
                }
            }
        }
        for (Group group : securityWorkspace.getChildren(Group.class)) {
            for (Grant grant : group.getChildren(Grant.class)) {
                if (grant.getType() != null && grant.getType().equals(ArchitectProject.class.getName())) {
                    globalGrantsForGroups.put(group, grant);
                }
                if (grant.getSubject() != null && grant.getSubject().equals(workspace.getUUID())) {
                    specificGrantsForGroups.put(group, grant);
                }
            }
        }
        
        if (users == null)  {
            users = new ArrayList<User>();
            users.addAll(specificGrantsForUsers.keySet());
            Collections.sort(users, new Comparator<User>() {
                public int compare(User o1, User o2) {
                    return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
                }
            });
        }
        if (groups == null) {
            groups = new ArrayList<Group>();
            groups.addAll(specificGrantsForGroups.keySet());
            Collections.sort(groups, new Comparator<Group>() {
                public int compare(Group o1, Group o2) {
                    return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
                }
            });
        }
        
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow", "pref:grow, 5dlu, pref:grow, pref:grow, 5dlu, pref:grow, pref:grow, 5dlu, pref:grow"));
        
        builder.add(panelLabel, cc.xy(1,1));
        
        JLabel userPermissions = new JLabel("User Permissions");
        userPermissions.setFont(new Font(userPermissions.getFont().getFontName(), Font.BOLD, userPermissions.getFont().getSize()));
        builder.add(userPermissions, cc.xy(1, 3));
        
        JPanel userListPanel = createUserListPanel(specificGrantsForUsers, globalGrantsForUsers);
        
        builder.add(userListPanel, cc.xy(1, 4));
        
        JLabel groupPermissions = new JLabel("Group Permissions");
        groupPermissions.setFont(userPermissions.getFont());
        builder.add(groupPermissions, cc.xy(1, 6));
        
        builder.add(createGroupListPanel(specificGrantsForGroups, globalGrantsForGroups), cc.xy(1, 7));
        
        JButton okButton = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                applyChanges();
                closeAction.actionPerformed(e);
            }
        });
        
        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                for (DataEntryPanel p : panels) {
                    p.discardChanges();
                }
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
    }
    
    private JPanel createUserListPanel(Map<User, Grant> specificGrantsForUsers, Map<User, Grant> globalGrantsForUsers) {
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));
        int lineNo = 0;
        builder.appendRow(builder.getDefaultRowSpec());
        lineNo++;
        builder.add(new JLabel("User Name"), cc.xy(1, lineNo));
        builder.add(new JLabel("Modify"), cc.xy(3, lineNo));
        builder.add(new JLabel("View"), cc.xy(5, lineNo));
        builder.add(new JLabel("Delete"), cc.xy(7, lineNo));
        builder.add(new JLabel("Grant"), cc.xy(9, lineNo));
        
        JButton addUserButton = new JButton(new AbstractAction("+") {
            public void actionPerformed(ActionEvent e) {
                List<User> availableUsers = securityWorkspace.getChildren(User.class);
                availableUsers.removeAll(users);
                
                JList userList = new JList(new DefaultListModel());
                for (User u : availableUsers) {
                    ((DefaultListModel) userList.getModel()).addElement(u);
                }
                
                Object[] messages = new Object[] {"Select Users to Add", new JScrollPane(userList)};

                String[] options = { "OK", "Cancel"};
                int option = JOptionPane.showOptionDialog(getPanel(), messages, "", JOptionPane.DEFAULT_OPTION, 
                                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                
                if (option == 0) {
                    int [] selected = userList.getSelectedIndices();
                    for (int i = 0; i < selected.length; i++) {
                        User user = availableUsers.get(selected[i]);
                        user.addGrant(new Grant(workspace.getUUID(), null, false, false, false, false, false));
                        users.add(user);
                    }
                    panel.removeAll();
                    createPanel();
                    parent.pack();
                }
            }
        });
        
        addUserButton.setPreferredSize(buttonDim);
        builder.add(addUserButton, cc.xy(11, lineNo));
        
        panels = new ArrayList<PrivilegesEditorPanel>();
        
        for (User user : users) {
            // can pass in null, it will just be empty
            final PrivilegesEditorPanel specific = new PrivilegesEditorPanel(specificGrantsForUsers.get(user), user, workspace.getUUID(), null, securityWorkspace);
            final PrivilegesEditorPanel global = new PrivilegesEditorPanel(globalGrantsForUsers.get(user), user, workspace.getUUID(), null, securityWorkspace);
            
            panels.add(specific);
            panels.add(global);
            
            global.getCreateModifyPrivilege().setEnabled(false);
            global.getDeletePrivilege().setEnabled(false);
            global.getViewPrivilege().setEnabled(false);
            global.getGrantPrivilege().setEnabled(false);
            
            global.getCreateModifyPrivilege().setText(null);
            global.getDeletePrivilege().setText(null);
            global.getViewPrivilege().setText(null);
            global.getGrantPrivilege().setText(null);
            
            specific.getCreateModifyPrivilege().setText(null);
            specific.getDeletePrivilege().setText(null);
            specific.getViewPrivilege().setText(null);
            specific.getGrantPrivilege().setText(null);
            
            builder.appendRow(builder.getDefaultRowSpec());
            lineNo++;
            builder.add(new JLabel(user.getName()), cc.xy(1, lineNo));
            builder.add(global.getCreateModifyPrivilege().isSelected() ? global.getCreateModifyPrivilege() : specific.getCreateModifyPrivilege(), cc.xy(3, lineNo));
            builder.add(global.getViewPrivilege().isSelected() ? global.getViewPrivilege() : specific.getViewPrivilege(), cc.xy(5, lineNo));
            builder.add(global.getDeletePrivilege().isSelected() ? global.getDeletePrivilege() : specific.getDeletePrivilege(), cc.xy(7, lineNo));
            builder.add(global.getGrantPrivilege().isSelected() ? global.getGrantPrivilege() : specific.getGrantPrivilege(), cc.xy(9, lineNo));
        
            final User u = user;
            JButton removeUserButton = new JButton(new AbstractAction("-") {
                public void actionPerformed(ActionEvent e) {
                    
                    specific.discardChanges();
                    if (specific.getGrant() != null) {
                        try {
                            u.removeChild(specific.getGrant());
                        } catch (IllegalArgumentException e1) {
                            throw new RuntimeException("unable to remove grant", e1);
                        } catch (ObjectDependentException e1) {
                            throw new RuntimeException("unable to remove grant", e1);
                        }
                    }
                    
                    panels.remove(specific);
                    panels.remove(global);
                    
                    users.remove(u);
                    panel.removeAll();
                    createPanel();
                    parent.pack();
                }
            });
            
            removeUserButton.setPreferredSize(buttonDim);
            builder.add(removeUserButton, cc.xy(11, lineNo));
            
        }
        
        return builder.getPanel();
    }
    
    private JPanel createGroupListPanel(Map<Group, Grant> specificGrantsForGroups, Map<Group, Grant> globalGrantsForGroups) {
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));
        int lineNo = 0;

        builder.appendRow(builder.getDefaultRowSpec());
        lineNo++;
        builder.add(new JLabel("Group Name"), cc.xy(1, lineNo));
        builder.add(new JLabel("Modify"), cc.xy(3, lineNo));
        builder.add(new JLabel("View"), cc.xy(5, lineNo));
        builder.add(new JLabel("Delete"), cc.xy(7, lineNo));
        builder.add(new JLabel("Grant"), cc.xy(9, lineNo));
        
        JButton addGroupButton = new JButton(new AbstractAction("+") {
            public void actionPerformed(ActionEvent e) {
                List<Group> availableGroups = securityWorkspace.getChildren(Group.class);
                availableGroups.removeAll(groups);
                
                JList groupList = new JList(new DefaultListModel());
                for (Group g : availableGroups) {
                    ((DefaultListModel) groupList.getModel()).addElement(g);
                }
                
                Object[] messages = new Object[] {"Select Groups to Add", new JScrollPane(groupList)};

                String[] options = { "OK", "Cancel",};
                int option = JOptionPane.showOptionDialog(getPanel(), messages, "", JOptionPane.DEFAULT_OPTION, 
                                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                
                if (option == 0) {
                    int [] selected = groupList.getSelectedIndices();
                    for (int i = 0; i < selected.length; i++) {
                        Group group = availableGroups.get(selected[i]);
                        group.addGrant(new Grant(workspace.getUUID(), null, false, false, false, false, false));
                        groups.add(group);
                    }
                    panel.removeAll();
                    createPanel();
                    parent.pack();
                }
            }
        });
        
        addGroupButton.setPreferredSize(buttonDim);
        builder.add(addGroupButton, cc.xy(11, lineNo));
        
        for (Group group : groups) {
            // can pass in null, it will just be empty
            final PrivilegesEditorPanel specific = new PrivilegesEditorPanel(specificGrantsForGroups.get(group), group, workspace.getUUID(), null, securityWorkspace);
            final PrivilegesEditorPanel global = new PrivilegesEditorPanel(globalGrantsForGroups.get(group), group, workspace.getUUID(), null, securityWorkspace);
            
            panels.add(specific);
            panels.add(global);
            
            global.getCreateModifyPrivilege().setEnabled(false);
            global.getDeletePrivilege().setEnabled(false);
            global.getViewPrivilege().setEnabled(false);
            global.getGrantPrivilege().setEnabled(false);
            
            global.getCreateModifyPrivilege().setText(null);
            global.getDeletePrivilege().setText(null);
            global.getViewPrivilege().setText(null);
            global.getGrantPrivilege().setText(null);
            
            specific.getCreateModifyPrivilege().setText(null);
            specific.getDeletePrivilege().setText(null);
            specific.getViewPrivilege().setText(null);
            specific.getGrantPrivilege().setText(null);
            
            builder.appendRow(builder.getDefaultRowSpec());
            lineNo++;
            builder.add(new JLabel(group.getName()), cc.xy(1, lineNo));
            builder.add(global.getCreateModifyPrivilege().isSelected() ? global.getCreateModifyPrivilege() : specific.getCreateModifyPrivilege(), cc.xy(3, lineNo));
            builder.add(global.getViewPrivilege().isSelected() ? global.getViewPrivilege() : specific.getViewPrivilege(), cc.xy(5, lineNo));
            builder.add(global.getDeletePrivilege().isSelected() ? global.getDeletePrivilege() : specific.getDeletePrivilege(), cc.xy(7, lineNo));
            builder.add(global.getGrantPrivilege().isSelected() ? global.getGrantPrivilege() : specific.getGrantPrivilege(), cc.xy(9, lineNo));
        
            final Group g = group;
            JButton removeGroupButton = new JButton(new AbstractAction("-") {
                public void actionPerformed(ActionEvent e) {
                    
                    specific.getCreateModifyPrivilege().setSelected(false);
                    specific.getDeletePrivilege().setSelected(false);
                    specific.getViewPrivilege().setSelected(false);
                    specific.getGrantPrivilege().setSelected(false);
                    specific.applyChanges();
                    panels.remove(specific);
                    panels.remove(global);
                    
                    groups.remove(g);
                    panel.removeAll();
                    createPanel();
                    parent.pack();
                }
            });
            
            removeGroupButton.setPreferredSize(buttonDim);
            builder.add(removeGroupButton, cc.xy(11, lineNo));
        }
        
        return builder.getPanel();
    }
    
    public boolean applyChanges() {
        for (DataEntryPanel p : panels) {
            p.applyChanges();
        }
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
}
