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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.architect.swingui.action.enterprise.RefreshProjectAction;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GroupEditorPanel implements DataEntryPanel {

    private static final ImageIcon RIGHT_ARROW = new ImageIcon(RefreshProjectAction.class.getResource("/icons/arrow-right.png"));
    private static final ImageIcon LEFT_ARROW = new ImageIcon(RefreshProjectAction.class.getResource("/icons/arrow-left.png"));
    private static final ImageIcon USER_ICON = new ImageIcon(GroupEditorPanel.class.getResource("icons/user.png"));
    
    private final Group group;
    
    private final JPanel panel;

    private final JLabel nameLabel;
    private final JTextField nameTextField;
    
    private final JList currentUsersList;
    private final JLabel currentUsersLabel;
    private final JScrollPane currentUsersScrollPane;
    
    private final JList availableUsersList;
    private final JLabel availableUsersLabel;
    private final JScrollPane availableUsersScrollPane;
    
    private final ArchitectSwingProject securityWorkspace;
    
    private final PrivilegesEditorPanel privilegesEditorPanel;
    
    private final Action closeAction;
    
    private final Action addAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            Object[] selection = availableUsersList.getSelectedValues();
            
            if (selection.length==0) {
                return;
            }
            
            hasUnsavedChanges = true;
            
            DefaultListModel availableUsersModel = (DefaultListModel) availableUsersList.getModel();
            DefaultListModel currentUsersModel = (DefaultListModel) currentUsersList.getModel();
            for (Object object : selection) {
                availableUsersModel.removeElement(object);
                currentUsersModel.addElement(object);
            }
        }
    };
    
    private final Action removeAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            Object[] selection = currentUsersList.getSelectedValues();
            
            if (selection.length==0) {
                return;
            }
            
            hasUnsavedChanges = true;
            
            DefaultListModel currentUsersModel = (DefaultListModel) currentUsersList.getModel();
            DefaultListModel availableUsersModel = (DefaultListModel) availableUsersList.getModel();
            for (Object object : selection) {
                currentUsersModel.removeElement(object);
                availableUsersModel.addElement(object);
            }   
        }
    };
    
    private boolean hasUnsavedChanges = false;
    
    private final DocumentListener textFieldListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent e) { hasUnsavedChanges = true; }
        public void insertUpdate(DocumentEvent e)  { hasUnsavedChanges = true; }
        public void removeUpdate(DocumentEvent e)  { hasUnsavedChanges = true; }
    };
    
    private final Action okAction = new AbstractAction("Apply") {
        public void actionPerformed(ActionEvent e) {
            applyChanges();
        }
    };
    
    private final Action cancelAction = new AbstractAction("Close") {
        public void actionPerformed(ActionEvent e) {
            discardChanges();
            closeAction.actionPerformed(e);
        }
    };
    
    private final String username;
    
    public GroupEditorPanel(Group baseGroup, String username, Action closeAction) {
        this.group = baseGroup;
        this.securityWorkspace = (ArchitectSwingProject) group.getParent();
        this.username = username;
        this.closeAction = closeAction;
        
        final Dimension prefScrollPaneDimension = new Dimension(250, 300);

        nameLabel = new JLabel("Group Name");
        
        nameTextField = new JTextField(25);
        nameTextField.setText(group.getName());
        nameTextField.getDocument().addDocumentListener(textFieldListener);
        
        ListCellRenderer userListCellRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setIcon(USER_ICON);
                return this;
            } 
        };
        
        availableUsersLabel = new JLabel("Available Users");
        availableUsersList = new JList(new DefaultListModel());
        availableUsersList.setCellRenderer(userListCellRenderer);
        availableUsersScrollPane = new JScrollPane(availableUsersList);
        availableUsersScrollPane.setPreferredSize(prefScrollPaneDimension);
        
        currentUsersLabel = new JLabel("Current Users");
        currentUsersList = new JList(new DefaultListModel());
        currentUsersList.setCellRenderer(userListCellRenderer);
        
        currentUsersScrollPane = new JScrollPane(currentUsersList);
        currentUsersScrollPane.setPreferredSize(prefScrollPaneDimension);
        
        Grant globalGrant = null;
        for (Grant grant : baseGroup.getChildren(Grant.class)) {
            if (grant.isSystemLevel() && grant.getType() != null && 
                    grant.getType().equals(ArchitectSwingProject.class.getName())) {
                if (globalGrant != null) {
                    throw new IllegalStateException(
                            "Multiple grants for system level workspace under the same group found.");
                }
                
                globalGrant = grant;
            }
        }

        if (globalGrant != null) {
            privilegesEditorPanel = new PrivilegesEditorPanel(globalGrant, baseGroup, null, ArchitectSwingProject.class.getName(), username, securityWorkspace);
        } else {
            privilegesEditorPanel = new PrivilegesEditorPanel(null, baseGroup, null, ArchitectSwingProject.class.getName(), username, securityWorkspace);
        }
        
        JButton addButton = new JButton(addAction);
        addButton.setIcon(RIGHT_ARROW);
        JButton removeButton = new JButton(removeAction);
        removeButton.setIcon(LEFT_ARROW);
        
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder upperPanelBuilder = new DefaultFormBuilder(new FormLayout(
                "pref, 5dlu, pref:grow", "pref, 5dlu"));
        upperPanelBuilder.add(nameLabel, cc.xy(1, 1));
        upperPanelBuilder.add(nameTextField, cc.xyw(3, 1, 1));
        
        DefaultFormBuilder buttonPanelBuilder = new DefaultFormBuilder(new FormLayout(
                "pref", "pref:grow, pref, 5dlu, pref, pref:grow"));
        buttonPanelBuilder.add(addButton, cc.xy(1,2));
        buttonPanelBuilder.add(removeButton, cc.xy(1, 5));
        
        DefaultFormBuilder centrePanelBuilder = new DefaultFormBuilder(new FormLayout(
                "pref, 5dlu, pref, 5dlu, pref", "pref, pref:grow"));
        centrePanelBuilder.add(availableUsersLabel, cc.xy(1, 1));
        centrePanelBuilder.add(currentUsersLabel, cc.xy(5, 1));
        centrePanelBuilder.add(availableUsersScrollPane, cc.xy(1, 2));
        centrePanelBuilder.add(buttonPanelBuilder.getPanel(), cc.xy(3, 2));
        centrePanelBuilder.add(currentUsersScrollPane, cc.xy(5, 2));
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow", "pref, 3dlu, pref:grow, 5dlu, pref"));
        builder.add(upperPanelBuilder.getPanel(), cc.xy(1, 1)); 
        builder.add(centrePanelBuilder.getPanel(), cc.xy(1, 3));
        
        DefaultFormBuilder bottomBuilder = new DefaultFormBuilder(new FormLayout("pref:grow, 5dlu, pref:grow", "pref, 3dlu, pref"));
        bottomBuilder.add(new JLabel("System Privileges"), cc.xy(1, 1));
        bottomBuilder.add(privilegesEditorPanel.getPanel(), cc.xy(1, 3));
        
        ButtonBarBuilder2 bbb = ButtonBarBuilder2.createLeftToRightBuilder();
        bbb.addGlue();
        bbb.addButton(new JButton(okAction));
        bbb.addRelatedGap();
        bbb.addButton(new JButton(cancelAction));
        
        bottomBuilder.add(bbb.getPanel(), cc.xy(3, 3));
        builder.add(bottomBuilder.getPanel(), cc.xy(1, 5));
        builder.setDefaultDialogBorder();
        
        panel = builder.getPanel();
        
        fillUserLists();
        disableIfNecessary();
    }
    
    /**
     * Calling this method will update the two lists of available and existing
     * groups a user can be in.
     * <p>
     * XXX This is almost identical to the one in UserEditorPanel
     */
    private void fillUserLists() {
        List<User> users = new ArrayList<User>(securityWorkspace.getChildren(User.class));
        List<GroupMember> groupMembers = group.getChildren(GroupMember.class);
        
        List<User> toRemove = new ArrayList<User>();
        for (User user : users) {
            for (GroupMember membership : groupMembers) {
                if (membership.getUser().getUUID().equals(user.getUUID())) {
                    toRemove.add(user);
                }
            }
        }
        users.removeAll(toRemove);
        
        DefaultListModel availableUsersModel = (DefaultListModel) availableUsersList.getModel();
        availableUsersModel.removeAllElements();
        for (User user : users) {
            availableUsersModel.addElement(user);
        }
        
        DefaultListModel currentUsersModel = (DefaultListModel) currentUsersList.getModel();
        currentUsersModel.removeAllElements();
        for (GroupMember member : groupMembers) {
            currentUsersModel.addElement(member.getUser());
        }
    }
    
    public JPanel getPanel() {
        return panel;
    }

    public boolean applyChanges() {
        privilegesEditorPanel.applyChanges();
        
        try {
            if (hasUnsavedChanges()) {
                securityWorkspace.begin("Applying changes to the security model");
                
                if (!group.getName().equals(nameTextField.getText())) {
                    group.setName(nameTextField.getText());
                }
                
                List<GroupMember> previousUsers = group.getChildren(GroupMember.class);
                
                // Add users ...
                DefaultListModel currentUsersModel = (DefaultListModel) currentUsersList.getModel();
                for (int i = 0; i < currentUsersModel.size(); i++) {
                    User user = (User) currentUsersModel.get(i);
                    
                    boolean addMe = true;
                    for (GroupMember previousUser : previousUsers) {
                        if (user.getUUID().equals(previousUser.getUser().getUUID())) {
                            addMe = false;
                        }
                    }
                
                    if (addMe) {
                        group.addMember(new GroupMember(user));
                    }
                }

                // Remove users ...
                DefaultListModel availableUsersModel = (DefaultListModel) availableUsersList.getModel();
                for (int i = 0; i < availableUsersModel.size(); i++) {
                    User user = (User) availableUsersModel.get(i);
                    
                    GroupMember removeMe = null;
                    for (GroupMember previousUser : previousUsers) {
                        if (user.getUUID().equals(previousUser.getUser().getUUID())) {
                            removeMe = previousUser;
                        }
                    }
                
                    if (removeMe != null) {
                        group.removeMember(removeMe);
                    }
                }
                
                securityWorkspace.commit();
            }
            
            // Success!!!
            hasUnsavedChanges = false;
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Unable to apply changes.", e);
            // return false ???
        }
    }

    public void discardChanges() {
        privilegesEditorPanel.discardChanges();
        hasUnsavedChanges = false;
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
        
        boolean disableModifyGroup = true;
        
        for (Grant g : grantsForUser) {
            if ((!g.isSystemLevel() && g.getSubject().equals(group.getUUID())) 
                    || (g.isSystemLevel() && g.getType().equals(Group.class.getName()))) {
                if (g.isModifyPrivilege()) {
                    disableModifyGroup = false;
                }
            }
        }
        
        if (disableModifyGroup) {
            nameTextField.setEnabled(false);
            currentUsersList.setEnabled(false);
            availableUsersList.setEnabled(false);
            removeAction.setEnabled(false);
            addAction.setEnabled(false);
        }
    }
    
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges || privilegesEditorPanel.hasUnsavedChanges();
    }
}
