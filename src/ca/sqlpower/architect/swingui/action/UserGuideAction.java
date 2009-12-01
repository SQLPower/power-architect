/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

/**
 * 
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.action.Messages;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;

/**
 * An {@link AbstractAction} that directs the user to how to obtain the User
 * Guide from SQL Power
 */
public class UserGuideAction extends AbstractAction {
    
    /**
     * The URL for the page where users can purchase User Documentation
     */
    public static final String USER_GUIDE_URL = "http://www.sqlpower.ca/page/architect-userguide";
    
    /**
     * Parent component for the dialog that would be displayed
     */
    private final Component parent;

    public UserGuideAction(Component parentComponent) {
        super(Messages.getString("UserGuideAction.userGuide"),  
                SPSUtils.createIcon("help", "Help", ArchitectSwingSessionContext.ICON_SIZE));
        putValue(SHORT_DESCRIPTION, Messages.getString("UserGuideAction.userGuide"));
        this.parent = parentComponent;
    }

    public void actionPerformed(ActionEvent e) {
        String[] options = {Messages.getString("UserGuideAction.takeToWebsite"), Messages.getString("UserGuideAction.close")}; //$NON-NLS-1$ //$NON-NLS-2$
        
        int choice = JOptionPane.showOptionDialog(this.parent, 
                Messages.getString("UserGuideAction.message"), //$NON-NLS-1$
                Messages.getString("UserGuideAction.title"),  //$NON-NLS-1$
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, 
                null, 
                options, 0);
        if (choice == 0) {
            try {
                BrowserUtil.launch(USER_GUIDE_URL);
            } catch (IOException e1) {
                throw new RuntimeException("Unexpected error in launch", e1); //$NON-NLS-1$
            }
        }
        
    }
}