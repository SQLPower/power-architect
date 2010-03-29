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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.codec.binary.Hex;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class UserEditorPanel implements DataEntryPanel{
    
    private final ArchitectProject securityWorkspace;
    private final User user;
    
    private final JPanel panel;

    private final JLabel usernameLabel;
    private final JLabel passwordLabel;
    private final JLabel fullnameLabel;
    private final JLabel emailLabel;
    
    private final JTextField usernameField;
    private final JTextField passwordField;
    private final JTextField fullnameField;
    private final JTextField emailField;
    
    private final JList currentGroupsList;
    private final JLabel currentGroupsLabel;
    private final JScrollPane currentGroupsScrollPane;
    
    private final JList availableGroupsList;
    private final JLabel availableGroupsLabel;
    private final JScrollPane availableGroupsScrollPane;
    
    private final MessageDigest digester;
    
    private final Action addAction = new AbstractAction(">") {
        public void actionPerformed(ActionEvent e) {
            Object[] selection = availableGroupsList.getSelectedValues();
            
            if (selection.length==0) {
                return;
            }
            
            hasUnsavedChanges = true;
            
            DefaultListModel availableGroupsModel = (DefaultListModel) availableGroupsList.getModel();
            DefaultListModel currentGroupsModel = (DefaultListModel) currentGroupsList.getModel();
            for (Object object : selection) {
                availableGroupsModel.removeElement(object);
                currentGroupsModel.addElement(object);
            }
        }
    };
    
    private final Action removeAction = new AbstractAction("<") {
        public void actionPerformed(ActionEvent e) {
            Object[] selection = currentGroupsList.getSelectedValues();
            
            if (selection.length==0) {
                return;
            }
            
            hasUnsavedChanges = true;
            
            DefaultListModel currentGroupsModel = (DefaultListModel) currentGroupsList.getModel();
            DefaultListModel availableGroupsModel = (DefaultListModel) availableGroupsList.getModel();
            for (Object object : selection) {
                currentGroupsModel.removeElement(object);
                availableGroupsModel.addElement(object);
            }
        }
    };
    
    private boolean hasUnsavedChanges = false;
    
    private final DocumentListener textFieldListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent e) { hasUnsavedChanges = true; }
        public void insertUpdate(DocumentEvent e)  { hasUnsavedChanges = true; }
        public void removeUpdate(DocumentEvent e)  { hasUnsavedChanges = true; }
    };
    
    public UserEditorPanel(User baseUser) {
        this.user = baseUser;
        this.securityWorkspace = (ArchitectProject) user.getParent();
        
        final Dimension prefButtonDimension = new Dimension(25, 25);
        final Dimension prefScrollPaneDimension = new Dimension(250, 300);

        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        
        usernameLabel = new JLabel("User Name");
        usernameField = new JTextField(25);
        usernameField.setText(user.getUsername());
        usernameField.getDocument().addDocumentListener(textFieldListener);
        
        passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField(25);
        passwordField.getDocument().addDocumentListener(textFieldListener);
        
        fullnameLabel = new JLabel("Full Name");
        fullnameField = new JTextField(25);
        fullnameField.setText(user.getFullName());
        fullnameField.getDocument().addDocumentListener(textFieldListener);
        
        emailLabel = new JLabel("Email");
        emailField = new JTextField(25);
        emailField.setText(user.getEmail());
        emailField.getDocument().addDocumentListener(textFieldListener);
        
        availableGroupsLabel = new JLabel("Available Groups");
        availableGroupsList = new JList(new DefaultListModel());
        availableGroupsScrollPane = new JScrollPane(availableGroupsList);
        availableGroupsScrollPane.setPreferredSize(prefScrollPaneDimension);
        
        currentGroupsLabel = new JLabel("Current Groups");
        currentGroupsList = new JList(new DefaultListModel());
        currentGroupsScrollPane = new JScrollPane(currentGroupsList);
        currentGroupsScrollPane.setPreferredSize(prefScrollPaneDimension);
        
        JButton addButton = new JButton(addAction);
        addButton.setPreferredSize(prefButtonDimension);
        JButton removeButton = new JButton(removeAction);
        removeButton.setPreferredSize(prefButtonDimension);
        
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder upperPanelBuilder = new DefaultFormBuilder(new FormLayout(
                "pref, 5dlu, pref", "pref, pref, pref, pref, 5dlu"));
        upperPanelBuilder.add(usernameLabel, cc.xy(1, 1));
        upperPanelBuilder.add(usernameField, cc.xyw(3, 1, 1));
        upperPanelBuilder.add(passwordLabel, cc.xy(1, 2));
        upperPanelBuilder.add(passwordField, cc.xyw(3, 2, 1));
        upperPanelBuilder.add(fullnameLabel, cc.xy(1, 3));
        upperPanelBuilder.add(fullnameField, cc.xyw(3, 3, 1));
        upperPanelBuilder.add(emailLabel, cc.xy(1, 4));
        upperPanelBuilder.add(emailField, cc.xy(3, 4));
        
        DefaultFormBuilder buttonPanelBuilder = new DefaultFormBuilder(new FormLayout(
                "pref", "pref:grow, pref, 5dlu, pref, pref:grow"));
        buttonPanelBuilder.add(addButton, cc.xy(1,2));
        buttonPanelBuilder.add(removeButton, cc.xy(1, 5));
        
        DefaultFormBuilder centrePanelBuilder = new DefaultFormBuilder(new FormLayout(
                "pref, 5dlu, pref, 5dlu, pref", "pref, pref:grow"));
        centrePanelBuilder.add(availableGroupsLabel, cc.xy(1, 1));
        centrePanelBuilder.add(currentGroupsLabel, cc.xy(5, 1));
        centrePanelBuilder.add(availableGroupsScrollPane, cc.xy(1, 2));
        centrePanelBuilder.add(buttonPanelBuilder.getPanel(), cc.xy(3, 2));
        centrePanelBuilder.add(currentGroupsScrollPane, cc.xy(5, 2));
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref", "pref, pref:grow"));
        builder.add(upperPanelBuilder.getPanel(), cc.xy(1, 1)); 
        builder.add(centrePanelBuilder.getPanel(), cc.xy(1, 2));
        
        panel = builder.getPanel();
        
        fillGroupLists();
    }
    
    private void fillGroupLists() {
        List<Group> availableGroups = securityWorkspace.getChildren(Group.class);
        List<Group> currentGroups = new ArrayList<Group>();
        
        for (Group group : availableGroups) {
            for (GroupMember member : group.getChildren(GroupMember.class)) {
                if (member.getUser().getUUID().equals(user.getUUID())) {
                    currentGroups.add(group);
                }
            }
        }
        availableGroups.removeAll(currentGroups);
        
        DefaultListModel availableGroupsModel = (DefaultListModel) availableGroupsList.getModel();
        availableGroupsModel.removeAllElements();
        for (Group group : availableGroups) {
            availableGroupsModel.addElement(group);
        }
        
        DefaultListModel currentGroupsModel = (DefaultListModel) currentGroupsList.getModel();
        currentGroupsModel.removeAllElements();
        for (Group group : currentGroups) {
            currentGroupsModel.addElement(group);
        }
    }
    
    public JPanel getPanel() {
        return panel;
    }

    public boolean applyChanges() {
        try {
            if (hasUnsavedChanges()) {
                securityWorkspace.begin("Applying changes to the security model");
                
                if (!usernameField.getText().equals(user.getUsername())) {
                    user.setName(usernameField.getText());
                }
                if (!passwordField.getText().equals("") && !passwordField.getText().equals(user.getPassword())) {
                    String password = new String(Hex.encodeHex(digester.digest(passwordField.getText().getBytes("UTF-8"))));
                    user.setPassword(password);
                }
                if (!fullnameField.getText().equals(user.getFullName())) {
                    user.setFullName(fullnameField.getText());
                }
                if (!emailField.getText().equals(user.getEmail())) {
                    user.setEmail(emailField.getText());
                }
                
                List<Group> previousGroups = new ArrayList<Group>();
                for (Group group : securityWorkspace.getChildren(Group.class)) {
                    for (GroupMember member : group.getChildren(GroupMember.class)) {
                        if (member.getUser().getUUID().equals(user.getUUID())) {
                            previousGroups.add(group);
                        }
                    }
                }
                
                // Add to groups ...
                DefaultListModel currentGroupsModel = (DefaultListModel) currentGroupsList.getModel();
                for (int i = 0; i < currentGroupsModel.size(); i++) {
                    Group group = (Group) currentGroupsModel.get(i);
                    
                    boolean addMe = true;
                    for (Group previousGroup : previousGroups) {
                        if (previousGroup.getUUID().equals(group.getUUID())) {
                            addMe = false;
                        }
                    }
                    
                    if (addMe) {
                        group.addMember(new GroupMember(user));
                    }
                }
                
                // Remove from groups ...
                DefaultListModel availableGroupsModel = (DefaultListModel) availableGroupsList.getModel();
                for (int i = 0; i < availableGroupsModel.size(); i++) {
                    Group group = (Group) availableGroupsModel.get(i);
                    
                    GroupMember removeMe = null;
                    for (Group previousGroup : previousGroups) {
                        if (previousGroup.getUUID().equals(group.getUUID())) {
                            for (GroupMember member : group.getChildren(GroupMember.class)) {
                                if (member.getUser().getUUID().equals(user.getUUID())) {
                                    removeMe = member;
                                }
                            }
                        }
                    }
                    
                    if (removeMe != null) {
                        group.removeMember(removeMe);
                    }
                }
                
                securityWorkspace.commit();
            }
            
            // success!!!
            hasUnsavedChanges = false;
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Unable to apply changes", e);
            // return false ???
        }
    }

    public void discardChanges() {
        hasUnsavedChanges = false;
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
}
