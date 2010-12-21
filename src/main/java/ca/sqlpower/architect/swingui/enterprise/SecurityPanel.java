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
import java.awt.Component;
import java.awt.Dialog;
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
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.codec.binary.Hex;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.enterprise.AbstractNetworkConflictResolver;
import ca.sqlpower.enterprise.AbstractNetworkConflictResolver.UpdateListener;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Shows the users and groups in the system workspace and allows them to be
 * edited as well as adding and removing groups and users.
 */
public class SecurityPanel {
    
    private static final ImageIcon USER_ICON = new ImageIcon(SecurityPanel.class.getResource("icons/user.png"));
    private static final ImageIcon GROUP_ICON = new ImageIcon(SecurityPanel.class.getResource("icons/group.png"));
    
    private final Action closeAction;
    
    private final JTree tree;
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Security");
    private final DefaultMutableTreeNode usersNode = new DefaultMutableTreeNode("Users");
    private final DefaultMutableTreeNode groupsNode = new DefaultMutableTreeNode("Groups");

    /**
     * The main panel of this window and can be added to a dialog or other panel
     * to let the user modify the security settings.
     */
    private final JPanel panel;

    /**
     * Holds a tree of editable objects on the left and the editor for the
     * currently selected item on the right.
     */
    private final JSplitPane splitpane;
    
    private final JPanel rightSidePanel;
    private final JScrollPane treePane;

    private final ArchitectSwingProject securityWorkspace;
    
    private final String username;

    private DataEntryPanel currentGroupOrUserEditPanel;
    private DefaultMutableTreeNode selectionForEditors;
    
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
        public void mouseClicked(MouseEvent e)  { maybeShowPopup(e); }
        public void mouseEntered(MouseEvent e)  { maybeShowPopup(e); }
        public void mouseExited(MouseEvent e)   { maybeShowPopup(e); }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                
                final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                
                DefaultMutableTreeNode node;
                try {
                    node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    tree.setSelectionPath(path);
                } catch (Exception ex) {
                    node = null;
                }
                
                JPopupMenu popupMenu = new JPopupMenu();
                if (node != null) {
                    SPObject obj;
                    try {
                        obj = (SPObject) node.getUserObject();
                    } catch (Exception ex) {
                        obj = null;
                    }
                    popupMenu.add(new EditAction(path));
                    popupMenu.add(new EditSecuritySettingsAction(path));
                    if (obj != null) popupMenu.add(new DeleteAction(obj));
                    popupMenu.addSeparator();
                } 

