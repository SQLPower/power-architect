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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.codec.binary.Hex;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SecurityPanel {

    private final Action closeAction;
    
    private final JTree tree;
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Security");
    private final DefaultMutableTreeNode usersNode = new DefaultMutableTreeNode("Users");
    private final DefaultMutableTreeNode groupsNode = new DefaultMutableTreeNode("Groups");
    
    private final JPanel panel;
    
    private final JPanel midPanel; // contains editor for group or user
    private final JPanel lowPanel; // contains editor for privileges and apply/cancel buttons
    private final JScrollPane treePane;

    private final ArchitectProject securityWorkspace;

    private DataEntryPanel currentGroupOrUserEditPanel;
    private DataEntryPanel currentPrivilegesEditPanel;
    private DefaultMutableTreeNode selectionForEditors;
    
    private final JPopupMenu popupMenu;
    
    private final TreeSelectionListener treeListener = new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (dmtn != null && dmtn.isLeaf() && dmtn != groupsNode && dmtn != usersNode) {
                if (promptForUnsavedChanges()) {
                    createEditPanel((SPObject) dmtn.getUserObject());
                    panel.revalidate();
                    selectionForEditors = dmtn;
                } else {
                    if (selectionForEditors != null) {
                        tree.removeTreeSelectionListener(treeListener);
                        tree.setSelectionPath(new TreePath(selectionForEditors.getPath()));
                        tree.addTreeSelectionListener(treeListener);
                    }
                }
            }
        }
    };
    
    private final MouseListener popupListener = new MouseListener() {
        public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
        public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    };
    
    private final Action newUserAction = new AbstractAction("New User...") {
        public void actionPerformed(ActionEvent e) {
            if (promptForUnsavedChanges()) {
                
                User user = createUserFromPrompter();

                if (user != null) {
                    securityWorkspace.addChild(user, securityWorkspace.getChildren(User.class).size());
                    
                    refreshTree();
                    
                    Enumeration<DefaultMutableTreeNode> userNodes = usersNode.children();
                    while (userNodes.hasMoreElements()) {
                        DefaultMutableTreeNode dmtn = userNodes.nextElement();
                        
                        if (((User) dmtn.getUserObject()).getUUID().equals(user.getUUID())) {
                            tree.setSelectionPath(new TreePath(dmtn.getPath()));
                        }
                    }
                }
            }
        }
    };
    
    private final Action newGroupAction = new AbstractAction("New Group...") {
        public void actionPerformed(ActionEvent e) {
            if (promptForUnsavedChanges()) {
               Group group = createGroupFromPrompter();

                if (group != null) {
                    securityWorkspace.addChild(group, securityWorkspace.getChildren(Group.class).size());
                    
                    refreshTree();
                    
                    Enumeration<DefaultMutableTreeNode> userNodes = groupsNode.children();
                    while (userNodes.hasMoreElements()) {
                        DefaultMutableTreeNode dmtn = userNodes.nextElement();
                        
                        if (((Group) dmtn.getUserObject()).getUUID().equals(group.getUUID())) {
                            tree.setSelectionPath(new TreePath(dmtn.getPath()));
                        }
                    }
                }
            }
        }
    };
    
    private final Action deleteAction = new AbstractAction("Delete") {
        public void actionPerformed(ActionEvent e) {
            if (promptForUnsavedChanges()) {
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (dmtn != null && dmtn.isLeaf() && dmtn != groupsNode && dmtn != usersNode) {

                    if (promptForDelete((SPObject) dmtn.getUserObject())) {
                        try {
                            securityWorkspace.removeChild((SPObject) dmtn.getUserObject());
                        } catch (IllegalArgumentException e1) {
                            throw new RuntimeException("Unable to delete: ", e1);
                        } catch (ObjectDependentException e1) {
                            throw new RuntimeException("Unable to delete: ", e1);
                        }
                        
                        refreshTree();
                        tree.setSelectionPath(new TreePath(usersNode.getFirstChild()));
                    }
                }
            }
        }
    };

    private final MessageDigest digester;
    
    public SecurityPanel(SPServerInfo serverInfo, Action closeAction) {
        this.closeAction = closeAction;
        this.securityWorkspace = ArchitectClientSideSession.getSecuritySessions()
                .get(serverInfo.getServerAddress()).getWorkspace();
        
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        
        rootNode.add(usersNode);
        rootNode.add(groupsNode);
        
        midPanel = new JPanel();
        lowPanel = new JPanel();
        
        tree = new JTree(rootNode);
        tree.addTreeSelectionListener(treeListener);
        treePane = new JScrollPane(tree);
        treePane.setPreferredSize(new Dimension(250, 550));
        
        popupMenu = new JPopupMenu();
        popupMenu.add(newUserAction);
        popupMenu.add(newGroupAction);
        popupMenu.add(deleteAction);
        tree.addMouseListener(popupListener);

        JPanel rightSidePanel = new JPanel();
        rightSidePanel.setLayout(new BorderLayout());
        rightSidePanel.add(midPanel, BorderLayout.CENTER);
        rightSidePanel.add(lowPanel, BorderLayout.SOUTH);
        
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow, pref:grow", "pref"));
        builder.add(treePane, cc.xywh(1, 1, 1, 1));
        builder.add(rightSidePanel, cc.xy(2, 1));
        
        panel = builder.getPanel();

        refreshTree();
        
        try {
            tree.setSelectionPath(new TreePath(usersNode.getFirstChild()));
        } catch (NoSuchElementException e) {} // This just means that the node has no children, so we cannot expand the path.
    }
    
    private void refreshTree() {
        tree.setModel(new DefaultTreeModel(rootNode));
        usersNode.removeAllChildren();
        for (User user : securityWorkspace.getChildren(User.class)) {
           usersNode.add(new DefaultMutableTreeNode(user));
        }
        groupsNode.removeAllChildren();
        for (Group group : securityWorkspace.getChildren(Group.class)) {
            groupsNode.add(new DefaultMutableTreeNode(group));
        }
        tree.expandPath(new TreePath(usersNode.getPath()));
        tree.expandPath(new TreePath(groupsNode.getPath()));
    }
    
    private void createEditPanel(final SPObject groupOrUser) {
        DataEntryPanel groupOrUserEditPanel;
        if (groupOrUser instanceof Group) {
            groupOrUserEditPanel = new GroupEditorPanel((Group) groupOrUser);
        } else if (groupOrUser instanceof User) {
            groupOrUserEditPanel = new UserEditorPanel((User) groupOrUser);
        } else {
            throw new IllegalStateException("Argument must be instance of group or user");
        }
        
        PrivilegesEditorPanel privilegesEditorPanel = null;
        for (Grant grant : groupOrUser.getChildren(Grant.class)) {
            if (grant.getType() != null && grant.getType().equals(ArchitectProject.class.getName())) {
                if (privilegesEditorPanel != null) {
                    throw new IllegalStateException("Multiple grants for this workspace found!");
                }
                privilegesEditorPanel = new PrivilegesEditorPanel(grant, groupOrUser, null, ArchitectProject.class.getName(), securityWorkspace);
            }
        }
        
        if (privilegesEditorPanel == null) {
            privilegesEditorPanel = new PrivilegesEditorPanel(null, groupOrUser, null, ArchitectProject.class.getName(), securityWorkspace);
        }
        
        currentGroupOrUserEditPanel = groupOrUserEditPanel;
        currentPrivilegesEditPanel = privilegesEditorPanel;
        currentPrivilegesEditPanel.getPanel().setBorder(BorderFactory.createTitledBorder("System Privileges"));
        
        JButton applyButton = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                currentGroupOrUserEditPanel.applyChanges();
                currentPrivilegesEditPanel.applyChanges();
                refreshTree();
                // So that the privileges editor panel gains a ref to whatever grant may 
                // have been created, and will not try to keep adding the same grant.
                createEditPanel(groupOrUser); 
            }
        });
        
        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                if (promptForUnsavedChanges()) {
                    currentGroupOrUserEditPanel.discardChanges();
                    currentPrivilegesEditPanel.discardChanges();
                    closeAction.actionPerformed(e);
                }
            }
        });
        
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder bottomBuilder = new DefaultFormBuilder(new FormLayout(
                "pref, 3dlu, pref, 3dlu, pref", "pref"));
        bottomBuilder.add(privilegesEditorPanel.getPanel(), cc.xy(1, 1));
        bottomBuilder.add(applyButton, cc.xy(3,1));
        bottomBuilder.add(cancelButton, cc.xy(5,1));
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref", "pref"));
        builder.add(groupOrUserEditPanel.getPanel(), cc.xy(1, 1));
        
        midPanel.removeAll();
        midPanel.add(builder.getPanel());
    
        lowPanel.removeAll();
        lowPanel.add(bottomBuilder.getPanel());
        
        panel.revalidate();
    }
    
    private User createUserFromPrompter() {
        JTextField nameField = new JTextField(15);
        JTextField passField = new JPasswordField(15);
        
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(new JLabel("User Name"), BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.EAST);
        
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.add(new JLabel("Password"), BorderLayout.WEST);
        passPanel.add(passField, BorderLayout.EAST);
        
        Object[] messages = new Object[] {
                "Specify the User's Name and Password.",
                namePanel, passPanel};

        String[] options = { "Accept", "Cancel",};
        int option = JOptionPane.showOptionDialog(getPanel(), messages, "Specify the User's Name and Password", JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        
        if (nameField.getText().equals("") 
                || nameField.getText() == null 
                || passField.getText().equals("") 
                || passField.getText() == null) {
            return null;
        }
        
        User user = null; 
        if (option == 0) {
            String password;
            try {
                password = new String(Hex.encodeHex(digester.digest(passField.getText().getBytes("UTF-8"))));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Unable to encode password", e);
            }
            user = new User(nameField.getText(), password);
        }
        
        return user;
    }
    
    private Group createGroupFromPrompter() {
        Object input = JOptionPane.showInputDialog(getPanel(), "Enter a name for the group.");
        if (input != null && !input.equals("")) {
            return new Group((String) input);
        } else {
            return null;
        }
    }
    
    private boolean promptForUnsavedChanges() {
        if ((currentGroupOrUserEditPanel != null && currentPrivilegesEditPanel != null) &&
                (currentGroupOrUserEditPanel.hasUnsavedChanges() || currentPrivilegesEditPanel.hasUnsavedChanges())) {
            
            int option = JOptionPane.showConfirmDialog(getPanel(), 
                    "You have not saved all of your changes,\n" +
                    "do you want to save them now?", "", 
                     JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                currentGroupOrUserEditPanel.applyChanges();
                currentPrivilegesEditPanel.applyChanges();
                return true;
            }
            
            if (option == JOptionPane.NO_OPTION) {
                return true;
            }
            
            if (option == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        return true;
    }
    
    private boolean promptForDelete(SPObject obj) {

        String typeString = "";
        if (obj instanceof User) {
            typeString = "User";
        }
        if (obj instanceof Group) {
            typeString = "Group";
        }
        
        Object[] options = {"Yes", "No"};
        
        int option = JOptionPane.showOptionDialog(null, new Object[]{"Are you sure you want to delete the " + typeString + " \"" + obj.getName() + "\"?"}, "", JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        
        if (option == 0) {
            return true;
        }
        
        return false;
    }
    
    public JPanel getPanel() {
        return panel;
    }
}
