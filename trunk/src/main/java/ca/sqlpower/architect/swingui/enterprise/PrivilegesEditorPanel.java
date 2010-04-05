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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PrivilegesEditorPanel implements DataEntryPanel {
    
    private Grant grant;
    private final SPObject groupOrUser;
    
    private final JPanel panel;
    
    private final Action checkAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
           hasUnsavedChanges = true;
        }
    };
    
    private final JCheckBox viewPrivilege = new JCheckBox(checkAction);
    private final JCheckBox createPrivilege = new JCheckBox(checkAction);
    private final JCheckBox modifyPrivilege = new JCheckBox(checkAction);
    private final JCheckBox deletePrivilege = new JCheckBox(checkAction);
    private final JCheckBox grantPrivilege = new JCheckBox(checkAction);
    
    private final String subject;
    private final String type;
    private final String username;
    
    private final ArchitectProject securityWorkspace;
    
    private boolean hasUnsavedChanges = false;
    
    public PrivilegesEditorPanel(Grant baseGrant, SPObject baseGroupOrUser, String subject, String type, String username, ArchitectProject securityWorkspace) {
        this.securityWorkspace = securityWorkspace;
        this.grant = baseGrant;
        this.groupOrUser = baseGroupOrUser;
        this.subject = subject;
        this.type = type;
        this.username = username;
        
        createPrivilege.setText("Create");
        modifyPrivilege.setText("Modify");
        deletePrivilege.setText("Delete");
        viewPrivilege.setText("View");
        grantPrivilege.setText("Grant");
        
        if (grant != null) {
            if (grant.isCreatePrivilege()) {
                createPrivilege.setSelected(true);
            }
            
            if (grant.isModifyPrivilege()) {
                modifyPrivilege.setSelected(true);
            }
            
            if (grant.isExecutePrivilege()) {
                viewPrivilege.setSelected(true);
            }
            
            if (grant.isDeletePrivilege()) {
                deletePrivilege.setSelected(true);
            }
            
            if (grant.isGrantPrivilege()) {
                grantPrivilege.setSelected(true);
            }
        }

        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref, 5dlu, pref, 5dlu, pref,", "pref:grow, 3dlu, pref:grow"));
        builder.add(viewPrivilege, cc.xy(1, 1));
        builder.add(createPrivilege, cc.xy(3, 1));
        builder.add(modifyPrivilege, cc.xy(5, 1));
        builder.add(deletePrivilege, cc.xy(1, 3));
        builder.add(grantPrivilege, cc.xy(3, 3));
        
        panel = builder.getPanel();
        
        disableIfNecessary();
    }
    
    public JCheckBox getCreatePrivilege() {
        return createPrivilege;
    }
    
    public JCheckBox getModifyPrivilege() {
        return modifyPrivilege;
    }
    
    public JCheckBox getDeletePrivilege() {
        return deletePrivilege;
    }
    
    public JCheckBox getViewPrivilege() {
        return viewPrivilege;
    }
    
    public JCheckBox getGrantPrivilege() {
        return grantPrivilege;
    }
    
    public JPanel getPanel() {
        return panel;
    }

    public boolean applyChanges() {
        try {
            if (hasUnsavedChanges()) {
                
                boolean doesNotRequireSave = false;
                if (grant != null) {
                    if ((getCreatePrivilege().isSelected() == grant.isCreatePrivilege())
                            && (getModifyPrivilege().isSelected() == grant.isModifyPrivilege())
                            && (getViewPrivilege().isSelected() == grant.isExecutePrivilege())
                            && (getDeletePrivilege().isSelected() == grant.isDeletePrivilege())
                            && (getGrantPrivilege().isSelected() == grant.isGrantPrivilege())) {
                        doesNotRequireSave = true;
                    }
                } else {
                    if ((getCreatePrivilege().isSelected() == false) 
                            && (getModifyPrivilege().isSelected() == false)
                            && (getViewPrivilege().isSelected() == false)
                            && (getDeletePrivilege().isSelected() == false)
                            && (getGrantPrivilege().isSelected() == false)) {
                        doesNotRequireSave = true;
                    }
                }
                
                if (!doesNotRequireSave) {
                    Grant newGrant = new Grant(
                            subject, type,
                            createPrivilege.isSelected(), 
                            modifyPrivilege.isSelected(),
                            deletePrivilege.isSelected(), 
                            viewPrivilege.isSelected(), 
                            grantPrivilege.isSelected());
                    
                    securityWorkspace.begin("");
                    if (groupOrUser instanceof User) {
                        if (grant != null) {
                            ((User) groupOrUser).removeGrant(grant);
                        }
                        ((User) groupOrUser).addGrant(newGrant);
                    }
                    
                    if (groupOrUser instanceof Group) {
                        if (grant != null) {
                            ((Group) groupOrUser).removeGrant(grant);
                        }
                        ((Group) groupOrUser).addGrant(newGrant);
                    }
                    securityWorkspace.commit();
                }
            }
            
            // success!!!
            hasUnsavedChanges = false;
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Unable to apply changes.", e);
            // return false ???
        }
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
                if (gm.getUser().getUUID().equals(user.getUsername())) {
                    for (Grant gr : g.getChildren(Grant.class)) {
                        grantsForUser.add(gr);
                    }
                }
            }
        }
        
        boolean disable = true;
        
        for (Grant g : grantsForUser) {
            if ((g.getSubject() != null && g.getSubject().equals(subject)) 
                    || (g.getType() != null && g.getType().equals(type))) {
                if (g.isGrantPrivilege()) {
                    disable = false;
                }
            }
        }
        
        if (disable) {
            getCreatePrivilege().setEnabled(false);
            getModifyPrivilege().setEnabled(false);
            getDeletePrivilege().setEnabled(false);
            getViewPrivilege().setEnabled(false);
            getGrantPrivilege().setEnabled(false);
        }
    }
    
    public Grant getGrant() {
        return grant;
    }
    
    public void discardChanges() {
        hasUnsavedChanges = false;
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
}