                popupMenu.add(newUserAction);
                popupMenu.add(newGroupAction);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    };
    
    private final Action newUserAction = new AbstractAction("New User...") {
        public void actionPerformed(ActionEvent e) {
            if (promptForUnsavedChanges()) {
                User user = createUserFromPrompter();
                if (user != null) {
                    try {
                        securityWorkspace.begin("Creating a new user");
                        securityWorkspace.addChild(user, securityWorkspace.getChildren(User.class).size());
                        DomainCategory category = new DomainCategory(user.getName() + "'s Domains");
                        user.addGrant(new Grant(category, true, true, true, true, true));
                        securityWorkspace.addChild(category, securityWorkspace.getChildren(DomainCategory.class).size());
                        securityWorkspace.commit();
                    } catch (Exception ex) {
                        securityWorkspace.rollback(ex.getMessage());
                        throw new RuntimeException(ex);
                    }
                        
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
    
    private final MessageDigest digester;
    private final Dialog dialog;
    private final ArchitectSession session;
    
    public SecurityPanel(SPServerInfo serverInfo, Action closeAction, Dialog d, ArchitectSession session) {
        this.closeAction = closeAction;
        
        splitpane = new JSplitPane();
        panel = new JPanel();
        
        ArchitectClientSideSession clientSideSession = ArchitectClientSideSession.getSecuritySessions()
                .get(serverInfo.getServerAddress());
        
        //Displaying an indeterminate progress bar in place of the split pane
        //until the security session has loaded fully.
        if (clientSideSession.getUpdater().getRevision() <= 0) {
            JLabel messageLabel = new JLabel("Opening");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, 5dlu, pref"));
            builder.setDefaultDialogBorder();
            builder.append(messageLabel, 3);
            builder.nextLine();
            builder.append(progressBar, 3);
            
            UpdateListener l = new UpdateListener() {
                
                @Override
                public void workspaceDeleted() {
                    //do nothing
                }
                
                @Override
                public boolean updatePerformed(AbstractNetworkConflictResolver resolver) {
                    panel.removeAll();
                    panel.add(splitpane);
                    dialog.pack();
                    refreshTree();
                    return true;
                }
                
                @Override
                public boolean updateException(AbstractNetworkConflictResolver resolver, Throwable t) {
                    //do nothing, the error will be handled elsewhere
                    return true;
                }
                
                @Override
                public void preUpdatePerformed(AbstractNetworkConflictResolver resolver) {
                    //do nothing
                }
            };
            
            clientSideSession.getUpdater().addListener(l);
            panel.add(builder.getPanel());
        }
        this.securityWorkspace = clientSideSession.getWorkspace();
        this.username = serverInfo.getUsername();
        this.dialog = d;
        this.session = session;
        
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        
        rootNode.add(usersNode);
        rootNode.add(groupsNode);
        
        rightSidePanel = new JPanel();
        
        tree = new JTree(rootNode);
        tree.addTreeSelectionListener(treeListener);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObject instanceof User) {
                    setIcon(USER_ICON);
                } else if (userObject instanceof Group) {
                    setIcon(GROUP_ICON);
                }
                return this;
            }
        });
        
        
        treePane = new JScrollPane(tree);
        treePane.setPreferredSize(new Dimension(200, treePane.getPreferredSize().height));
        
        tree.addMouseListener(popupListener);

        splitpane.setRightComponent(rightSidePanel);
        splitpane.setLeftComponent(treePane);
        
        if (clientSideSession.getUpdater().getRevision() > 0) {
            panel.removeAll();
            panel.add(splitpane);
        }
        
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
            groupOrUserEditPanel = new GroupEditorPanel((Group) groupOrUser, username, closeAction);
        } else if (groupOrUser instanceof User) {
            groupOrUserEditPanel = new UserEditorPanel((User) groupOrUser, username, closeAction, session);
        } else {
            throw new IllegalStateException("Argument must be instance of Group or User");
        }

        currentGroupOrUserEditPanel = groupOrUserEditPanel;
        
        rightSidePanel.removeAll();
        rightSidePanel.add(groupOrUserEditPanel.getPanel());
        
        panel.revalidate();
        dialog.pack();
    }
    
    private User createUserFromPrompter() {
        JTextField nameField = new JTextField(15);
        JTextField passField = new JPasswordField(15);
        JTextField confirmField = new JPasswordField(15);
        
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(new JLabel("User Name"), BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.EAST);
        
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.add(new JLabel("Password"), BorderLayout.WEST);
        passPanel.add(passField, BorderLayout.EAST);
        
        JPanel confirmPanel = new JPanel(new BorderLayout());
        confirmPanel.add(new JLabel("Confirm Password"), BorderLayout.WEST);
        confirmPanel.add(confirmField, BorderLayout.EAST);
        
        Object[] messages = new Object[] {
                "Specify the User's Name and Password.",
                namePanel, passPanel, confirmPanel};

        String[] options = { "OK", "Cancel",};
        int option = JOptionPane.showOptionDialog(getPanel(), messages, 
                "Specify the User's Name and Password", JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        
        if (nameField.getText().equals("") 
                || nameField.getText() == null 
                || passField.getText().equals("") 
                || passField.getText() == null) {
            return null;
        }
        
        if(!passField.getText().equals(confirmField.getText())) {
            JOptionPane.showMessageDialog(getPanel(),
                    "The passwords you entered do not match.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
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
        Object input = JOptionPane.showInputDialog(
                getPanel(), 
                "Enter a name for the group.", 
                "Enter the name of the group.", 
                JOptionPane.INFORMATION_MESSAGE);
        if (input != null && !input.equals("")) {
            return new Group((String) input);
        } else {
            return null;
        }
    }
    
    private boolean promptForUnsavedChanges() {
        if (currentGroupOrUserEditPanel != null &&
                (currentGroupOrUserEditPanel.hasUnsavedChanges())) {
            
            int option = JOptionPane.showConfirmDialog(getPanel(), 
                    "You have not saved all of your changes,\n" +
                    "do you want to save them now?", "", 
                     JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                currentGroupOrUserEditPanel.applyChanges();
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
    
    private class EditAction extends AbstractAction {
        private final TreePath path;
        
        public EditAction(TreePath path) {
            super("Edit");
            this.path = path;
        }
        
        public void actionPerformed(ActionEvent e) {
            tree.setSelectionPath(path);
        }
    }
    
    private class EditSecuritySettingsAction extends AbstractAction {
        private final TreePath path;
        
        public EditSecuritySettingsAction(TreePath path) {
            super("Manage Security Settings...");
            this.path = path;
        }
        
        public void actionPerformed(ActionEvent e) {
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            SPObject object;
            
            try {
                object = (SPObject) node.getUserObject();
            } catch (Exception ex) {
                object = null;
            }
            
            Class<?> objectClass = null;
            if (object != null) {
                objectClass = object.getClass();
            } else {
                if (node == usersNode) {
                    objectClass = User.class;
                } else if (node == groupsNode) {
                    objectClass = Group.class;
                }
            }
            
            
            final JDialog d = SPSUtils.makeOwnedDialog(dialog, "Security Manager");
            
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
                
            ProjectSecurityPanel spm = new ProjectSecurityPanel(
                    securityWorkspace, object, objectClass, username, d, closeAction);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(spm.getPanel());
                
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(panel);
            d.setVisible(true);
        }
    }
    
    private class DeleteAction extends AbstractAction {
        private final SPObject obj;
        
        public DeleteAction(SPObject obj) {
            super("Delete");
            this.obj = obj;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (promptForUnsavedChanges()) {
                if (promptForDelete(obj)) {
                    try {
                        if (obj instanceof User) {
                            for (Group group : securityWorkspace.getChildren(Group.class)) {
                                group.removeUser((User) obj);
                            }
                        }
                        securityWorkspace.removeChild(obj);
                    } catch (Exception ex) {
                        throw new RuntimeException("Unable to delete: ", ex);
                    }
                    refreshTree();
                    tree.setSelectionPath(new TreePath(usersNode.getFirstChild()));
                }
            }
        }
    }
}
