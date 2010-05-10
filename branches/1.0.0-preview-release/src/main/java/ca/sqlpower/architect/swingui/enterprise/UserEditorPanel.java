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
import java.awt.event.ActionEvent;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.JSONResponseHandler;
import ca.sqlpower.architect.swingui.action.enterprise.RefreshProjectAction;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class UserEditorPanel implements DataEntryPanel{
    
    private static final ImageIcon RIGHT_ARROW = new ImageIcon(RefreshProjectAction.class.getResource("/icons/arrow-right.png"));
    private static final ImageIcon LEFT_ARROW = new ImageIcon(RefreshProjectAction.class.getResource("/icons/arrow-left.png"));
    
    private final ArchitectProject securityWorkspace;
    private final User user;
    
    private final JPanel panel;

    private final JLabel usernameLabel;
    private final JLabel fullnameLabel;
    private final JLabel emailLabel;
    private final JButton passwordButton;
    
    private final JTextField usernameField;
    private final JTextField fullnameField;
    private final JTextField emailField;
    
    private final JList currentGroupsList;
    private final JLabel currentGroupsLabel;
    private final JScrollPane currentGroupsScrollPane;
    
    private final JList availableGroupsList;
    private final JLabel availableGroupsLabel;
    private final JScrollPane availableGroupsScrollPane;
    
    private final String username;
    
    private final PrivilegesEditorPanel privilegesEditorPanel;

    private final Action closeAction;
    
    private final Action addAction = new AbstractAction() {
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
    
    private final Action removeAction = new AbstractAction() {
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
    
    private boolean hasUnsavedChanges = false;
    
    private final DocumentListener textFieldListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent e) { hasUnsavedChanges = true; }
        public void insertUpdate(DocumentEvent e)  { hasUnsavedChanges = true; }
        public void removeUpdate(DocumentEvent e)  { hasUnsavedChanges = true; }
    };
    
    public UserEditorPanel(User baseUser, String username, Action closeAction, final Dialog d, final ArchitectSession session) {
        this.user = baseUser;
        this.securityWorkspace = (ArchitectProject) user.getParent();
        this.username = username;
        this.closeAction = closeAction;
        
        final Dimension prefScrollPaneDimension = new Dimension(250, 300);

        usernameLabel = new JLabel("User Name");
        usernameField = new JTextField();
        usernameField.setText(user.getUsername());
        usernameField.getDocument().addDocumentListener(textFieldListener);
        
        fullnameLabel = new JLabel("Full Name");
        fullnameField = new JTextField();
        fullnameField.setText(user.getFullName());
        fullnameField.getDocument().addDocumentListener(textFieldListener);
        
        emailLabel = new JLabel("Email");
        emailField = new JTextField();
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
        
        Grant globalGrant = null;
        for (Grant grant : user.getChildren(Grant.class)) {
            if (grant.getType() != null && grant.getType().equals(ArchitectProject.class.getName())) {
                if (globalGrant != null) {
                    throw new IllegalStateException("Multiple grants for Architect Project found");
                }
                
                globalGrant = grant;
            }
        }

        if (globalGrant != null) {
            privilegesEditorPanel = new PrivilegesEditorPanel(
                    globalGrant, user, null, ArchitectProject.class.getName(), username, securityWorkspace);
        } else {
            privilegesEditorPanel = new PrivilegesEditorPanel(
                    null, user, null, ArchitectProject.class.getName(), username, securityWorkspace);
        }
        
        JButton addButton = new JButton(addAction);
        addButton.setIcon(RIGHT_ARROW);
        JButton removeButton = new JButton(removeAction);
        removeButton.setIcon(LEFT_ARROW);
        
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder upperPanelBuilder = new DefaultFormBuilder(new FormLayout(
                "pref, 5dlu, pref:grow", "pref, pref, pref, pref, pref, 5dlu"));
        upperPanelBuilder.add(usernameLabel, cc.xy(1, 1));
        upperPanelBuilder.add(usernameField, cc.xyw(3, 1, 1));
        upperPanelBuilder.add(fullnameLabel, cc.xy(1, 3));
        upperPanelBuilder.add(fullnameField, cc.xyw(3, 3, 1));
        upperPanelBuilder.add(emailLabel, cc.xy(1, 4));
        upperPanelBuilder.add(emailField, cc.xy(3, 4));

        passwordButton =  new JButton(new AbstractAction("Change Password") {
            public void actionPerformed(ActionEvent e) {

                final JDialog dialog = new JDialog(d);
                    
                final JTextField oldPasswordField = new JPasswordField(21);
                final JTextField newPasswordField = new JPasswordField(21);
                final JTextField newPasswordFiled2 = new JPasswordField(21);
                
                CellConstraints cc = new CellConstraints();
                DefaultFormBuilder dialogBuilder = new DefaultFormBuilder(new FormLayout(
                        "pref:grow", "pref, pref, pref, pref, pref, pref, pref"));
                dialogBuilder.add(new JLabel("Enter your old password"), cc.xy(1, 1));
                dialogBuilder.add(oldPasswordField, cc.xy(1, 2));
                dialogBuilder.add(new JLabel("Enter your new password"), cc.xy(1, 3));
                dialogBuilder.add(newPasswordField, cc.xy(1, 4));
                dialogBuilder.add(new JLabel("Confirm your password"), cc.xy(1, 5));
                dialogBuilder.add(newPasswordFiled2, cc.xy(1, 6));
                
                ButtonBarBuilder bbb = ButtonBarBuilder.createLeftToRightBuilder();
                bbb.addGlue();
                bbb.addGridded(new JButton(new AbstractAction("OK") {
                    public void actionPerformed(ActionEvent e) {
                        if (newPasswordField.getText().equals(newPasswordFiled2.getText())) {
                            SPServerInfo serviceInfo = ((ArchitectClientSideSession) securityWorkspace.getSession())
                            .getProjectLocation().getServiceInfo();
                            
                            HttpClient client = ArchitectClientSideSession.createHttpClient(serviceInfo);
                            
                            MessageDigest digester;
                            try {
                                digester = MessageDigest.getInstance("SHA-256");
                            } catch (NoSuchAlgorithmException e1) {
                                throw new RuntimeException(e1);
                            }
                            
                            try {
                                JSONObject json = new JSONObject();
                                json.put("username", user.getUsername());
                                json.put("oldPassword", new String(Hex.encodeHex(digester.digest(oldPasswordField.getText().getBytes()))));
                                json.put("newPassword", new String(Hex.encodeHex(digester.digest(newPasswordField.getText().getBytes()))));
                                
                                URI serverURI = new URI("http", null, 
                                        serviceInfo.getServerAddress(), 
                                        serviceInfo.getPort(),
                                        serviceInfo.getPath() + "/project/system/change_password", 
                                        null, null);
                                HttpPost postRequest = new HttpPost(serverURI);
                                postRequest.setEntity(new StringEntity(json.toString())); 
                                postRequest.setHeader("Content-Type", "application/json");
                                HttpUriRequest request = postRequest;
                                client.execute(request, new JSONResponseHandler());
                                dialog.dispose();
                            } catch (Exception x) {
                                session.createUserPrompter("The password you have entered is incorrect.", 
                                        UserPromptType.MESSAGE, 
                                        UserPromptOptions.OK, 
                                        UserPromptResponse.OK, 
                                        "OK", "OK").promptUser("");
                            }
                        } else {
                            JOptionPane.showMessageDialog(getPanel(), "The the passwords you entered were not the same");
                        }
                    }
                }));
                bbb.addRelatedGap();
                bbb.addGridded(new JButton(new AbstractAction("Cancel") {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                }));
                bbb.setDefaultButtonBarGapBorder();
                
                dialogBuilder.add(bbb.getPanel(), cc.xy(1, 7));
                dialogBuilder.setDefaultDialogBorder();
                dialog.setContentPane(dialogBuilder.getPanel());
                dialog.pack();
                dialog.setLocationRelativeTo(d);
                dialog.setVisible(true);
            }
        });
        
        ButtonBarBuilder passwordBuilder = ButtonBarBuilder.createLeftToRightBuilder();
        passwordBuilder.addGlue();
        passwordBuilder.addGridded(passwordButton);
            
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
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow", "pref, pref, 3dlu, pref:grow, 5dlu, pref"));
        builder.add(upperPanelBuilder.getPanel(), cc.xy(1, 1)); 
        builder.add(passwordBuilder.getPanel(), cc.xy(1, 2));
        builder.add(centrePanelBuilder.getPanel(), cc.xy(1, 4));
        
        DefaultFormBuilder bottomBuilder = new DefaultFormBuilder(new FormLayout("pref:grow, 5dlu, pref:grow", "pref, 3dlu, pref"));
        bottomBuilder.add(new JLabel("System Privileges"), cc.xy(1, 1));
        bottomBuilder.add(privilegesEditorPanel.getPanel(), cc.xy(1, 3));
        
        ButtonBarBuilder bbb = ButtonBarBuilder.createLeftToRightBuilder();
        bbb.addGlue();
        bbb.addGridded(new JButton(okAction));
        bbb.addRelatedGap();
        bbb.addGridded(new JButton(cancelAction));
        
        bottomBuilder.add(bbb.getPanel(), cc.xy(3, 3));
        builder.add(bottomBuilder.getPanel(), cc.xy(1, 6));
        builder.setDefaultDialogBorder();
        
        panel = builder.getPanel();
        
        fillGroupLists();
        disableIfNecessary();
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
        privilegesEditorPanel.applyChanges();
        
        try {
            if (hasUnsavedChanges()) {
                securityWorkspace.begin("Applying changes to the security model");
                
                if (!usernameField.getText().equals(user.getUsername())) {
                    user.setName(usernameField.getText());
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

    public void disableIfNecessary() {
        User creatingUser = null;
        List<Grant> grantsForUser = new ArrayList<Grant>();
        for (User aUser : securityWorkspace.getChildren(User.class)) {
            if (aUser.getUsername().equals(username)) {
                creatingUser = aUser;
            }
        }
        
        if (creatingUser == null) throw new IllegalStateException("User cannot possibly be null");
    
        for (Grant g : creatingUser.getChildren(Grant.class)) {
            grantsForUser.add(g);
        }
        
        for (Group g : securityWorkspace.getChildren(Group.class)) {
            for (GroupMember gm : g.getChildren(GroupMember.class)) {
                if (gm.getUser().getUUID().equals(creatingUser.getUsername())) {
                    for (Grant gr : g.getChildren(Grant.class)) {
                        grantsForUser.add(gr);
                    }
                }
            }
        }
        
        boolean disableModifyUser = true;
        boolean disableModifyGroups = true;

        if (username.equals(user.getUsername())) {
            disableModifyUser = false;
        }
        
        for (Grant g : grantsForUser) {
            if ((g.getSubject() != null && g.getSubject().equals(user.getUUID())) 
                    || (g.getType() != null && g.getType().equals(User.class.getName()))) {
                if (g.isModifyPrivilege()) {
                    disableModifyUser = false;
                }
            }
            
            if (g.getType() != null && g.getType().equals(Group.class.getName())) {
                if (g.isModifyPrivilege()) {
                    disableModifyGroups = false;
                }
            }
        }
        
        if (disableModifyUser) {
            usernameField.setEnabled(false);
            fullnameField.setEnabled(false);
            emailField.setEnabled(false);
            passwordButton.setEnabled(false);
        }
        
        if (disableModifyGroups) {
            addAction.setEnabled(false);
            removeAction.setEnabled(false);
            currentGroupsList.setEnabled(false);
            availableGroupsList.setEnabled(false);
        }
    }
    
    public void discardChanges() {
        privilegesEditorPanel.discardChanges();
        hasUnsavedChanges = false;
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges || privilegesEditorPanel.hasUnsavedChanges();
    }
}
